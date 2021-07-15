package ru.neoflex.example.akka

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.concurrent.duration._
import scala.collection.immutable
import scala.concurrent.Await

object CreditHistoryProcessing extends SLF4JLogging{

  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("CreditHistoryProcessor")
    val intSource = Source.fromIterator(() => immutable.Seq(1, 2, 3).iterator)
    val printSink = Sink.foreach[Int](i => log.info(i.toString))
    val result = intSource.toMat(printSink)(Keep.right).run()
    Await.result(result, 1.second)
    actorSystem.terminate()
  }
}
