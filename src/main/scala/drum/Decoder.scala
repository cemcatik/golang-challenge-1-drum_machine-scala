package drum

import java.io.File
import java.nio.{ByteBuffer, ByteOrder}

import drum.Decoder._
import org.apache.commons.io.FileUtils

import scala.util.parsing.combinator._

class Decoder extends Parsers {
  type Elem = Byte

  def literal(s: String) = acceptSeq(s.getBytes).map(_.mkString) named s
  val byte = Parser { in =>
    if (in.atEnd) Failure("Reached end of input", in)
    else Success(in.first, in.rest)
  } named "byte"
  val uint8 = byte ^^ (_ & 0xFF) named "uint8"
  val int32 = repN(4, byte) ^^ (bs => ByteBuffer.wrap(bs.toArray).getInt)  named "int32"
  val int64 = repN(8, byte) ^^ (bs => ByteBuffer.wrap(bs.toArray).getLong) named "int64"
  def bytes(length: Int) = repN(length, byte) named s"byte[$length]"
  def string(length: Int) = bytes(length) ^^ (bs => new String(bs.toArray, "US-ASCII")) named s"string[$length]"
  val lstring = int32 >> (l => string(l)) named "lstring"
  val floatL = bytes(4) ^^ (bs => ByteBuffer.wrap(bs.toArray).order(ByteOrder.LITTLE_ENDIAN).getFloat) named "floatL"

  val envelope =
    literal("SPLICE") ~>
    int64 >> (l => bytes(l.toInt))

  val pattern = string(32) ~ floatL ~ instrument.* ^^ {
    case hwVersion ~ tempo ~ instruments => Pattern(hwVersion.trim, tempo, instruments)
  }

  val instrument = uint8 ~ lstring ~ bytes(16) ^^ {
    case id ~ name ~ steps =>
      val ss = steps map {
        case 0 => false
        case 1 => true
      }
      Instrument(id, name, ss)
  }

  case class RichParseResult[T](pr: ParseResult[T]) {
    def flatMap[B](f: T => ParseResult[B]): ParseResult[B] = pr match {
      case Success(result, _) => f(result)
      case e: NoSuccess       => e
    }
  }

  def decodeFile(path: File): ParseResult[Pattern] = {
    val f = FileUtils.readFileToByteArray(path)
    val r = new ByteReader(f)
    for {
      env <- RichParseResult(envelope(r))
      pat <- pattern(new ByteReader(env))
    } yield pat
  }
}

object Decoder {
  case class Pattern(hwVersion: String,
                     tempo: Double,
                     instruments: Seq[Instrument]) {
    override def toString = {
      val formattedTempo = if (tempo.isValidInt) f"$tempo%.0f" else f"$tempo%.1f"

      s"""Saved with HW Version: $hwVersion
          |Tempo: $formattedTempo
          |${instruments mkString "\n"}
          |""".stripMargin
    }
  }

  case class Instrument(id: Int,
                        name: String,
                        steps: Seq[Boolean]) {
    require(steps.size == 16, "There has to be 16 steps")

    override def toString = {
      val ss = steps.
        map {
          case false => "-"
          case true  => "x"
        }.
        grouped(4).
        map(_ mkString "").
        mkString("|", "|", "|")

      s"($id) $name\t$ss"
    }
  }
}
