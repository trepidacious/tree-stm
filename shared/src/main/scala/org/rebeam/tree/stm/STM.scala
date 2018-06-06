package org.rebeam.tree.stm

import cats.data.State
import org.rebeam.tree.random.PRandom

object STM {
  type Guid = Long

// TODO there might be some way to provide these functions to enable one import STM._ rather than
// an import stm._ in every program, but the [A] makes it difficult.
//  def get[A, F[_]](id: Id[A])(implicit stm: STM[F]): F[Option[A]] = stm.get(id)
//  def set[A, F[_]](id: Id[A], a: A)(implicit stm: STM[F]): F[Unit] = stm.set(id, a)
//
//  def randomInt[F[_]](implicit stm: STM[F]): F[Int] = stm.randomInt
//  def randomIntUntil[F[_]](bound: Int)(implicit stm: STM[F]): F[Int] = stm.randomIntUntil(bound)
//  def randomLong[F[_]](implicit stm: STM[F]): F[Long] = stm.randomLong
//  def randomBoolean[F[_]](implicit stm: STM[F]): F[Boolean] = stm.randomBoolean
//  def randomFloat[F[_]](implicit stm: STM[F]): F[Float] = stm.randomFloat
//  def randomDouble[F[_]](implicit stm: STM[F]): F[Double] = stm.randomDouble

}

/**
  * An identifier for an item of data of a known type, using a Guid
  * @param guid The Guid
  * @tparam A Type of the identified item
  */
case class Id[+A](guid: STM.Guid)


trait STM[F[_]] {

  def get[A](id: Id[A]): F[Option[A]]
  def set[A](id: Id[A], a: A): F[Unit]

  def randomInt: F[Int]
  def randomIntUntil(bound: Int): F[Int]
  def randomLong: F[Long]
  def randomBoolean: F[Boolean]
  def randomFloat: F[Float]
  def randomDouble: F[Double]

}


/**
  * Implementation of STM using a Map and PRandom as State
  */
object MapStateSTM {
  import STM._

  case class StateData(map: Map[Guid, Any], random: PRandom)

  def emptyState: StateData = StateData(Map.empty, PRandom(0))

  type S[A] = State[StateData, A]

  def rand[A](rf: PRandom => (PRandom, A)): S[A] = State(sd => {
    val (newRandom, a) = rf(sd.random)
    (sd.copy(random = newRandom), a)
  })

  implicit val stmInstance: STM[S] = new STM[S] {
    def get[A](id: Id[A]): S[Option[A]] = State(sd => (sd, sd.map.get(id.guid).map(_.asInstanceOf[A])))
    def set[A](id: Id[A], a: A): S[Unit] = State(sd => (sd.copy(map = sd.map + (id.guid -> a)), ()))

    def randomInt: S[Int] = rand(_.int)
    def randomIntUntil(bound: Int): S[Int] = rand(_.intUntil(bound))
    def randomLong: S[Long] = rand(_.long)
    def randomBoolean: S[Boolean] = rand(_.boolean)
    def randomFloat: S[Float] = rand(_.float)
    def randomDouble: S[Double] = rand(_.double)
  }
}
