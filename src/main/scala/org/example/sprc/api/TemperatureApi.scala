package org.example.sprc.api

import cats.effect.IO
import io.circe.Decoder.Result
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder, HCursor, Json}
import org.example.sprc.db.DatabaseUtil
import org.example.sprc.model._
import org.example.sprc.service.TemperatureService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.toMessageSyntax
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{HttpRoutes, MediaType, Request}

import java.sql.Timestamp

class TemperatureApi(temperatureService: TemperatureService) extends Http4sDsl[IO] {

  implicit val TimestampFormat : Encoder[Timestamp] with Decoder[Timestamp] = new Encoder[Timestamp] with Decoder[Timestamp] {
    override def apply(a: Timestamp): Json = Encoder.encodeString.apply(a.toString)
    override def apply(c: HCursor): Result[Timestamp] = Decoder.decodeString.map((s: String) => Timestamp.valueOf(s)).apply(c)
  }

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req@GET -> Root =>
      Ok(temperatureService.getTemperature(req.params), `Content-Type`(MediaType.application.json))

    case req@GET -> Root / CITIES / IntVar(id) =>
      Ok(temperatureService.getCityTemperature(id, req.params), `Content-Type`(MediaType.application.json))

    case req@GET -> Root / COUNTRIES / IntVar(id) =>
      Ok(temperatureService.getCountryTemperature(id, req.params), `Content-Type`(MediaType.application.json))

    case req@POST -> Root =>
      for {
        temper <- extractTemperature(req)
        createdTemperature = temperatureService.addTemperature(temper)
        response <- DatabaseUtil.postResponse(createdTemperature)
      } yield
        response

    case req @ PUT -> Root / IntVar(id) =>
      for {
        temperature <- extractTemperature(req)
        updatedResponse = temperatureService.updateTemperature(id, temperature)
        response <- DatabaseUtil.getResponse(updatedResponse)
      } yield response

    case DELETE -> Root / IntVar(id) =>
      temperatureService.deleteTemperature(id) match {
        case Left(_)  => NotFound()
        case Right(_) => Ok()
      }
  }

  private def extractTemperature(req: Request[IO]): IO[Temperature] =
    req.decodeJson[Temperature].attempt.map {
      case Left(_) => errorTemperature
      case Right(temperature) => temperature
    }
}
