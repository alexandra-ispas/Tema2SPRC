package org.example.sprc.service

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doobie.enumerated.SqlState
import org.example.sprc.model.{Country, EntryNotFoundError, UniqueKeyAlreadyExists}
import org.example.sprc.repository.CountryDao

class CountryService(dao: CountryDao) {

  def getCountries: Array[Country] =
    dao.getCountries.compile.toVector.unsafeRunSync().toArray

  def getCountry(id: Int): IO[Either[EntryNotFoundError.type, Country]] =
    dao.getCountry(id)

  def deleteCountry(id: Int): IO[Either[EntryNotFoundError.type, Unit]] =
    dao.deleteCountry(id)

  def addCountry(country: Country): IO[Either[SqlState, Country]] = dao.createCountry(country)

  def updateCountry(
      id: Int,
      country: Country
  ): IO[Either[EntryNotFoundError.type, Country]] = dao.updateCountry(id, country)
}
