package org.example.sprc.api

import cats.effect.IO
import io.circe.syntax.EncoderOps
import org.example.sprc.model.{EntryNotFoundError, IDResponse, Temperature}
import org.example.sprc.service.TemperatureService
import org.http4s.circe.toMessageSyntax
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._


class TemperatureApi(temperatureService: TemperatureService) extends Http4sDsl[IO] {

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req@GET -> Root =>
      Ok(temperatureService.getTemperature(req.params), `Content-Type`(MediaType.application.json))

    case req@POST -> Root =>
      for {
        temperature <- req.decodeJson[Temperature]
        temperature <- temperatureService.addTemperature(temperature)
        id = IDResponse(temperature.id.get)
        response <- Created(id.asJson, `Content-Type`(MediaType.application.json))
      } yield  {
        response
      }

    //    case req @ PUT -> Root / IntVar(id) =>
    //      for {
    //        city <- req.decodeJson[City]
    //        updatedResponse <- cityService.updateCity(id, city)
    //        response <- cityResult(updatedResponse)
    //      } yield response
    //
    //    case DELETE -> Root / IntVar(id) =>
    //      cityService.deleteCity(id).flatMap {
    //        case Left(EntryNotFoundError) => NotFound()
    //        case Right(_)                => NoContent()
    //      }
    //    }
    //
  }

  private def temperatureResult(
      result: Either[EntryNotFoundError.type, Temperature]
  ): IO[Response[IO]] = {
    result match {
      case Left(EntryNotFoundError) => NotFound()
      case Right(temperature)       => Ok(temperature.asJson)
    }
  }

}
