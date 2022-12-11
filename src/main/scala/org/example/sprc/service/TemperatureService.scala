package org.example.sprc.service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.example.sprc.model.{EntryNotFoundError, Temperature}
import org.example.sprc.repository.TemperatureDao

class TemperatureService(dao: TemperatureDao) {

  def getTemperature(requirements: Map[String, String]): Array[Temperature] =
    dao.getTemperatures(requirements).compile.toVector.unsafeRunSync().toArray

  //  def deleteCountry(id: Int): IO[Either[EntryNotFoundError.type, Unit]] =
  //    dao.deleteCountry(id)
  //
  def addTemperature(temperature: Temperature): IO[Temperature] =
    dao.createTemperature(temperature)

  //  def updateCountry(
  //      id: Int,
  //      country: Country
  //  ): IO[Either[EntryNotFoundError.type, Country]] = dao.updateCountry(id, country)

}
