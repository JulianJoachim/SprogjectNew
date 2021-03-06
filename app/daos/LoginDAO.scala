package daos

import model.Login
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LoginDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                           (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Logins = TableQuery[LoginTable]

  def all(): Future[Seq[Login]] = db.run(Logins.result)

  def insert(login: Login): Future[Unit] = {
    db.run(Logins += login).map { _ => () }
  }

  def search(username: String): Future[Unit] = {
    db.run(Logins.filter(_.username === username).result).map {username => username}
  }

  protected class LoginTable(tag: Tag) extends Table[Login](tag, "LOGIN") {
    def username = column[String]("USERNAME", O.PrimaryKey)
    def password = column[String]("PASSWORD")

    def * = (username, password) <> (Login.tupled, Login.unapply)
  }

  lazy val Users = new TableQuery(tag => new LoginTable(tag))
}