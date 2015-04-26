package drum

import java.io.File

import org.specs2._
import org.specs2.specification.Tables

class DecoderSpec extends Specification with Tables {
  val expected1 =
    """Saved with HW Version: 0.808-alpha
      |Tempo: 120
      |(0) kick	|x---|x---|x---|x---|
      |(1) snare	|----|x---|----|x---|
      |(2) clap	|----|x-x-|----|----|
      |(3) hh-open	|--x-|--x-|x-x-|--x-|
      |(4) hh-close	|x---|x---|----|x--x|
      |(5) cowbell	|----|----|--x-|----|
      |""".stripMargin

  val expected2 =
    """Saved with HW Version: 0.808-alpha
      |Tempo: 98.4
      |(0) kick	|x---|----|x---|----|
      |(1) snare	|----|x---|----|x---|
      |(3) hh-open	|--x-|--x-|x-x-|--x-|
      |(5) cowbell	|----|----|x---|----|
      |""".stripMargin

  val expected2MoreBells =
    """Saved with HW Version: 0.808-alpha
      |Tempo: 98.4
      |(0) kick	|x---|----|x---|----|
      |(1) snare	|----|x---|----|x---|
      |(3) hh-open	|--x-|--x-|x-x-|--x-|
      |(5) cowbell	|x---|x-x-|x---|x-x-|
      |""".stripMargin

  val expected3 =
    """Saved with HW Version: 0.808-alpha
      |Tempo: 118
      |(40) kick	|x---|----|x---|----|
      |(1) clap	|----|x---|----|x---|
      |(3) hh-open	|--x-|--x-|x-x-|--x-|
      |(5) low-tom	|----|---x|----|----|
      |(12) mid-tom	|----|----|x---|----|
      |(9) hi-tom	|----|----|-x--|----|
      |""".stripMargin

  val expected4 =
    """Saved with HW Version: 0.909
      |Tempo: 240
      |(0) SubKick	|----|----|----|----|
      |(1) Kick	|x---|----|x---|----|
      |(99) Maracas	|x-x-|x-x-|x-x-|x-x-|
      |(255) Low Conga	|----|x---|----|x---|
      |""".stripMargin

  val expected5 =
    """Saved with HW Version: 0.708-alpha
      |Tempo: 999
      |(1) Kick	|x---|----|x---|----|
      |(2) HiHat	|x-x-|x-x-|x-x-|x-x-|
      |""".stripMargin

  def is = s2"""
    drum.Decoder should decode splices as expected ${
      "serialized file"            | "expected"         |>
      "pattern_1.splice"           ! expected1          |
      "pattern_2.splice"           ! expected2          |
      "pattern_2-morebells.splice" ! expected2MoreBells |
      "pattern_3.splice"           ! expected3          |
      "pattern_4.splice"           ! expected4          |
      "pattern_5.splice"           ! expected5          |
        { (f, e) =>
          val path = new File("src/test/resources", f)
          new Decoder().decodeFile(path).get.toString must_== e
        }
    }
"""
}
