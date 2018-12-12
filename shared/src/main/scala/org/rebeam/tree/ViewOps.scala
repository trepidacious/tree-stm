package org.rebeam.tree

import cats.Monad

abstract class ViewOps[F[_]: Monad] {

  /**
    * Get data at an [[Id]].
    * This will cause the ViewOps to fail
    * if the data is not available. The data
    * will be retrieved if possible, and the
    * view will be re-displayed. If the id
    * is not retrievable, the view will stay
    * failed.
    * @param id   The data's [[Id]]
    * @tparam A   Type of data
    * @return     The data at specified [[Id]]
    */
  def get[A](id: Id[A]): F[A]

  /**
    * Get data at an [[Id]].
    * This wil return F[None] if the data is not
    * available. The data will be retrieved
    * if possible, and the view will be
    * re-displayed. If the id is not
    * retrievable, the view will not be redisplayed.
    * @param id   The data's [[Id]]
    * @tparam A   Type of data
    * @return     The data at specified [[Id]], or
    *             None if not available.
    */
  def getOption[A](id: Id[A]): F[Option[A]]

  // For convenience, could use Monad directly
  def pure[A](a: A): F[A] = implicitly[Monad[F]].pure(a)
}



