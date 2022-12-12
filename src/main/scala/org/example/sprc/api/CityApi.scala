package org.example.sprc.api

import cats.effect.IO
import doobie.enumerated.SqlState
import io.circe.generic.auto._
import org.example.sprc.db.DatabaseUtil
import org.example.sprc.model.{COUNTRY, City, errorCity}
import org.example.sprc.service.CityService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{HttpRoutes, MediaType, Request}

class CityApi(cityService: CityService) extends Http4sDsl[IO] {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root =>
        Ok(cityService.getCities, `Content-Type`(MediaType.application.json))

    case GET -> Root / IntVar(id) =>
      val result: Either[SqlState, City] = cityService.getCity(id)
      DatabaseUtil.getResponse(result)

    case GET -> Root / COUNTRY / IntVar(idTara) =>
      Ok(cityService.getCitiesByCountry(idTara), `Content-Type`(MediaType.application.json))

    case req @ POST -> Root =>
      for {
        city <- extractCityRequest(req)
        createdCity = cityService.addCity(city)
        response <- DatabaseUtil.postResponse(createdCity)
      } yield response

    case req @ PUT -> Root / IntVar(id) =>
      for {
        city <- extractCityRequest(req)
        updatedResponse = cityService.updateCity(id, city)
        response <- DatabaseUtil.updateResponse(updatedResponse)
      } yield response

    case DELETE -> Root / IntVar(id) =>
      cityService.deleteCity(id) match {
        case Left(_) => NotFound()
        case Right(_) => Ok()
      }
    }

  private def extractCityRequest(req: Request[IO]): IO[City] =
    req.decodeJson[City].attempt.map {
      case Left(_) => errorCity
      case Right(city) => city
    }
}
