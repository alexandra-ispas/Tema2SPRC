package org.example.sprc.db

import scala.concurrent.ExecutionContext

import cats.effect.{IO, Resource}
import doobie.hikari.HikariTransactor
import org.example.sprc.config.DatabaseConfig

object Database {

  def transactor(
      dbConfig: DatabaseConfig,
      ec: ExecutionContext
  ): Resource[IO, HikariTransactor[IO]] =
    HikariTransactor.newHikariTransactor[IO](
      driverClassName = dbConfig.driver,
      url = dbConfig.url,
      user = dbConfig.user,
      pass = dbConfig.password,
      connectEC = ec
    )
}
