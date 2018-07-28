package org.rebeam.tree

import cats.data.StateT
import cats.implicits._

/**
  * Implementation of ViewOps using a function from Id[A] to Option[A]
  */
object DataSourceViewOps {

  case class Error(errorGuid: Guid, missingGuids: Set[Guid])

  trait DataSource {
    def get[A](id: Id[A]): Option[A]
  }

  case class StateData(
      dataSource: DataSource,
      missingGuids: Set[Guid])

  def initialStateData(dataSource: DataSource): StateData = StateData(dataSource, Set.empty)

  type ErrorOr[A] = Either[Error, A]
  type S[A] = StateT[ErrorOr, StateData, A]

  implicit val viewOpsInstance: ViewOps[S] = new ViewOps[S] {

    def get[A](id: Id[A]): S[A] =
      StateT[ErrorOr, StateData, A](sd => {
        sd.dataSource.get(id).map((sd, _)).toRight(Error(id.guid, sd.missingGuids + id.guid))
      })

    def getOption[A](id: Id[A]): S[Option[A]] =
      StateT[ErrorOr, StateData, Option[A]](sd => {
        sd.dataSource.get(id).fold[ErrorOr[(StateData, Option[A])]](
          Right((sd.copy(missingGuids = sd.missingGuids + id.guid), None))
        )(
          a => Right((sd, Some(a)))
        )
      })

  }
}
