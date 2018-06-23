package org.rebeam.tree

import cats.Monad

abstract class TransactionOps[F[_]: Monad] {

  def randomInt: F[Int]
  def randomIntUntil(bound: Int): F[Int]
  def randomLong: F[Long]
  def randomBoolean: F[Boolean]
  def randomFloat: F[Float]
  def randomDouble: F[Double]

  def context: F[TransactionContext]

  // For convenience, could use Monad directly
  def pure[A](a: A): F[A] = implicitly[Monad[F]].pure(a)
}
