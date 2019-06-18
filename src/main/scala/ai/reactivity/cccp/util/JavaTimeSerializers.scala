package ai.reactivity.cccp.util

import java.time.format.DateTimeFormatter
import java.time.temporal.{TemporalAccessor, TemporalQuery}
import java.time.{Instant, LocalDateTime, LocalTime, ZonedDateTime}

import org.json4s.CustomSerializer
import org.json4s.JsonAST.JString

object JavaTimeSerializers {

  val defaults: List[CustomSerializer[_]] =
    InstantSerializer ::
      LocalTimeSerializer ::
      LocalDateTimeSerializer ::
      ZonedDateTimeSerializer ::
      Nil

  object InstantSerializer extends InstantSerializer(DateTimeFormatter.ISO_INSTANT)

  object LocalTimeSerializer extends LocalTimeSerializer(DateTimeFormatter.ISO_LOCAL_TIME)

  object LocalDateTimeSerializer extends LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

  object ZonedDateTimeSerializer extends ZonedDateTimeSerializer(DateTimeFormatter.ISO_ZONED_DATE_TIME)

  class InstantSerializer private[JavaTimeSerializers](val format: DateTimeFormatter) extends CustomSerializer[Instant](_ => ( {
    case JString(s) => format.parse(s, asQuery(Instant.from))
  }, {
    case t: Instant => JString(format.format(t))
  }))

  class LocalTimeSerializer private[JavaTimeSerializers](val format: DateTimeFormatter) extends CustomSerializer[LocalTime](_ => ( {
    case JString(s) => format.parse(s, asQuery(LocalTime.from))
  }, {
    case t: LocalTime => JString(format.format(t))
  }))

  class LocalDateTimeSerializer private[JavaTimeSerializers](val format: DateTimeFormatter) extends CustomSerializer[LocalDateTime](_ => ( {
    case JString(s) => format.parse(s, asQuery(LocalDateTime.from))
  }, {
    case t: LocalDateTime => JString(format.format(t))
  }))

  class ZonedDateTimeSerializer private[JavaTimeSerializers](val format: DateTimeFormatter) extends CustomSerializer[ZonedDateTime](_ => ( {
    case JString(s) => format.parse(s, asQuery(ZonedDateTime.from))
  }, {
    case t: ZonedDateTime => JString(format.format(t))
  }))

  def asQuery[A](f: TemporalAccessor => A): TemporalQuery[A] = (temporal: TemporalAccessor) => f(temporal)
}
