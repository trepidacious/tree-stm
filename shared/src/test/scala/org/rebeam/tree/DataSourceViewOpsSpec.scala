package org.rebeam.tree

import cats.implicits._
import org.scalatest._
import org.scalatest.prop.Checkers
import SpecUtils._
import DataSourceViewOps._
import cats.Monad

class DataSourceViewOpsSpec extends WordSpec with Matchers with Checkers {

  case class MapDataSource(map: Map[Guid, Any]) extends DataSource {
    override def get[A](id: Id[A]): Option[A] = map.get(id.guid).map(_.asInstanceOf[A])

    def put[A](id: Id[A], a: A): MapDataSource =
      copy(map = map.updated(id.guid, a))
  }

  object MapDataSource {
    val empty: MapDataSource = MapDataSource(Map.empty)
  }

  val id0: Id[Int] = Id[Int](guid(0, 0, 0))
  val id1: Id[Int] = Id[Int](guid(0, 0, 1))
  val id2: Id[String] = Id[String](guid(0, 0, 2))
  val idInfinity: Id[Int] = Id[Int](guid(42, 42, 42))
  val idInfinitySquared: Id[Int] = Id[Int](guid(84, 84, 84))

  val dataSource: DataSource =
    MapDataSource.empty
      .put(id0, 0)
      .put(id1, 1)
      .put(id2, "2")

  def printValidIds[F[_]: Monad](implicit v: ViewOps[F]): F[String] = {
    import v._
    for {
      zero <- get[Int](id0)
      one <- get[Int](id1)
      two <- get[String](id2)
    } yield s"$zero, $one, $two"
  }

  def printInvalidIds[F[_]: Monad](implicit v: ViewOps[F]): F[String] = {
    import v._
    for {
      zero <- get[Int](id0)
      one <- get[Int](id1)
      two <- get[String](id2)
      infinity <- get[Int](idInfinity)
    } yield s"$zero, $one, $two, $infinity"
  }

  def printIdsWithOption[F[_]: Monad](implicit v: ViewOps[F]): F[String] = {
    import v._
    for {
      zero <- get[Int](id0)
      one <- get[Int](id1)
      infinity <- getOption[Int](idInfinity)
      two <- get[String](id2)
    } yield s"$zero, $one, $two, ${infinity.getOrElse("MISSING")}"
  }

  def printIdsWithOptionThenInvalidGet[F[_]: Monad](implicit v: ViewOps[F]): F[String] = {
    import v._
    for {
      zero <- get[Int](id0)
      one <- get[Int](id1)
      infinity <- getOption[Int](idInfinity)
      two <- get[String](id2)
      infinitySquared <- get[Int](idInfinitySquared)
    } yield s"$zero, $one, $two, ${infinity.getOrElse("MISSING")}, $infinitySquared"
  }

  def printIdsWithInvalidGetThenInvalidGetOption[F[_]: Monad](implicit v: ViewOps[F]): F[String] = {
    import v._
    for {
      zero <- get[Int](id0)
      one <- get[Int](id1)
      infinity <- get[Int](idInfinity)
      two <- get[String](id2)
      infinitySquared <- getOption[Int](idInfinitySquared)
    } yield s"$zero, $one, $two, $infinity, $infinitySquared"
  }

  "DataSourceViewOps" should {

    "access data" in {

      val (state, result) = runView(printValidIds[S], initialStateData(dataSource))

      assert(state.missingGuids.isEmpty)
      assert(state.viewedGuids == Set(id0, id1, id2).map(_.guid))
      assert(result == "0, 1, 2")

    }

    "fail to get missing data" in {

      val error = failView(printInvalidIds[S], initialStateData(dataSource))

      assert(error.errorGuid == idInfinity.guid)
      // Note that the failed attempt to get the invalid Id still means the Id was viewed
      assert(error.viewedGuids == Set(id0, id1, id2, idInfinity).map(_.guid))
      assert(error.missingGuids == Set(idInfinity.guid))

    }

    "tolerate missing data with getOption" in {

      val (state, result) = runView(printIdsWithOption[S], initialStateData(dataSource))

      assert(state.missingGuids == Set(idInfinity.guid))
      // Note that getting None for the invalid Id still means the Id was viewed
      assert(state.viewedGuids == Set(id0, id1, id2, idInfinity).map(_.guid))
      assert(result == "0, 1, 2, MISSING")
    }

    "fail to get missing data, and also notice tolerated missing data with getOption" in {

      val error = failView(printIdsWithOptionThenInvalidGet[S], initialStateData(dataSource))

      // We fail because of inf squared, which is used with a get, but we also
      // notice the missing idInfinity used earlier with getOption
      assert(error.errorGuid == idInfinitySquared.guid)
      // Note that the idInfinity and IdInfinitySquared are both accessed, as an Option None and a failed access,
      // respectively
      assert(error.viewedGuids == Set(id0, id1, id2, idInfinity, idInfinitySquared).map(_.guid))
      assert(error.missingGuids == Set(idInfinity.guid, idInfinitySquared.guid))
    }

    "stop on get missing data, not running later getOption" in {

      val error = failView(printIdsWithInvalidGetThenInvalidGetOption[S], initialStateData(dataSource))

      // We fail because of idInfinity, and never get to idInfinitySquared
      assert(error.errorGuid == idInfinity.guid)
      // Note that the failed attempt to get the invalid Id still means the Id was viewed,
      // but the later attempt to read id 2 and idInfinitySquared never run
      assert(error.viewedGuids == Set(id0, id1, idInfinity).map(_.guid))
      assert(error.missingGuids == Set(idInfinity.guid))
    }
  }
}
