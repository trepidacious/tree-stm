package org.rebeam.tree.stm

import cats.Monad
import cats.data.State
import cats.implicits._

object STM {
  type Guid = Long
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

}

object STMExample {
  def example[F[_]: Monad](implicit stm: STM[F]): F[Unit] = {
    import stm._
    for {
      a <- get[Int](Id(0))
      _ <- set[Int](Id(1), 1)
    } yield ()
  }
}

object MapSTM {
  import STM._
  type S[A] = State[Map[Guid, Any], A]

}