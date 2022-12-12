package org.example.sprc.api

import cats.effect.IO
import doobie.enumerated.SqlState
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax.EncoderOps
import org.example.sprc.model._
import org.example.sprc.service.TemperatureService
import org.http4s.circe.toMessageSyntax
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._

import java.sql.Timestamp

class TemperatureApi(temperatureService: TemperatureService) extends Http4sDsl[IO] {

  implicit val TimestampFormat : Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeString.apply(a.toString)

    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeString.map((s: String) => Timestamp.valueOf(s)).apply(c)
  }

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req@GET -> Root =>
      Ok(temperatureService.getTemperature(req.params), `Content-Type`(MediaType.application.json))

    case req@GET -> Root / "cities" / IntVar(id) =>
      print("cities")
      Ok(temperatureService.getCityTemperature(id, req.params), `Content-Type`(MediaType.application.json))

    case req@GET -> Root / "countries" / IntVar(id) =>
      Ok(temperatureService.getCountryTemperature(id, req.params), `Content-Type`(MediaType.application.json))

      case req@POST -> Root =>
        val temperature = req.decodeJson[Temperature].attempt.map {
          case Left(_) => errorTemperature
          case Right(value) => value
        }
        for {
          t <- temperature
          createdTemperature = temperatureService.addTemperature(t)
          response <- putResult(createdTemperature)
        } yield response

    case req @ PUT -> Root / IntVar(id) =>
      for {
        temperature <- req.decodeJson[Temperature]
        updatedResponse <- temperatureService.updateTemperature(id, temperature)
        response <- temperatureResult(updatedResponse)
      } yield response

    case DELETE -> Root / IntVar(id) =>
      temperatureService.deleteTemperature(id).flatMap {
        case Left(EntryNotFoundError) => NotFound()
        case Right(_)                 => Ok()
      }

  }

  private def putResult(
   temperature: Either[SqlState, Temperature]
  ): IO[Response[IO]] = temperature match {
    case Right(temperature) => Created(IDResponse(temperature.id.get).asJson, `Content-Type`(MediaType.application.json))
    case Left(UNIQUE_VIOLATION) => Conflict()
    case Left(_) => BadRequest()
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
