package model

import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import daos.LoginDAO

class LoginFunc(loginDAO: LoginDAO, db: Database)(implicit ec: ExecutionContext) {

  def validateLogin(username: String, password: String): Future[Option[String]] = {
    val matches = db.run(loginDAO.Users.filter(userRow => userRow.username === username).result)
    matches.map(userRows => userRows.headOption.flatMap {
      userRow => if (password == userRow.password) Some(userRow.username) else None
    })
  }

  def validateCreation (username: String): Future[Option[String]] = {
    val matches = db.run(loginDAO.Users.filter(userRow => userRow.username === username).result)
    matches.map(userRows => userRows.headOption.map {
      userRow => userRow.username
    })
  }



}
