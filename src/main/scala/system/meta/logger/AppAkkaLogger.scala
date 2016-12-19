package system.meta.logger

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import system.meta.config.NamingConfig


object AppAkkaLogger extends NamingConfig {
  def apply(loggerName: String = appName)(implicit actorSystem: ActorSystem): LoggingAdapter = {
    val eventStream = actorSystem.eventStream
    Logging(eventStream, loggerName)
  }
}
