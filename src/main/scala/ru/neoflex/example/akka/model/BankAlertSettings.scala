package ru.neoflex.example.akka.model

import spray.json._

case class BankAlertSettings(
  bankId: String,
  paymentThreshold: Option[BigDecimal],       //настройка порога по платежу
  debtAmountThreshold: Option[BigDecimal],    //настройка порога по задолженности
  overdueAmountThreshold: Option[BigDecimal]  //настройка порога по просрочке
)

object BankAlertSettings extends DefaultJsonProtocol {
  implicit val bankAlertSettingsFormat: JsonFormat[BankAlertSettings] = jsonFormat4(BankAlertSettings.apply)
}
