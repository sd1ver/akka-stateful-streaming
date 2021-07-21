package ru.neoflex.example.akka

import akka.Done
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.external.ExternalShardAllocationStrategy
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import akka.kafka.ConsumerMessage.Committable
import akka.kafka.ProducerMessage.Envelope
import akka.kafka._
import akka.kafka.cluster.sharding.KafkaClusterSharding
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import ru.neoflex.example.akka.model.BankAlertSettings

import scala.concurrent.Future
import scala.concurrent.duration._

//noinspection TypeAnnotation
trait StreamSources {

  def committerSettings: CommitterSettings

  def dataSink(implicit system: ActorSystem): Sink[Envelope[String, String, Committable], Future[Done]] = {
    val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    Producer.committableSink(producerSettings, committerSettings)
  }

  def stateCommandSource(topic: String)(implicit system: ActorSystem) = {
    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    val subscription     = Subscriptions.topics(topic)
    Consumer.sourceWithOffsetContext(consumerSettings, subscription)
  }

  def createShardedKafkaSource(
    sharding: ClusterSharding,
    entity: Entity[Command[BankAlertSettings], ShardingEnvelope[Command[BankAlertSettings]]],
    topic: String,
    kafkaTimeout: FiniteDuration = 10.seconds
  )(implicit system: ActorSystem
  ) = {
    val consumerSettings  = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    val kafkaSharding     = KafkaClusterSharding(system)
    val rebalanceListener = kafkaSharding.rebalanceListener(entity.typeKey)
    val subscription      = Subscriptions.topics(topic).withRebalanceListener(rebalanceListener.toClassic)
    val messageExtractor  =
      kafkaSharding.messageExtractor[Command[BankAlertSettings]](topic, kafkaTimeout, consumerSettings)

    val allocationStrategy = new ExternalShardAllocationStrategy(system, entity.typeKey.name)
    Source.futureSource {
      messageExtractor.map { m =>
        sharding.init(entity.withAllocationStrategy(allocationStrategy).withMessageExtractor(m))
        Consumer.sourceWithOffsetContext(consumerSettings, subscription).asSource
      }(system.dispatcher)
    }.asSourceWithContext { case (_, committableOffset) => committableOffset }.map { case (record, _) => record }
  }

}
