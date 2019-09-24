import com.ubirch.viz.server.rest.{ApiRest, ApiSwagger, ResourcesApp}
import javax.servlet.ServletContext
import org.scalatra.LifeCycle

class ScalatraBootstrap extends LifeCycle {

  implicit val swagger: ApiSwagger = new ApiSwagger

  override def init(context: ServletContext) {

    context.mount(new ApiRest, "/", "RestApi")
    context.mount(new ResourcesApp, "/api-docs")
  }
}

