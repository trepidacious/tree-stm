package org.rebeam.tree

//import org.rebeam.tree.ExampleData._
//import org.rebeam.tree.MapStateSTM._
//import org.rebeam.tree.Delta._
//import org.rebeam.tree.Guid._
//import org.rebeam.tree.Transaction.DeltaAtId
import org.scalatest._
import org.scalatest.prop.Checkers

class ExampleDataSpec extends WordSpec with Matchers with Checkers {
//
//  private val taskListResult = createTaskList[S].run(emptyState).value
//
//  private val taskListGuid = Guid(SessionId(0), SessionTransactionId(0), TransactionClock(2))
//  private val task1Guid = Guid(SessionId(0), SessionTransactionId(0), TransactionClock(0))
//  private val task2Guid = Guid(SessionId(0), SessionTransactionId(0), TransactionClock(1))
//
//  private val taskListId = Id[TaskList](taskListGuid)
//  private val task1Id = Id[Task](task1Guid)
//  private val task2Id = Id[Task](task2Guid)
//
//  "ExampleData" should {
//
//    "create expected data" in {
//      val (s1, taskList) = taskListResult
//
//      assert(taskList.toString == "TaskList(id-0-0-2,Task List,List(Task(id-0-0-0,task 1,false), Task(id-0-0-1,task 2,true)))")
//      assert(s1.nextGuid == Guid(SessionId(0), SessionTransactionId(0), TransactionClock(3)))
//      assert(s1.map == Map(
//        taskListGuid -> taskList,
//        task1Guid -> taskList.tasks(0),
//        task2Guid -> taskList.tasks(1)
//      ))
//
//    }
//
//    "use cursor to task list name" in {
//
//      val (s1, taskList) = taskListResult
//
//      // Cursor at the name of task list
//      val taskListName = DeltaCursor
//        .AtId(taskList.id)
//        .zoom(TaskList.name)
//
//      val newName = "The list of tasks"
//
//      val t1 = taskListName.set(newName)
//
//      // We should have a transaction with expected delta at expected id
//      assert (t1 == DeltaAtId(taskListId, LensDelta(TaskList.name, ValueDelta(newName))))
//
//      val (s2, _) = t1[S].run(s1).value
//
//      assert(s2.get(taskListId).map(_.name).contains(newName))
//
//    }

//    "use cursor to first task's name" in {
//
//      val (s1, taskList) = taskListResult
//
//      // Cursor at the name of the first task
//      val firstTaskName = DeltaCursor
//        .AtId(taskList.id)
//        .zoom(TaskList.tasks)
//        .zoomIndex(0)
//        .zoom(Task.name)
//
//      val newName = "The First Task"
//
//      val t1 = firstTaskName.set(newName)
//
//      // We should have a transaction with expected delta at expected id
//      assert (t1 ==
//        DeltaAtId(
//          taskListId,
//          LensDelta(
//            TaskList.tasks,
//            TraversableIndexDelta[List, Task](
//              0,
//              LensDelta(
//                Task.name,
//                ValueDelta(newName)
//              )
//            )
//          )
//        )
//      )
//
//      val (s2, _) = t1[S].run(s1).value
//
//      assert(s2.get(task1Id).map(_.name).contains(newName))
//    }
//  }

}
