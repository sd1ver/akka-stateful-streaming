package ru.neoflex.example.akka

import akka.actor.typed.ActorRef
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, EntityTypeKey }
import akka.kafka.ConsumerMessage
import akka.stream.scaladsl.Flow
import akka.stream.typed.scaladsl.ActorFlow
import akka.util.Timeout
import ru.neoflex.example.akka.CreditHistoryProcessing.log
import ru.neoflex.example.akka.model.{ BankAlertSettings, CreditAccountIndicator }

import scala.concurrent.{ ExecutionContext, Future }

case class DataWithState(state: Option[BankAlertSettings], data: CreditAccountIndicator)

class StateInterractor(
  typeKey: EntityTypeKey[Command[BankAlertSettings]],
  sharding: ClusterSharding,
  stateRegionRef: ActorRef[ShardingEnvelope[Command[BankAlertSettings]]]
)(implicit val timeout: Timeout,
  ec: ExecutionContext) {
  type Committable[T] = (T, ConsumerMessage.CommittableOffset)

  def stateRequestAsync(
    event: CreditAccountIndicator
  ): Future[DataWithState] = {
    val ref = sharding.entityRefFor(typeKey, event.bankId)
    ref.ask[StateAnswer[BankAlertSettings]](GetState(_)).map(ans => DataWithState(ans.data, event))
  }

  def stateUpdateAsync(
    state: BankAlertSettings
  ): Future[StateUpdated[BankAlertSettings]] = {
    val key = state.bankId
    val ref = sharding.entityRefFor(typeKey, key)
    ref.ask[StateUpdated[BankAlertSettings]](actor => UpdateState(state, actor))
  }

  def requestStateFlow: Flow[Committable[CreditAccountIndicator], Committable[DataWithState], _] = {
    ActorFlow.ask(1)(stateRegionRef)(requestState)
    ???
  }

  private def requestState(
    indicator: CreditAccountIndicator,
    responseActor: ActorRef[StateAnswer[BankAlertSettings]]
  ): ShardingEnvelope[Command[BankAlertSettings]] = {

    val entityId = indicator.bankId
    log.info(s"Send request to $entityId")
    ShardingEnvelope(entityId, GetState(responseActor))
  }

}
