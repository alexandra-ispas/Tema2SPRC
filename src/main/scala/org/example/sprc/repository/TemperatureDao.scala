package org.example.sprc.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie._
import doobie.implicits._
import org.example.sprc.model.Temperature

import scala.collection.mutable
import doobie.implicits.javasql._
import doobie.implicits.javatime._

class TemperatureDao(transactor: Transactor[IO], cityDao: CityDao) {

  def getTemperatures(requirements: Map[String, String]): fs2.Stream[IO, Temperature] = {
    var query: mutable.StringBuilder = new mutable.StringBuilder("SELECT * FROM temperaturi where")

    if (requirements.contains("from")) {
      val startTimestamp = requirements("from")
      query.append(s" timestamp::date > \'$startTimestamp\'::date and")
    }

    if (requirements.contains("until")) {
      val endTimestamp = requirements("until")
      query.append(s" timestamp::date < \'$endTimestamp\'::date and")
    }

    if (requirements.contains("lat")) {
      val lat = requirements("lat").toDouble
      val cityIds = cityDao.getCityIdsByLat(lat)
      if(cityIds.nonEmpty)
        query = query.append(" idOras in (" + cityIds.mkString(",") + ") and")
    }

    if (requirements.contains("lon")) {
      val lon = requirements("lon").toDouble
      val cityIds = cityDao.getCityIdsByLon(lon)
      if(cityIds.nonEmpty)
        query = query.append(" idOras in (" + cityIds.mkString(",") + ") and")
    }

    query = query.dropRight(4).filter(_ != '\n')

    Fragment.const(query.mkString(""))
      .query[Temperature]
      .stream
      .transact(transactor)
  }

  def getCityTemperature(cityId: Int, requirements: Map[String, String]): fs2.Stream[IO, Temperature] = {
    var query: mutable.StringBuilder = new mutable.StringBuilder(s"SELECT * FROM temperaturi where idOras = $cityId and")

    if(requirements.contains("from"))
      query = query.append(s" timestamp::date > \'${requirements("from")}\'::date and")

    if(requirements.contains("until"))
      query.append(s" timestamp::date < \'${requirements("until")}\'::date and")

    query = query.dropRight(4).filter(_ != '\n')

    Fragment.const(query.mkString(""))
      .query[Temperature]
      .stream
      .transact(transactor)
  }

  def getCountryTemperature(countryId: Int, requirements: Map[String, String]): fs2.Stream[IO, Temperature] = {
    val cityIds: Array[Int] = cityDao
      .getCityByCountryId(countryId)
      .compile.toVector
      .unsafeRunSync().toArray
      .map(_.id.get)

    var query: mutable.StringBuilder = new mutable.StringBuilder(s"SELECT * FROM temperaturi where idOras in (${cityIds.mkString(",")}) and")

//    if(requirements.contains("from"))
//      query = query.append(s" timestamp::date > \'${requirements("from")}\'::date and")
//
//    if(requirements.contains("until"))
//      query.append(s" timestamp::date < \'${requirements("until")}\'::date and")
//
//    query = query.dropRight(4).filter(_ != '\n')

    Fragment.const(preprocessQuery(requirements, query).mkString(""))
      .query[Temperature]
      .stream
      .transact(transactor)
  }

  def createTemperature(temperature: Temperature): Either[SqlState, Temperature] =
    sql"INSERT INTO temperaturi (valoare, idOras) VALUES (${temperature.valoare},  ${temperature.idOras})"
      .update
      .withUniqueGeneratedKeys[Int]("id")
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) => Left(SqlState(exception.getSQLState))
        case Right(id) => Right(temperature.copy(id = Some(id)))
      }.unsafeRunSync()

  def deleteTemperature(id: Int): Either[SqlState, Int] =
    sql"DELETE FROM temperaturi WHERE id = $id".update.run
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) => Left(SqlState(exception.getSQLState))
        case Right(x) => Right(x)
      }.unsafeRunSync()

  def updateCountry(
      id: Int,
      temperature: Temperature
  ): Either[SqlState, Temperature] =
    sql"UPDATE temperaturi SET valoare = ${temperature.valoare.get} where id=$id"
      .update.run
      .transact(transactor)
      .attemptSql
      .map {
        case Left(exception) => Left(SqlState(exception.getSQLState))
        case Right(id) => Right(temperature.copy(id = Some(id)))
      }.unsafeRunSync()

  private def preprocessQuery(requirements: Map[String, String], query: mutable.StringBuilder): mutable.StringBuilder = {
    var result = query
    if(requirements.contains("from"))
      result = result.append(s" timestamp::date > \'${requirements("from")}\'::date and")

    if(requirements.contains("until"))
      query.append(s" timestamp::date < \'${requirements("until")}\'::date and")

    result.dropRight(4).filter(_ != '\n')
  }
}
