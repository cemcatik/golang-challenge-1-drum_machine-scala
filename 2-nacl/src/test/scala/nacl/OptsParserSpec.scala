package nacl

import nacl.NaCl._
import org.specs2.Specification
import org.specs2.specification.Tables

class OptsParserSpec extends Specification with Tables {
  def is = s2"""
  Command Line Parser ${
    "args"            | "expectation"                                  |>
    "-l 8080"         ! Some(Opts(true, Some(8080), None))             |
    "-l 8080 message" ! None                                           |
    "8080"            ! None                                           |
    "8080 message"    ! Some(Opts(false, Some(8080), Some("message"))) |
    { (args, exp) => parser.parse(args.split(" "), Opts()) must_== exp }
  }
  """
}
