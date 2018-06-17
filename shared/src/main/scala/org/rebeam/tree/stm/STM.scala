package org.rebeam.tree.stm

import cats.Monad

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


abstract class STM[F[_]: Monad] {

  def get[A](id: Id[A]): F[Option[A]]
  def set[A](id: Id[A], a: A): F[Unit]

  def modifyF[A](id: Id[A], f: A => F[A]): F[Option[A]]

  def randomInt: F[Int]
  def randomIntUntil(bound: Int): F[Int]
  def randomLong: F[Long]
  def randomBoolean: F[Boolean]
  def randomFloat: F[Float]
  def randomDouble: F[Double]

  def context: F[TransactionContext]

  //Safer if this is used only via put and putF
  //def createGuid: F[Guid]

  def putF[A](create: Id[A] => F[A]): F[A]

  // For convenience, allow use of plain A
  def put[A](create: Id[A] => A): F[A] = putF(create.andThen(pure))
  def modify[A](id: Id[A], f: A => A): F[Option[A]] = modifyF(id, f.andThen(pure))

  // For convenience, could use Monad directly
  def pure[A](a: A): F[A] = implicitly[Monad[F]].pure(a)
}



