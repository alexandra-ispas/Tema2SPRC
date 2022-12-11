package org.example.sprc.api

import cats.effect.IO
import doobie.enumerated.SqlState
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION
import io.circe.generic.auto._
import io.circe.syntax._
import org.example.sprc.model.{Country, EntryNotFoundError, IDResponse}
import org.example.sprc.service.CountryService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{HttpRoutes, MediaType, Response}

class CountriesApi(countryService: CountryService) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      Ok(countryService.getCountries, `Content-Type`(MediaType.application.json))

    case GET -> Root / IntVar(id) =>
      for {
        result <- countryService.getCountry(id)
        response <- countryResult(result)
      } yield response

    case req @ POST -> Root =>
      for {
        country <- req.decodeJson[Country]
        createdTodo <- countryService.addCountry(country)
        response <- putResult(createdTodo)
      } yield response


    case req @ PUT -> Root / IntVar(id) =>
      for {
        country <- req.decodeJson[Country]
        updatedResponse <- countryService.updateCountry(id, country)
        response <- countryResult(updatedResponse)
      } yield response

    case DELETE -> Root / IntVar(id) =>
      countryService.deleteCountry(id).flatMap {
        case Left(EntryNotFoundError) => NotFound()
        case Right(_)                 => NoContent()
      }
  }

  private def putResult(
     country: Either[SqlState, Country]
  ): IO[Response[IO]] = country match {
    case Right(country) => Created(IDResponse(country.id.get).asJson, `Content-Type`(MediaType.application.json))
    case Left(UNIQUE_VIOLATION) => Conflict()
    case Left(_) => BadRequest()
  }

  private def countryResult(
    result: Either[EntryNotFoundError.type, Country]
  ): IO[Response[IO]] = {
    result match {
      case Left(EntryNotFoundError) => NotFound()
      case Right(country)           => Ok(country.asJson)
    }
  }

}
