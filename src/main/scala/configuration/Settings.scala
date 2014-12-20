package configuration

import com.typesafe.config.Config

abstract class Settings[T](prefix: String) {

  def apply(config: Config): T =
    fromSubConfig(config getConfig prefix)

  def fromSubConfig(c: Config): T
}
