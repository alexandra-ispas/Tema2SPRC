package org.example.sprc.service

import cats.effect.unsafe.implicits.global
import doobie.enumerated.SqlState
import doobie.postgres.sqlstate.class22.INVALID_PARAMETER_VALUE
import org.example.sprc.model.{Temperature, TemperatureResponse}
import org.example.sprc.repository.TemperatureDao

class TemperatureService(dao: TemperatureDao) {

  def getTemperature(requirements: Map[String, String]): Array[TemperatureResponse] =
    dao.getTemperatures(requirements)
      .compile
      .toVector
      .unsafeRunSync().toArray
      .map(x => TemperatureResponse(x.id, x.valoare, x.timestamp))

  def deleteTemperature(id: Int): Either[SqlState, Int] =
    dao.deleteTemperature(id)

  def getCityTemperature(id: Int, requirements: Map[String, String]): Array[TemperatureResponse] =
    dao.getCityTemperature(id, requirements)
      .compile
      .toVector
      .unsafeRunSync().toArray
      .map(x => TemperatureResponse(x.id, x.valoare, x.timestamp))

  def getCountryTemperature(id: Int, requirements: Map[String, String]):Array[TemperatureResponse] =
    dao.getCountryTemperature(id, requirements)
      .compile.toVector
      .unsafeRunSync().toArray
      .map(x => TemperatureResponse(x.id, x.valoare, x.timestamp))

  def addTemperature(temperature: Temperature): Either[SqlState, Temperature] = {
    if (isRequestValid(temperature)) {
      dao.createTemperature(temperature)
    } else  Left(INVALID_PARAMETER_VALUE)
  }

  def updateTemperature(
      id: Int,
      temperature: Temperature
  ): Either[SqlState, Temperature] = {
    if (isRequestValid(temperature))
      dao.updateCountry(id, temperature)
    else
      Left(INVALID_PARAMETER_VALUE)
  }

  private def isRequestValid(temperature: Temperature) =
    !(temperature.valoare.isEmpty || temperature.idOras.isEmpty)

}
