package org.example.sprc.api

import cats.data.Kleisli
import cats.effect.IO
import org.example.sprc.service.Services
import org.http4s.server.Router
import org.http4s._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT

class Apis(service: Services) {
  private val countriesApi: CountriesApi = new CountriesApi(service.countryService)
  private val cityApi: CityApi = new CityApi(service.cityService)
  private val temperatureApi: TemperatureApi = new TemperatureApi(service.temperatureService)

  val httpRoutes: Kleisli[IO, Request[IO], Response[IO]] = Router(
    "api/countries" -> countriesApi.routes,
    "api/cities" -> cityApi.routes,
    "api/temperatures" -> temperatureApi.routes,
  ).orNotFound

}
