package org.example.sprc.repository

import cats.effect.IO
import doobie.util.transactor.Transactor

final class Dao(transactor: Transactor[IO]) {
  lazy val countryDao = new CountryDao(transactor)
  lazy val cityDao = new CityDao(transactor)
  lazy val temperatureDao = new TemperatureDao(transactor)
}
