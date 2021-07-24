package ru.neoflex.example.akka.model

import spray.json._

case class CreditAccountIndicator(
  accountId: Long,
  bankId: String,
  payment: BigDecimal,      // размер платежа
  debtAmount: BigDecimal,   // размер задолженности
  overdueAmount: BigDecimal // размер просроченной задолжности
)

object CreditAccountIndicator extends DefaultJsonProtocol {
  implicit val creditAccountIndicatorFormat: RootJsonFormat[CreditAccountIndicator] = jsonFormat5(
    CreditAccountIndicator.apply
  )
}
