package system.crawler.host.utils

import java.net.URL

import scala.collection.immutable.ListMap


class UrlTransformer {
  def toAbsolutePath(basePath: String, relativePath: String): String = {
    new URL(new URL(basePath), relativePath).toString
  }

  def prepareBasePath(path: String): String = {
    val uri = new URL(new URL(path), "./").toURI
    val normal = uri.normalize()
    normal.toASCIIString
  }
}