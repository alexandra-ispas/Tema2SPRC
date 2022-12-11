package org.example.sprc

package object model {

  case class Country(id: Option[Int], nume: String, lat: Double, lon: Double)
  case class City(id: Option[Int], idTara: Int, nume: String, lat: Double, lon: Double)
  case class Temperature(id: Option[Int], idOras: Int, valoare: Double, timestamp: Option[String])

  case object EntryNotFoundError
  case object UniqueKeyAlreadyExists

  case class IDResponse(id: Int)
}
