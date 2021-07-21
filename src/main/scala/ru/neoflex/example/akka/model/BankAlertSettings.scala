package ru.neoflex.example.akka.model

import spray.json._

case class BankAlertSettings(
  bankId: String,
  paymentThreshold: Option[BigDecimal],
  debtAmountThreshold: Option[BigDecimal],
  overdueAmountThreshold: Option[BigDecimal])

object BankAlertSettings extends DefaultJsonProtocol {
  implicit val bankAlertSettingsFormat: JsonFormat[BankAlertSettings] = jsonFormat4(BankAlertSettings.apply)
}
