package org.example.sprc.api

import cats.effect.IO
import doobie.enumerated.SqlState
import doobie.postgres.sqlstate.class23._
import io.circe.generic.auto._
import io.circe.syntax._
import org.example.sprc.model._
import org.example.sprc.service.CountryService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{HttpRoutes, MediaType, Request, Response}

class CountriesApi(countryService: CountryService) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      Ok(countryService.getCountries, `Content-Type`(MediaType.application.json))

    case GET -> Root / IntVar(id) =>
      for {
        result <- countryService.getCountry(id)
        response <- getResult(result)
      } yield response

    case req @ POST -> Root =>
      for {
        country <- extractCountryRequest(req)
        createdCountry = countryService.addCountry(country)
        response <- putResult("put", createdCountry)
      } yield response

    case req @ PUT -> Root / IntVar(id) =>
      for {
        country <- extractCountryRequest(req)
        updatedResponse = countryService.updateCountry(id, country)
        response <- putResult("update", updatedResponse)
      } yield response

    case DELETE -> Root / IntVar(id) =>
      countryService.deleteCountry(id).flatMap {
        case Left(EntryNotFoundError) => NotFound()
        case Right(_)                 => Ok()
      }
  }

  // todo: make this generic
  private def putResult(
     opType: String,
     country: Either[SqlState, Country]
  ): IO[Response[IO]] = country match {
    case Right(country) if opType == "put" => Created(IDResponse(country.id.get).asJson, `Content-Type`(MediaType.application.json))
    case Right(country) => Ok(country.asJson, `Content-Type`(MediaType.application.json))
    case Left(UNIQUE_VIOLATION) => Conflict()
    case Left(_) => BadRequest()
  }

  private def getResult(
    result: Either[EntryNotFoundError.type, Country]
  ): IO[Response[IO]] = {
    result match {
      case Left(EntryNotFoundError) => NotFound()
      case Right(country)           => Ok(country.asJson)
    }
  }

  private def extractCountryRequest(req: Request[IO]): IO[Country] =
    req.decodeJson[Country].attempt.map {
      case Left(_) => errorCountry
      case Right(value) => value
    }

}
