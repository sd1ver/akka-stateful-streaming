package ru.neoflex.example.akka.model

import spray.json._

case class CreditAccountIndicator(
  accountId: Long,
  bankId: String,
  payment: BigDecimal,
  debtAmount: BigDecimal,
  overdueAmount: BigDecimal)

object CreditAccountIndicator extends DefaultJsonProtocol {
  implicit val creditAccountIndicatorFormat: RootJsonFormat[CreditAccountIndicator] = jsonFormat5(
    CreditAccountIndicator.apply
  )
}
