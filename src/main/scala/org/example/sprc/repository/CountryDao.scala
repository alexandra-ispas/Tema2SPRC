package org.example.sprc.repository

import cats.effect._
import doobie._
import doobie.implicits._
import doobie.postgres.sqlstate
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION
import org.example.sprc.model.{Country, EntryNotFoundError, UniqueKeyAlreadyExists}
import org.postgresql.util.PSQLException

class CountryDao(transactor: Transactor[IO]) {

  def getCountries: fs2.Stream[IO, Country] =
    sql"SELECT * FROM tari"
      .query[Country]
      .stream
      .transact(transactor)

  def getCountry(id: Int): IO[Either[EntryNotFoundError.type, Country]] = {
    sql"SELECT id, nume, lat, lon FROM tari WHERE id = $id"
      .query[Country]
      .option
      .transact(transactor)
      .map {
        case Some(todo) => Right(todo)
        case None       => Left(EntryNotFoundError)
      }
  }

  def createCountry(country: Country): IO[Either[SqlState, Country]] = {
    sql"INSERT INTO tari (nume, lat, lon) VALUES (${country.nume}, ${country.lat},  ${country.lon})"
      .update
      .withUniqueGeneratedKeys[Int]("id")
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) => Left(SqlState(exception.getSQLState))
        case Right(id) => Right(country.copy(id = Some(id)))
      }
  }

  def deleteCountry(id: Int): IO[Either[EntryNotFoundError.type, Unit]] = {
    sql"DELETE FROM tari WHERE id = $id".update.run.transact(transactor).map {
      affectedRowsNr =>
        if (affectedRowsNr == 1) Right(())
        else Left(EntryNotFoundError)
    }
  }

  def updateCountry(
      id: Int,
      country: Country
  ): IO[Either[EntryNotFoundError.type, Country]] = {
    sql"UPDATE tari SET nume = ${country.nume}, lat = ${country.lat}, lon = ${country.lon} where id=$id".update.run
      .transact(transactor)
      .map { affectedRowsNr =>
        if (affectedRowsNr == 1)
          Right(country.copy(id = Option(id)))
        else Left(EntryNotFoundError)
      }
  }

}
