package controllers

import daos.{LoginDAO, TasklistDAO}
import model.{GetDelete, Login, LoginFunc, Tasklist}
import play.api.data.Form
import play.api.data.Forms.{mapping, number, text}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc._
import slick.jdbc.JdbcProfile

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeController @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(tasklistDao: TasklistDAO, loginDao: LoginDAO, cc: ControllerComponents)
                               (implicit executionContext: ExecutionContext) extends AbstractController(cc)
                                with HasDatabaseConfigProvider[JdbcProfile] {

  def index(): Action[AnyContent] = Action.async { implicit request =>
    val usernameS: String = request.session.get("username").getOrElse("Unauthorized")
    val matches = (usernameS != "Unauthorized")
        if (matches) tasklistDao.allOfUser(usernameS).map{ case (tasks) => Ok(views.html.index(tasks, usernameS)) }
        else Future.successful(Redirect(routes.HomeController.login()).withNewSession)
    }

  def login() = Action { implicit request =>
    Ok(views.html.login(request.session.get("errorL").getOrElse("")))
  }

  def register() = Action { implicit request =>
    Ok(views.html.register(request.session.get("errorR").getOrElse("")))
  }

  def logout() = Action { implicit request =>
    Redirect(routes.HomeController.login()).withNewSession
  }

  def env() = Action { implicit request: Request[AnyContent] =>
    Ok("Nothing to see here")
    //Ok(System.getenv("JDBC_DATABASE_URL"))
  }

  val loginForm = Form(
    mapping(
      "username" -> text(),
      "password" -> text())(Login.apply)(Login.unapply))

  private val model = new LoginFunc(loginDao, db)

  def validateLogin = Action.async { implicit request =>
    val login: Login = loginForm.bindFromRequest().get
    val matches = model.validateLogin(login.username, login.password)
    matches.map { usersName =>
      usersName match {
        case Some(login.username) =>
          Redirect(routes.HomeController.index()).withSession("username" -> login.username)
        case None =>
          Redirect(routes.HomeController.login()).withSession("errorL" -> "User/Passwort Kombination existiert nicht.")
      }
    }
  }

  val createForm = Form(
    mapping(
      "username2" -> text(),
      "password2" -> text())(Login.apply)(Login.unapply))

  def insertLogin = Action.async { implicit request =>
    val createLogin: Login = createForm.bindFromRequest().get
    val matches = model.validateCreation(createLogin.username)

    matches.map { usersName =>
      usersName match {
        case Some(createLogin.username) =>
          Redirect(routes.HomeController.register()).withSession("errorR" -> "User existiert bereits.")
        case None =>
          loginDao.insert(createLogin)
          Redirect(routes.HomeController.index).withSession("username" -> createLogin.username)
      }
    }
  }

  val taskForm = Form(
    mapping(
      "idAuto" -> number(),
      "title" -> text(),
      "body" -> text(),
      "username" -> text())(Tasklist.apply)(Tasklist.unapply)
  )

  def insertTask = Action.async { implicit request =>
    val task: Tasklist = taskForm.bindFromRequest.get
    tasklistDao.insert(task).map(_ => Redirect(routes.HomeController.index))
  }

  val readDelete = Form(
    mapping(
      "numberToDelete" -> number())(GetDelete.apply)(GetDelete.unapply))

  def deleteTask() = Action.async { implicit request =>
    val num: GetDelete = readDelete.bindFromRequest.get
    tasklistDao.remove(num.numberToDelete).map(_ => Redirect(routes.HomeController.index))
  }
}
