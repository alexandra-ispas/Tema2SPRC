package org.example.sprc.repository

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION
import org.example.sprc.model.{City, EntryNotFoundError}

class CityDao(transactor: Transactor[IO]) {

  def getCities: fs2.Stream[IO, City] =
      sql"SELECT * FROM orase"
        .query[City]
        .stream
        .transact(transactor)

  def getCity(id: Int): IO[Either[EntryNotFoundError.type, City]] = {
    sql"SELECT id, idTara, nume, lat, lon FROM orase WHERE id = $id"
      .query[City]
      .option
      .transact(transactor)
      .map {
        case Some(city) => Right(city)
        case None       => Left(EntryNotFoundError)
      }
  }

  def getCityByCountryId(idTara: Int): fs2.Stream[IO, City] = {
    sql"SELECT id, idTara, nume, lat, lon FROM orase WHERE idTara = $idTara"
      .query[City]
      .stream
      .transact(transactor)
  }

  def createCity(city: City): IO[Either[SqlState, City]] = {
    sql"INSERT INTO orase (idTara, nume, lat, lon) VALUES (${city.idTara}, ${city.nume}, ${city.lat},  ${city.lon})"
      .update
      .withUniqueGeneratedKeys[Int]("id")
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) if exception.getSQLState == UNIQUE_VIOLATION.value => Left(UNIQUE_VIOLATION)
        case Left(exception)  => println(s"$exception ${exception.getSQLState}");Left(UNIQUE_VIOLATION)
        case Right(id) => Right(city.copy(id = Some(id)))
      }
  }

  def deleteCity(id: Int): IO[Either[EntryNotFoundError.type, Unit]] = {
    sql"DELETE FROM orase WHERE id = $id".update.run.transact(transactor).map {
      affectedRowsNr =>
        if (affectedRowsNr == 1) Right(())
        else Left(EntryNotFoundError)
    }
  }

  def updateCity(
      id: Int,
      city: City
  ): IO[Either[EntryNotFoundError.type, City]] = {
    sql"UPDATE orase SET idTara = ${city.idTara} nume = ${city.nume}, lat = ${city.lat}, lon = ${city.lon} where id=$id".update.run
      .transact(transactor)
      .map { affectedRowsNr =>
        if (affectedRowsNr == 1)
          Right(city.copy(id = Option(id)))
        else Left(EntryNotFoundError)
      }
  }

}
