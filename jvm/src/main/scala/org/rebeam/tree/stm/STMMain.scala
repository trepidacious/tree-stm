package org.rebeam.tree.stm

import MapStateSTM._
import cats.Monad
import cats.implicits._

object STMMain {

  def example[F[_]: Monad](implicit stm: STM[F]): F[Option[Int]] = {
    import stm._
    for {
      a <- get[Int](Id(0))
      _ <- set[Int](Id(1), 1)
    } yield a
  }

  def main(args: Array[String]): Unit = {
    val e: S[Option[Int]] = example[S]
    println(e.run(emptyState).value)
  }

}
