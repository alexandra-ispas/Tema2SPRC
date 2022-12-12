package org.example.sprc.api

import cats.effect.IO
import doobie.enumerated.SqlState
import doobie.postgres.sqlstate.class23.{CHECK_VIOLATION, FOREIGN_KEY_VIOLATION, UNIQUE_VIOLATION}
import io.circe.generic.auto._
import io.circe.syntax._
import org.example.sprc.model.{City, Country, EntryNotFoundError, IDResponse, errorCity}
import org.example.sprc.service.CityService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{HttpRoutes, MediaType, Response}

class CityApi(cityService: CityService) extends Http4sDsl[IO] {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root =>
        Ok(cityService.getCities, `Content-Type`(MediaType.application.json))

    case GET -> Root / IntVar(id) =>
      for {
        result <- cityService.getCity(id)
        response <- cityResult(result)
      } yield response

    case GET -> Root / "country" / IntVar(idTara) =>
      Ok(cityService.getCitiesByCountry(idTara), `Content-Type`(MediaType.application.json))

    case req @ POST -> Root =>
      val city = req.decodeJson[City].attempt.map {
        case Left(_) => errorCity
        case Right(value) => value
      }

      for {
        c <- city
        createdTodo = cityService.addCity(c)
        response <- putResult("put", createdTodo)
      } yield response

    case req @ PUT -> Root / IntVar(id) =>
      val city = req.decodeJson[City].attempt.map {
        case Left(_) => errorCity
        case Right(value) => value
      }

      for {
        c <- city
        updatedResponse = cityService.updateCity(id, c)
        response <- putResult("update", updatedResponse)
      } yield response

    case DELETE -> Root / IntVar(id) =>
      cityService.deleteCity(id).flatMap {
        case Left(EntryNotFoundError) => NotFound()
        case Right(_)                 => Ok()
      }
    }

  private def cityResult(
      result: Either[EntryNotFoundError.type, City]
  ): IO[Response[IO]] = {
    result match {
      case Left(EntryNotFoundError) => NotFound()
      case Right(city)              => Ok(city.asJson)
    }
  }

  private def putResult(
    opType: String,
   city: Either[SqlState, City]
 ): IO[Response[IO]] = city match {
    case Right(city) if opType == "put" => Created(IDResponse(city.id.get).asJson, `Content-Type`(MediaType.application.json))
    case Right(city) => Ok(city.asJson, `Content-Type`(MediaType.application.json))
    case Left(UNIQUE_VIOLATION) => Conflict()
    case Left(CHECK_VIOLATION) => BadRequest()
    case Left(FOREIGN_KEY_VIOLATION) => BadRequest()
    case Left(_) => BadRequest()
  }
}
