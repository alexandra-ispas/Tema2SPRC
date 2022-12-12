package org.example.sprc.service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.enumerated.SqlState
import doobie.postgres.sqlstate.class22.INVALID_PARAMETER_VALUE
import doobie.postgres.sqlstate.class23.FOREIGN_KEY_VIOLATION
import org.example.sprc.model.{Country, EntryNotFoundError}
import org.example.sprc.repository.CountryDao

class CountryService(dao: CountryDao) {

  def getCountries: Array[Country] =
    dao.getCountries.compile.toVector.unsafeRunSync().toArray

  def getCountry(id: Int): IO[Either[EntryNotFoundError.type, Country]] =
    dao.getCountry(id)

  def deleteCountry(id: Int): IO[Either[EntryNotFoundError.type, Unit]] =
    dao.deleteCountry(id)

  def addCountry(country: Country): Either[SqlState, Country] = {
    if (country.nume.isEmpty || country.lat.isEmpty || country.lon.isEmpty)
      Left(FOREIGN_KEY_VIOLATION)
    else dao.createCountry(country)
  }

  def updateCountry(
      id: Int,
      country: Country
  ): Either[SqlState, Country] =
    if (country.nume.isEmpty || country.lat.isEmpty || country.lon.isEmpty)
      Left(INVALID_PARAMETER_VALUE)
    else dao.updateCountry(id, country)
}
