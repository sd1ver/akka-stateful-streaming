package ru.neoflex.example.akka

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, Entity, EntityTypeKey }
import akka.event.slf4j.SLF4JLogging
import akka.kafka.ConsumerMessage.Committable
import akka.kafka.ProducerMessage.Envelope
import akka.kafka._
import akka.kafka.scaladsl.Committer
import akka.stream.scaladsl.Keep
import akka.util.Timeout
import org.apache.kafka.clients.producer.ProducerRecord
import ru.neoflex.example.akka.model.{ AlertEvent, BankAlertSettings, CreditAccountIndicator }
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration._
object CreditHistoryProcessing extends SLF4JLogging with StreamSources {

  implicit val actorSystem: ActorSystem = ActorSystem("CreditHistoryProcessor")
  import actorSystem.dispatcher

  implicit val timeout: Timeout = 10.second

  override val committerSettings: CommitterSettings =
    CommitterSettings(actorSystem).withCommitWhen(CommitWhen.OffsetFirstObserved)

  private val typeKey = EntityTypeKey[Command[BankAlertSettings]](classOf[BankAlertSettings].getSimpleName)

  private val alertTopic = "client-alerts"

  def main(args: Array[String]): Unit = {
    val entityManager    = new EntityManager[BankAlertSettings]()
    val entity           = Entity(typeKey)(entityManager.create)
    val sharding         = ClusterSharding(actorSystem.toTyped)
    val region           = sharding.init(entity)
    val stateInterractor = new StateInterractor(typeKey, sharding, region)

    val indicatorsSource = createShardedKafkaSource(sharding, entity, "credit-indicators")

    val indicatorsComplete = indicatorsSource
      .map(r => r.value.parseJson.convertTo[CreditAccountIndicator])
      .mapAsync(1)(stateInterractor.stateRequestAsync)
      .map(calcAlerts)
      .asSource
      .map(toEnvelope)
      .toMat(dataSink)(Keep.right)
      .run()

    val settingsSource   = stateCommandSource("bank-settings")
    val offsetSink       = Committer.sink(committerSettings)
    val settingsComplete = settingsSource
      .map(r => r.value.parseJson.convertTo[BankAlertSettings])
      .mapAsync(1)(stateInterractor.stateUpdateAsync)
      .asSource
      .map { case (result, committable) => log.info(s"State updated to ${result.data}"); committable }
      .toMat(offsetSink)(Keep.right)
      .run()

    Await.result(indicatorsComplete, Duration.Inf)
    Await.result(settingsComplete, Duration.Inf)
    actorSystem.terminate()
  }

  def toEnvelope(data: (Either[Unit, AlertEvent], Committable)): Envelope[String, String, Committable] = {
    val (alertOrNone, committable) = data
    alertOrNone.fold(
      _ => ProducerMessage.passThrough(committable),
      alertToEnvelope(committable)
    )
  }

  def alertToEnvelope(committable: Committable)(alert: AlertEvent): Envelope[String, String, Committable] = {
    val message = alert.toJson.compactPrint
    val record  = new ProducerRecord[String, String](alertTopic, message, alert.bankId)
    ProducerMessage.single(record, committable)
  }

  private def calcAlerts(dataWithState: DataWithState): Either[Unit, AlertEvent] = {
    import dataWithState._
    val thresholdsWithIndicators = Seq(
      state.flatMap(_.paymentThreshold)       -> data.payment,
      state.flatMap(_.overdueAmountThreshold) -> data.overdueAmount,
      state.flatMap(_.debtAmountThreshold)    -> data.debtAmount
    )
    thresholdsWithIndicators
      .flatMap { case (threshold, value) => threshold.find(_ > value) }
      .headOption
      .map(_ => AlertEvent(data.accountId, data.bankId, s"Threshold exceeded for $data"))
      .toRight(())
  }

}
