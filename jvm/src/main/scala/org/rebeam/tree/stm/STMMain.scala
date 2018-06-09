package org.rebeam.tree.stm

import MapStateSTM._
import cats.Monad
import cats.implicits._

object STMMain {

  case class ThingWithId(id: Id[ThingWithId], name: String)

  def example[F[_]: Monad](implicit stm: STM[F]): F[Option[ThingWithId]] = {
    import stm._
    for {
      thing <- put[ThingWithId](id => ThingWithId(id, "I'm a thing!"))
      _ <- putF[Int](_ => randomInt)
      _ <- putF[Int](_ => randomInt)
      _ <- put[ThingWithId](id => ThingWithId(id, "I'm a thing!"))
      a <- get(thing.id)
      _ <- modify[ThingWithId](thing.id, _.copy(name = "I'm a modified thing!"))
    } yield a
  }

  def main(args: Array[String]): Unit = {
    val (state, result) = example[S].run(emptyState).value
    println(state)
    println(result)
  }

}
