package org.example.sprc.repository

import cats.effect._
import cats.effect.unsafe.implicits.global
import doobie._
import doobie.implicits._
import doobie.postgres.sqlstate.class23.FOREIGN_KEY_VIOLATION
import org.example.sprc.model.Country

class CountryDao(transactor: Transactor[IO]) {

  def getCountries: fs2.Stream[IO, Country] =
    sql"SELECT * FROM tari"
      .query[Country]
      .stream
      .transact(transactor)

  def getCountry(id: Int): Either[SqlState, Country] =
    sql"SELECT id, nume, lat, lon FROM tari WHERE id = $id"
      .query[Country]
      .option
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) => Left(SqlState(exception.getSQLState))
        case Right(country) => Right(country.get)
      }.unsafeRunSync()

  def createCountry(country: Country): Either[SqlState, Country] = {
    sql"INSERT INTO tari (nume, lat, lon) VALUES (${country.nume}, ${country.lat},  ${country.lon})"
      .update
      .withUniqueGeneratedKeys[Int]("id")
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) => Left(SqlState(exception.getSQLState))
        case Right(id) => Right(country.copy(id = Some(id)))
      }.unsafeRunSync()
  }

  def deleteCountry(id: Int): Either[SqlState, Int] = {
    sql"DELETE FROM tari WHERE id = $id".update.run
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) => Left(SqlState(exception.getSQLState))
        case Right(x) => Right(x)
      }.unsafeRunSync()
  }

  def updateCountry(
      id: Int,
      country: Country
  ): Either[SqlState, Country] = {
    sql"UPDATE tari SET nume = ${country.nume}, lat = ${country.lat}, lon = ${country.lon} where id=$id".update.run
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) => Left(SqlState(exception.getSQLState))
        case Right(id) if id > 0 => Right(country.copy(id = Some(id)))
        case Right(_) => Left(FOREIGN_KEY_VIOLATION)
      }.unsafeRunSync()
  }

}
