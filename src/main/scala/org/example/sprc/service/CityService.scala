package org.example.sprc.service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.enumerated.SqlState
import org.example.sprc.model.{City, EntryNotFoundError}
import org.example.sprc.repository.CityDao

class CityService(dao: CityDao) {
  def getCities: Array[City] =
      dao.getCities.compile.toVector.unsafeRunSync().toArray

  def getCity(id: Int): IO[Either[EntryNotFoundError.type, City]] =
    dao.getCity(id)

  def getCitiesByCountry(idTara: Int): Array[City] =
    dao.getCityByCountryId(idTara).compile.toVector.unsafeRunSync().toArray

  def deleteCity(id: Int): IO[Either[EntryNotFoundError.type, Unit]] =
    dao.deleteCity(id)

  def addCity(city: City): IO[Either[SqlState, City]] = dao.createCity(city)

  def updateCity(
      id: Int,
      city: City
  ): IO[Either[EntryNotFoundError.type, City]] = dao.updateCity(id, city)

}
