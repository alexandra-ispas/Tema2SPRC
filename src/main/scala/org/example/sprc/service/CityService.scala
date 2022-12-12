package org.example.sprc.service

import cats.effect.unsafe.implicits.global
import doobie.enumerated.SqlState
import doobie.postgres.sqlstate.class22.INVALID_PARAMETER_VALUE
import org.example.sprc.model.City
import org.example.sprc.repository.CityDao

class CityService(dao: CityDao) {
  def getCities: Array[City] =
    dao.getCities.compile.toVector.unsafeRunSync().toArray

  def getCity(id: Int): Either[SqlState, City] =
    dao.getCity(id)

  def getCitiesByCountry(idTara: Int): Array[City] =
    dao.getCityByCountryId(idTara).compile.toVector.unsafeRunSync().toArray

  def deleteCity(id: Int): Either[SqlState, Int] =
    dao.deleteCity(id)

  def addCity(city: City): Either[SqlState, City] =
    if (isRequestValid(city))
      dao.createCity(city)
    else Left(INVALID_PARAMETER_VALUE)

  def updateCity(
      id: Int,
      city: City
  ): Either[SqlState, City] =
    if (isRequestValid(city))
      dao.updateCity(id, city)
    else Left(INVALID_PARAMETER_VALUE)

  private def isRequestValid(city: City) =
    !(city.idTara.isEmpty || city.nume.isEmpty || city.lat.isEmpty || city.lon.isEmpty)
}
