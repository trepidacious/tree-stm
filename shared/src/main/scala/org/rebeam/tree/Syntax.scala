package org.rebeam.tree

import cats.Monad
import cats.implicits._

object Syntax {
  implicit class RefList[A](l: List[Ref[A]]) {
    def deref[F[_]: Monad](implicit stm: STMOps[F]): F[List[A]] =
      l.traverse[F, A](ref => stm.get(ref.id))
  }
}
