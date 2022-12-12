package org.example.sprc.api

import cats.effect.IO
import io.circe.generic.auto._
import org.example.sprc.db.DatabaseUtil
import org.example.sprc.model._
import org.example.sprc.service.CountryService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{HttpRoutes, MediaType, Request}

class CountriesApi(countryService: CountryService) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      Ok(countryService.getCountries, `Content-Type`(MediaType.application.json))

    case GET -> Root / IntVar(id) =>
      val result = countryService.getCountry(id)
      DatabaseUtil.getResponse(result)

    case req @ POST -> Root =>
      for {
        country <- extractCountryRequest(req)
        createdCountry = countryService.addCountry(country)
        response <- DatabaseUtil.postResponse(createdCountry)
      } yield response

    case req @ PUT -> Root / IntVar(id) =>
      for {
        country <- extractCountryRequest(req)
        updatedResponse = countryService.updateCountry(id, country)
        response <- DatabaseUtil.updateResponse(updatedResponse)
      } yield response

    case DELETE -> Root / IntVar(id) =>
      countryService.deleteCountry(id) match {
        case Left(_) => NotFound()
        case Right(_) => Ok()
      }
  }

  private def extractCountryRequest(req: Request[IO]): IO[Country] =
    req.decodeJson[Country].attempt.map {
      case Left(_) => errorCountry
      case Right(country) => country
    }
}
