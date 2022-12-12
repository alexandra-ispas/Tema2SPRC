package org.example.sprc.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie._
import doobie.implicits._
import org.example.sprc.model.City

class CityDao(transactor: Transactor[IO]) {

  def getCities: fs2.Stream[IO, City] =
      sql"SELECT * FROM orase"
        .query[City]
        .stream
        .transact(transactor)

  def getCity(id: Int): Either[SqlState, City] =
    sql"SELECT id, idTara, nume, lat, lon FROM orase WHERE id = $id"
      .query[City]
      .option
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) => Left(SqlState(exception.getSQLState))
        case Right(city) => Right(city.get)
      }.unsafeRunSync()

  def getCityIdsByLat(lat: Double): Array[Int] =
    sql"SELECT id from orase where lat = $lat"
      .query[Int]
      .stream
      .transact(transactor)
      .compile.toVector
      .unsafeRunSync().toArray
  def getCityIdsByLon(lon: Double): Array[Int] =
    sql"SELECT id from orase where lon = $lon"
      .query[Int]
      .stream
      .transact(transactor)
      .compile.toVector
      .unsafeRunSync().toArray

  def getCityByCountryId(countryId: Int): fs2.Stream[IO, City] =
    sql"SELECT * FROM orase WHERE idTara = $countryId"
      .query[City]
      .stream
      .transact(transactor)

  def createCity(city: City): Either[SqlState, City] =
    sql"INSERT INTO orase (idTara, nume, lat, lon) VALUES (${city.idTara}, ${city.nume}, ${city.lat},  ${city.lon})"
      .update
      .withUniqueGeneratedKeys[Int]("id")
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) => Left(SqlState(exception.getSQLState))
        case Right(id) => Right(city.copy(id = Some(id)))
      }.unsafeRunSync()

  def deleteCity(id: Int): Either[SqlState, Int] =
    sql"DELETE FROM orase WHERE id = $id"
      .update.run
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) => Left(SqlState(exception.getSQLState))
        case Right(x) => Right(x)
      }.unsafeRunSync()

  def updateCity(
      id: Int,
      city: City
  ): Either[SqlState, City] =
    sql"UPDATE orase SET id = ${city.id}, idTara = ${city.idTara}, nume = ${city.nume}, lat = ${city.lat}, lon = ${city.lon} where id=$id"
      .update.run
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) => Left(SqlState(exception.getSQLState))
        case Right(id) => Right(city.copy(id = Some(id)))
      }.unsafeRunSync()

}
