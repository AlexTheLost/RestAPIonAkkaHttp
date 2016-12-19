package system.meta.config

import com.typesafe.config.ConfigFactory


trait ConfigLoader {
  val config = ConfigFactory.load()
}

trait HttpConfig extends ConfigLoader {
  val host = config.getString("http.host")
  val port = config.getInt("http.port")
}

trait NamingConfig extends ConfigLoader {
  val appName = config.getString("naming.appName")
}