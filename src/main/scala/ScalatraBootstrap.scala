import com.ubirch.viz.rest.{ ApiRest, ApiSwagger, ResourcesApp }
import com.ubirch.viz.Service
import javax.servlet.ServletContext
import org.scalatra.LifeCycle

class ScalatraBootstrap extends LifeCycle {

  implicit val swagger: ApiSwagger = new ApiSwagger

  lazy val restApi: ApiRest = Service.get[ApiRest]
  lazy val resourceApp: ResourcesApp = Service.get[ResourcesApp]

  override def init(context: ServletContext): Unit = {
    context.initParameters("org.scalatra.cors.preflightMaxAge") = "5"
    context.initParameters("org.scalatra.cors.allowCredentials") = "false"

    context.mount(restApi, "/", "RestApi")
    context.mount(resourceApp, "/api-docs")
  }
}

