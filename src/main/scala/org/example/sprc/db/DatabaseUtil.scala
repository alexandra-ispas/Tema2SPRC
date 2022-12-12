package org.example.sprc.db

import cats.effect.IO
import doobie.enumerated.SqlState
import doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.example.sprc.model._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import org.http4s.{MediaType, Response}


object DatabaseUtil extends Http4sDsl[IO] {

  def getResponse[T <: Entity : Encoder](
    result: Either[SqlState, T]
  ): IO[Response[IO]] =
    result match {
      case Left(_) => NotFound()
      case Right(value) => Ok(value.asJson)
    }

  def postResponse[T <: Entity : Encoder](
   result: Either[SqlState, T]
  ): IO[Response[IO]] =
    result match {
      case Right(value) => Created(IDResponse(value.id.get).asJson, `Content-Type`(MediaType.application.json))
      case Left(UNIQUE_VIOLATION) => Conflict()
      case Left(_) => BadRequest()
    }

  def updateResponse[T <: Entity : Encoder] (
    result: Either[SqlState, T]
  ): IO[Response[IO]] =
    result match {
      case Right(country) => Ok(country.asJson, `Content-Type`(MediaType.application.json))
      case Left(UNIQUE_VIOLATION) => Conflict()
      case Left(_) => BadRequest()
    }

}
