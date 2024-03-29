package org.example.sprc.config

import cats.effect.{IO, Resource}
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

final case class ServerConfig(host: String, port: Int)

final case class DatabaseConfig(
    driver: String,
    url: String,
    user: String,
    password: String,
    threadPoolSize: Int
)

final case class MetricsConfig(prefixName: String)

final case class Config(
    server: ServerConfig,
    database: DatabaseConfig,
    metrics: MetricsConfig
)

object AppConfig {

  def load(configFile: String = "application.conf"): Resource[IO, Config] =
    Resource.eval(
      ConfigSource
        .fromConfig(ConfigFactory.load(configFile))
        .loadF[IO, Config]()
    )
}
