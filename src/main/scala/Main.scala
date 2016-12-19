
import akka.actor.ActorSystem
import system.handler.api.http.RestApi

object Main extends App {
  implicit val actorSystem = ActorSystem("Main")
  // Start Rest-API:
  RestApi()
}


