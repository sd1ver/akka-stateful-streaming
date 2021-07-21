package ru.neoflex.example.akka.model

import spray.json._

case class AlertEvent(accountId: Long, bankId: String, message: String)

object AlertEvent extends DefaultJsonProtocol {
  implicit val alertEventFormat: JsonFormat[AlertEvent] = jsonFormat3(AlertEvent.apply)
}
