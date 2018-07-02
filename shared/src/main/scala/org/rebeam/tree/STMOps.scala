package org.rebeam.tree

import cats.Monad

object STMOps {

// TODO there might be some way to provide these functions to enable one import STM._ rather than
// an import stm._ in every program, but the [A] makes it difficult.

//  case class Getter[A]() {
//    def apply[F[_]](id: Id[A])(implicit stm: STMOps[F]): F[Option[A]] = stm.get(id)
//  }
//
//  case class Setter[A]() {
//    def apply[F[_]](id: Id[A], a: A)(implicit stm: STMOps[F]): F[Unit] = stm.set(id, a)
//  }
//
//  case class PutterF[A]() {
//    def apply[F[_]](create: Id[A] => F[A])(implicit stm: STMOps[F]): F[A] = stm.putF(create)
//  }
//
//  case class Putter[A]() {
//    def apply[F[_]](create: Id[A] => A)(implicit stm: STMOps[F]): F[A] = stm.put(create)
//  }
//
//  def set[A]: Setter[A] = Setter[A]()
//
//  def putF[A]: PutterF[A] = PutterF[A]()
//
//  def put[A]: Putter[A] = Putter[A]()
//
//  def get[A]: Getter[A] = Getter[A]()
//
//  //  def get[A, F[_]](id: Id[A])(implicit stm: STMOps[F]): F[Option[A]] = stm.get(id)
//  //  def set[A, F[_]](id: Id[A], a: A)(implicit stm: STMOps[F]): F[Unit] = stm.set(id, a)
////
//  def randomInt[F[_]](implicit stm: STMOps[F]): F[Int] = stm.randomInt
//  def randomIntUntil[F[_]](bound: Int)(implicit stm: STMOps[F]): F[Int] = stm.randomIntUntil(bound)
//  def randomLong[F[_]](implicit stm: STMOps[F]): F[Long] = stm.randomLong
//  def randomBoolean[F[_]](implicit stm: STMOps[F]): F[Boolean] = stm.randomBoolean
//  def randomFloat[F[_]](implicit stm: STMOps[F]): F[Float] = stm.randomFloat
//  def randomDouble[F[_]](implicit stm: STMOps[F]): F[Double] = stm.randomDouble

}



abstract class STMOps[F[_]: Monad] extends TransactionOps {

  def get[A](id: Id[A]): F[Option[A]]
  def set[A](id: Id[A], a: A): F[Unit]

  def modifyF[A](id: Id[A], f: A => F[A]): F[Option[A]]

  //Safer if this is used only via put and putF
  //def createGuid: F[Guid]

  def putF[A](create: Id[A] => F[A]): F[A]

  // For convenience, allow use of plain A
  def put[A](create: Id[A] => A): F[A] = putF(create.andThen(pure))
  def modify[A](id: Id[A], f: A => A): F[Option[A]] = modifyF(id, f.andThen(pure))

}



