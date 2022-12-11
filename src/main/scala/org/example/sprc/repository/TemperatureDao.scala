package org.example.sprc.repository

import cats.effect.IO
import doobie._
import doobie.implicits._
import org.example.sprc.model.Temperature

import java.sql.Timestamp
import java.time.Instant

class TemperatureDao(transactor: Transactor[IO]) {
  def getTemperatures(requirements: Map[String, String]): fs2.Stream[IO, Temperature] = {

    val lat = requirements.get("lat")
    val lon = requirements.get("lon")
    val from = requirements.get("from")
    val until = requirements.get("until")

    val requestHelper = requirements
      .map((x: (String, String)) => s"${x._1} == ${x._2}")
      .fold("")((x, y) => y ++ "," ++ x)
      .dropRight(1)

    println(requestHelper)

    val x: Fragment = sql"SELECT * FROM temperaturi where ${requestHelper}"
      x
      .query[Temperature]
      .stream
      .transact(transactor)
  }



  //  def getCountry(id: Int): IO[Either[EntryNotFoundError.type, Country]] = {
  //    sql"SELECT id, nume, lat, lon FROM tari WHERE id = $id"
  //      .query[Country]
  //      .option
  //      .transact(transactor)
  //      .map {
  //        case Some(todo) => Right(todo)
  //        case None       => Left(EntryNotFoundError)
  //      }
  //  }
  //

  def createTemperature(temperature: Temperature): IO[Temperature] = {
    sql"INSERT INTO temperaturi (valoare, timestamp, idOras) VALUES (${temperature.valoare}, ${Timestamp.from(Instant.now()).toString},  ${temperature.idOras})"
      .update
      .withUniqueGeneratedKeys[Int]("id")
      .transact(transactor)
      .map { id => temperature.copy(id = Some(id)) }
  }

  //  def deleteCountry(id: Int): IO[Either[EntryNotFoundError.type, Unit]] = {
  //    sql"DELETE FROM tari WHERE id = $id".update.run.transact(transactor).map {
  //      affectedRowsNr =>
  //        if (affectedRowsNr == 1) Right(())
  //        else Left(EntryNotFoundError)
  //    }
  //  }
  //
  //  def updateCountry(
  //      id: Int,
  //      country: Country
  //  ): IO[Either[EntryNotFoundError.type, Country]] = {
  //    sql"UPDATE tari SET nume = ${country.nume}, lat = ${country.lat}, lon = ${country.lon} where id=$id".update.run
  //      .transact(transactor)
  //      .map { affectedRowsNr =>
  //        if (affectedRowsNr == 1)
  //          Right(country.copy(id = Option(id)))
  //        else Left(EntryNotFoundError)
  //      }
  //  }

}
