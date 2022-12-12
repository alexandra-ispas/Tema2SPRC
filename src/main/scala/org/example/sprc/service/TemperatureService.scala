package org.example.sprc.service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.enumerated.SqlState
import doobie.postgres.sqlstate.class23.FOREIGN_KEY_VIOLATION
import org.example.sprc.model.{EntryNotFoundError, Temperature}
import org.example.sprc.repository.TemperatureDao

class TemperatureService(dao: TemperatureDao) {

  def getTemperature(requirements: Map[String, String]): Array[Temperature] =
    dao.getTemperatures(requirements)
      .compile
      .toVector
      .unsafeRunSync().toArray

  def deleteTemperature(id: Int): IO[Either[EntryNotFoundError.type, Unit]] =
    dao.deleteTemperature(id)

  def getCityTemperature(id: Int, requirements: Map[String, String]): Array[Temperature] =
    dao.getCityTemperature(id, requirements)
      .compile
      .toVector
      .unsafeRunSync().toArray

  def getCountryTemperature(id: Int, requirements: Map[String, String]):Array[Temperature] =
    dao.getCountryTemperature(id, requirements)
      .compile.toVector
      .unsafeRunSync().toArray

  def addTemperature(temperature: Temperature): Either[SqlState, Temperature] = {
    if (temperature.valoare.isEmpty || temperature.idoras.isEmpty)
      Left(FOREIGN_KEY_VIOLATION)
    else dao.createTemperature(temperature)
  }

  def updateTemperature(
      id: Int,
      temperature: Temperature
  ): IO[Either[EntryNotFoundError.type, Temperature]] = dao.updateCountry(id, temperature)

}
