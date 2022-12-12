package org.example.sprc

import java.sql.Timestamp

package object model {

  sealed trait Entity {
    def id: Option[Int]
  }

  case class Country(
    id: Option[Int],
    nume: Option[String],
    lat: Option[Double],
    lon: Option[Double]
  ) extends Entity

  case class City(
   id: Option[Int],
   idTara: Option[Int],
   nume: Option[String],
   lat: Option[Double],
   lon: Option[Double]
  ) extends Entity

  case class Temperature(
    id: Option[Int],
    valoare: Option[Double],
    timestamp: Option[Timestamp],
    idOras: Option[Int]
  ) extends Entity

  case class TemperatureResponse(
    id: Option[Int],
    valoare: Option[Double],
    timestamp: Option[Timestamp]
  ) extends Entity

  val errorCity: City = City(None, None, None, None, None)
  val errorCountry: Country = Country(None, None, None, None)
  val errorTemperature: Temperature = Temperature(None, None, None, None)

  case class IDResponse(id: Int)

  val CITIES = "cities"
  val COUNTRIES = "countries"
  val COUNTRY = "country"

}
