package models.graphql

import org.apache.commons.codec.binary.Base64
import sangria.schema._
import sangria.validation.ValueCoercionViolation

import scala.util.{Failure, Success, Try}

protected[graphql] trait CommonSchemaReferenceTypes {
  case object Base64CoercionViolation extends ValueCoercionViolation("Base64-encoded value expected.")
  case object JsonCoercionViolation extends ValueCoercionViolation("Valid JSON value expected.")
  case object UuidCoercionViolation extends ValueCoercionViolation("UUID value expected in format [00000000-0000-0000-0000-000000000000].")

  private[this] def parseUuid(s: String) = Try(java.util.UUID.fromString(s)) match {
    case Success(u) => Right(u)
    case Failure(_) => Left(UuidCoercionViolation)
  }

  implicit val uuidType = ScalarType[java.util.UUID](
    name = "UUID",
    description = Some("A string representing a UUID, in format [00000000-0000-0000-0000-000000000000]."),
    coerceOutput = (u, _) => u.toString,
    coerceUserInput = {
      case s: String => parseUuid(s)
      case _ => Left(UuidCoercionViolation)
    },
    coerceInput = {
      case sangria.ast.StringValue(s, _, _) => parseUuid(s)
      case _ => Left(UuidCoercionViolation)
    }
  )

  private[this] def parseJson(s: String) = io.circe.parser.parse(s) match {
    case Right(u) => Right(u)
    case Left(_) => Left(UuidCoercionViolation)
  }

  implicit val jsonType = ScalarType[io.circe.Json](
    name = "JSON",
    description = Some("A string representation of JSON."),
    coerceOutput = (u, _) => u.spaces2,
    coerceUserInput = {
      case s: String => parseJson(s)
      case _ => Left(JsonCoercionViolation)
    },
    coerceInput = {
      case sangria.ast.StringValue(s, _, _) => parseJson(s)
      case _ => Left(JsonCoercionViolation)
    }
  )

  implicit val byteArrayType = ScalarType[Array[Byte]](
    name = "Base64",
    description = Some("A binary array of bytes, encoded with Base64."),
    coerceOutput = (u, _) => Base64.encodeBase64(u),
    coerceUserInput = {
      case s: String => Right(Base64.decodeBase64(s))
      case _ => Left(Base64CoercionViolation)
    },
    coerceInput = {
      case sangria.ast.StringValue(s, _, _) => Right(Base64.decodeBase64(s))
      case _ => Left(Base64CoercionViolation)
    }
  )
}