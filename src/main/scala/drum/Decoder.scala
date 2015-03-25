package drum

import java.io.File

import drum.ByteReader._
import drum.Decoder._
import org.apache.commons.io.FileUtils

class Decoder extends ByteParsers {
  val envelope = (
    literal("SPLICE") ~>
    int64 >> (l => bytes(l.toInt))
  ) named "envelope"

  val instrument = uint8 ~ lstring ~ repN(16, boolean) ^^ {
    case id ~ name ~ steps => Instrument(id, name, steps)
  } named "instrument"

  val pattern = string(32) ~ floatL ~ instrument.* ^^ {
    case hwVersion ~ tempo ~ instruments => Pattern(hwVersion.trim, tempo, instruments)
  } named "pattern"

  def decodeFile(path: File): ParseResult[Pattern] = {
    val f = FileUtils.readFileToByteArray(path).toSeq
    for {
      env <- envelope(f)
      pat <- pattern(env)
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
