package daos

import model.Tasklist
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TasklistDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                           (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Tasklists = TableQuery[TasklistTable]

  def all(): Future[Seq[Tasklist]] = db.run(Tasklists.result)

  def allOfUser(username: String): Future[Seq[Tasklist]] = db.run(Tasklists.filter(_.username === username).result)

  def insert(task: Tasklist): Future[Unit] = {
    db.run(Tasklists += task).map { _ => () }
  }

  def remove(itemId: Int): Future[Boolean] = {
    db.run(Tasklists.filter(_.id === itemId).delete).map(count => count > 0)
  }

  private class TasklistTable(tag: Tag) extends Table[Tasklist](tag, "TASKLIST") {

    def id = column[Int]("ID", SqlType("SERIAL"), O.AutoInc, O.PrimaryKey)
    def title = column[String]("TITLE")
    def body = column[String]("BODY")
    def username = column[String]("USERNAME")

    def * = (id, title, body, username) <> (Tasklist.tupled, Tasklist.unapply)
  }
}