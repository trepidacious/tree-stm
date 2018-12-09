package org.rebeam.tree

import cats.data.StateT
import cats.implicits._

/**
  * Implementation of ViewOps using a DataSource
  */
object DataSourceViewOps {

  case class Error(
    errorGuid: Guid,
    viewedGuids: Set[Guid],
    missingGuids: Set[Guid])



  case class StateData(
      dataSource: DataSource,
      viewedGuids: Set[Guid],
      missingGuids: Set[Guid]) {
    def viewed(guid: Guid): StateData = copy(viewedGuids = viewedGuids + guid)
    def missed(guid: Guid): StateData = copy(missingGuids = missingGuids + guid)
  }

  def initialStateData(dataSource: DataSource): StateData = StateData(dataSource, Set.empty, Set.empty)

  type ErrorOr[A] = Either[Error, A]
  type S[A] = StateT[ErrorOr, StateData, A]

  implicit val viewOpsInstance: ViewOps[S] = new ViewOps[S] {

    def get[A](id: Id[A]): S[A] =
      StateT[ErrorOr, StateData, A](sd => {
        //Get data as an option, map this to an Option[(StateData, A)] by adding a new state
        //updated to record viewing the id, then convert to an ErrorOr with appropriate
        //Error on missing data. This is the S => F[S, A] required by StateT.
        sd.dataSource.get(id)
          .map((sd.viewed(id.guid), _))
          .toRight(Error(
            id.guid,
            sd.viewedGuids + id.guid,
            sd.missingGuids + id.guid
          ))
      })

    def getOption[A](id: Id[A]): S[Option[A]] =
      StateT[ErrorOr, StateData, Option[A]](sd => {
        sd.dataSource.get(id).fold[ErrorOr[(StateData, Option[A])]](
          Right((sd.missed(id.guid).viewed(id.guid), None))
        )(
          a => Right((sd.viewed(id.guid), Some(a)))
        )
      })

  }
}
