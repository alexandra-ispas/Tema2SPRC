package org.example.sprc.service

import cats.effect.unsafe.implicits.global
import doobie.enumerated.SqlState
import doobie.postgres.sqlstate.class22.INVALID_PARAMETER_VALUE
import org.example.sprc.model.Country
import org.example.sprc.repository.CountryDao

class CountryService(dao: CountryDao) {

  def getCountries: Array[Country] =
    dao.getCountries.compile.toVector.unsafeRunSync().toArray

  def getCountry(id: Int): Either[SqlState, Country] =
    dao.getCountry(id)

  def deleteCountry(id: Int): Either[SqlState, Int] =
    dao.deleteCountry(id)

  def addCountry(country: Country): Either[SqlState, Country] = {
    if (isRequestValid(country))
      dao.createCountry(country)
    else Left(INVALID_PARAMETER_VALUE)
  }

  def updateCountry(
      id: Int,
      country: Country
  ): Either[SqlState, Country] =
    if (isRequestValid(country))
      dao.updateCountry(id, country)
    else Left(INVALID_PARAMETER_VALUE)

  private def isRequestValid(country: Country) =
    !(country.nume.isEmpty || country.lat.isEmpty || country.lon.isEmpty)
}
