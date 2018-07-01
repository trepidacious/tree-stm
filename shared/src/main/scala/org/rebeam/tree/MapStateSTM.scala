package org.rebeam.tree

import cats.data.State
import cats.implicits._
import org.rebeam.tree.Guid._

/**
  * Implementation of STM using a Map and PRandom as State
  */
object MapStateSTM {

  case class StateData(nextGuid: Guid, map: Map[Guid, Any], random: PRandom, context: TransactionContext)

  def emptyState: StateData = StateData(Guid(SessionId(0), SessionTransactionId(0), TransactionClock(0)), Map.empty, PRandom(0), TransactionContext(Moment(0)))

  type S[A] = State[StateData, A]

  private def rand[A](rf: PRandom => (PRandom, A)): S[A] = State(sd => {
    val (newRandom, a) = rf(sd.random)
    (sd.copy(random = newRandom), a)
  })

  implicit val stmInstance: STMOps[S] = new STMOps[S] {
    def get[A](id: Id[A]): S[Option[A]] = State(sd => (sd, sd.map.get(id.guid).map(_.asInstanceOf[A])))
    def set[A](id: Id[A], a: A): S[Unit] = State(sd => (sd.copy(map = sd.map + (id.guid -> a)), ()))

    def modifyF[A](id: Id[A], f: A => S[A]): S[Option[A]] = for {
      a1 <- get[A](id)
      a2 <- a1.traverse(f)
      _ <- a2.map(v => set(id, v)).sequence
      // IDEA not happy with this shorter alternative for some reason...
      //_ <- a2.traverse(v => set(id, v))
    } yield {
      println(id + ": " + a1 + " -> " + a2)
      a2
    }

    def randomInt: S[Int] = rand(_.int)
    def randomIntUntil(bound: Int): S[Int] = rand(_.intUntil(bound))
    def randomLong: S[Long] = rand(_.long)
    def randomBoolean: S[Boolean] = rand(_.boolean)
    def randomFloat: S[Float] = rand(_.float)
    def randomDouble: S[Double] = rand(_.double)

    def context: S[TransactionContext] = State.inspect(_.context)

    private def createGuid: S[Guid] = State(sd => (sd.copy(nextGuid = sd.nextGuid.copy(transactionClock = sd.nextGuid.transactionClock.next)), sd.nextGuid))

    def putF[A](create: Id[A] => S[A]) : S[A] = for {
      id <- createGuid.map(Id[A] _)
      a <- create(id)
      _ <- set(id, a)
    } yield a

  }
}
