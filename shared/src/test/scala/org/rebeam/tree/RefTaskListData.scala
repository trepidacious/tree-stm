package org.rebeam.tree

import cats.Monad
import cats.implicits._
import io.circe.generic.JsonCodec
import monocle.macros.Lenses
import org.rebeam.tree.codec._
import org.rebeam.tree.codec.Codec._
import Syntax._

/**
  * Data example using a TaskList containing [[Ref]]s to Tasks with their
  * own [[Id]]s.
  */
object RefTaskListData {

  @JsonCodec
  @Lenses
  case class Task(id: Id[Task], name: String, done: Boolean) {
    def prettyPrint: String = s"[${if (done) "X" else " "}] $name"
  }

  @JsonCodec
  @Lenses
  case class TaskList(id: Id[TaskList], name: String, tasks: List[Ref[Task]])

  implicit val taskDeltaCodec: DeltaCodec[Task] = lens("name", Task.name) or lens("done", Task.done)

  // Can edit any list of Ref[Task] using a new value - note we would be editing the list contents, not the
  // individual Tasks, which are edited via their id in the STM
  // Note: A better approach would be to provide individual editing operations on the list, e.g. add, remove etc.,
  // or to provide these via actions on the TaskList
  implicit val tasksDeltaCodec: DeltaCodec[List[Ref[Task]]] = value[List[Ref[Task]]]

  implicit val taskListDeltaCodec: DeltaCodec[TaskList] = lens("name", TaskList.name) or lens("tasks", TaskList.tasks)

  // Allows Tasks and TaskLists to be put in STM
  implicit val taskIdCodec: IdCodec[Task] = IdCodec[Task]("Task")

  implicit val taskListIdCodec: IdCodec[TaskList] = IdCodec[TaskList]("TaskList")

  def createTask[F[_]: Monad](i: Int)(implicit stm: STMOps[F]): F[Ref[Task]] =
    stm.put[Task](Task(_, s"Task $i", done = i % 2 == 0)).map(task => Ref(task.id))

  def createTaskList[F[_]: Monad](implicit stm: STMOps[F]): F[TaskList] = {
    import stm._
    for {
      tasks <- 1.to(10).toList.traverse(createTask[F])
      taskList <- put[TaskList](TaskList(_, "Task List", tasks))
    } yield taskList
  }

  def printTaskList[F[_]: Monad](l: TaskList)(implicit stm: STMOps[F]): F[String] = {
    for {
      tasks <- l.tasks.deref[F]
    } yield s"${l.name}: ${tasks.map(_.prettyPrint).mkString(", ")}"
  }

}
