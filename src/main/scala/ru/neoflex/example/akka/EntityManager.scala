package ru.neoflex.example.akka

import akka.actor.typed.{ ActorRef, Behavior }
import akka.cluster.sharding.typed.scaladsl.EntityContext
import akka.event.slf4j.SLF4JLogging
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior }

sealed trait BankAlertSerializable
sealed trait Command[R] extends BankAlertSerializable

final case class UpdateState[R](data: R, replyTo: ActorRef[StateUpdated[R]]) extends Command[R]
case class StateUpdated[R](data: Option[R])                                  extends BankAlertSerializable

final case class GetState[R](replyTo: ActorRef[StateAnswer[R]]) extends Command[R]

case class StateAnswer[R](data: Option[R]) extends BankAlertSerializable

case class Event[R](data: R) extends BankAlertSerializable

final case class State[R](value: Option[R]) {
  def update(data: R): State[R] = copy(value = Some(data))
}

class EntityManager[R] extends SLF4JLogging {

  def create(entityContext: EntityContext[Command[R]]): Behavior[Command[R]] = {
    val persistenceId = PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId)
    log.info(s"Start entity for  ${entityContext.entityId}")
    create(persistenceId)
  }

  def create(persistenceId: PersistenceId): Behavior[Command[R]] =
    EventSourcedBehavior[Command[R], Event[R], State[R]](
      persistenceId = persistenceId,
      emptyState = State[R](None),
      handleCommand,
      handleEvent
    )

  def handleCommand(state: State[R], command: Command[R]): Effect[Event[R], State[R]] = {
    command match {
      case UpdateState(data, replyTo) =>
        val event = Event(data)
        Effect.persist(event).thenReply(replyTo)(s => StateUpdated(s.value))
      case GetState(replyTo)          =>
        val answer = StateAnswer(state.value)
        log.info(s"Answer state ${answer.data}")
        Effect.reply(replyTo)(answer)
    }
  }

  def handleEvent(state: State[R], event: Event[R]): State[R] = {
    state.update(event.data)
  }

}
