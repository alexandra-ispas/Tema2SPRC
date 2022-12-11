package org.example.sprc.service

import org.example.sprc.repository.Dao

final class Services(dao: Dao) {
  lazy val countryService = new CountryService(dao.countryDao)
  lazy val cityService = new CityService(dao.cityDao)
  lazy val temperatureService = new TemperatureService(dao.temperatureDao)
}
