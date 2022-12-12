package org.example.sprc.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie._
import doobie.implicits._
import doobie.postgres.sqlstate.class23.FOREIGN_KEY_VIOLATION
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

  def getCityIdsByLat(lat: Double): Array[Int] = {
    sql"SELECT id from orase where lat = $lat"
      .query[Int]
      .stream
      .transact(transactor)
      .compile.toVector
      .unsafeRunSync().toArray
  }
  def getCityIdsByLon(lon: Double): Array[Int] = {
    sql"SELECT id from orase where lon = $lon"
      .query[Int]
      .stream
      .transact(transactor)
      .compile.toVector
      .unsafeRunSync().toArray
  }

  def getCityByCountryId(countryId: Int): fs2.Stream[IO, City] = {
    sql"SELECT * FROM orase WHERE idTara = $countryId"
      .query[City]
      .stream
      .transact(transactor)
  }

  def createCity(city: City): Either[SqlState, City] = {
    sql"INSERT INTO orase (idTara, nume, lat, lon) VALUES (${city.idTara}, ${city.nume}, ${city.lat},  ${city.lon})"
      .update
      .withUniqueGeneratedKeys[Int]("id")
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) => Left(SqlState(exception.getSQLState))
        case Right(id) => Right(city.copy(id = Some(id)))
      }.unsafeRunSync()
  }

  def deleteCity(id: Int): IO[Either[EntryNotFoundError.type, Unit]] = {
    sql"DELETE FROM orase WHERE id = $id"
      .update.run
      .transact(transactor).map {
      affectedRowsNr =>
        if (affectedRowsNr == 1) Right(())
        else Left(EntryNotFoundError)
    }
  }

  def updateCity(
      id: Int,
      city: City
  ): Either[SqlState, City] = {
    sql"UPDATE orase SET id = ${city.id}, idTara = ${city.idTara}, nume = ${city.nume}, lat = ${city.lat}, lon = ${city.lon} where id=$id"
      .update.run
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) => Left(SqlState(exception.getSQLState))
        case Right(id) if id > 0 => Right(city.copy(id = Some(id)))
        case Right(_) => Left(FOREIGN_KEY_VIOLATION)
      }.unsafeRunSync()
  }

}
