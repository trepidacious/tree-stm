package org.rebeam.tree

import cats.Monad
import org.rebeam.tree.codec.IdCodec

abstract class STMOps[F[_]: Monad] extends TransactionOps {

  def get[A](id: Id[A]): F[Option[A]]

  /**
    * Modify data at an id
    * @param id   The data's [[Id]]
    * @param f    Function to transform old value to new one
    * @tparam A   Type of data
    * @return     The modified data
    */
  def modifyF[A](id: Id[A], f: A => F[A]): F[Option[A]]

  /**
    * Put a new value into the STM. This will create a new
    * Id, and this is used to create the data to add to the
    * STM (in case the data includes the Id).
    *
    * @param create   Function to create data from Id
    * @param idCodec  Used to encode/decode data
    *                 and deltas
    * @tparam A       The type of data
    * @return         The created data
    */
  def putF[A](create: Id[A] => F[A])(implicit idCodec: IdCodec[A]): F[A]

  // For convenience, allow use of plain A
  def put[A](create: Id[A] => A)(implicit idCodec: IdCodec[A]): F[A] = putF(create.andThen(pure))
  def modify[A](id: Id[A], f: A => A): F[Option[A]] = modifyF(id, f.andThen(pure))

}



