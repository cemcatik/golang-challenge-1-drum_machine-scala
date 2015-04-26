package drum

import java.nio.{ByteOrder, ByteBuffer}

import scala.util.parsing.combinator.Parsers

trait ByteParsers extends Parsers {
  type Elem = Byte

  def literal(s: String) = acceptSeq(s.getBytes).map(_.mkString) named s

  val byte = Parser { in =>
    if (in.atEnd) Failure("Reached end of input", in)
    else Success(in.first, in.rest)
  } named "byte"

  val boolean = accept(0x00.toByte) ^^ (_ => false) | accept(0x01.toByte) ^^ (_ => true) named "boolean"

  val uint8 = byte ^^ (_ & 0xFF) named "uint8"

  val int32 = repN(4, byte) ^^ (bs => ByteBuffer.wrap(bs.toArray).getInt)  named "int32"

  val int64 = repN(8, byte) ^^ (bs => ByteBuffer.wrap(bs.toArray).getLong) named "int64"

  def bytes(length: Int) = repN(length, byte) named s"byte[$length]"

  def string(length: Int) = bytes(length) ^^ (bs => new String(bs.toArray, "US-ASCII")) named s"string[$length]"

  val lstring = int32 >> (l => string(l)) named "lstring"

  val floatL = bytes(4) ^^ (bs => ByteBuffer.wrap(bs.toArray).order(ByteOrder.LITTLE_ENDIAN).getFloat) named "floatL"

  trait RichParseResult[T] {
    def flatMap[B](f: T => ParseResult[B]): ParseResult[B]
  }

  implicit def toRichParseResult[T](pr: ParseResult[T]): RichParseResult[T] = new RichParseResult[T] {
    def flatMap[B](f: T => ParseResult[B]): ParseResult[B] = pr match {
      case Success(result, _) => f(result)
      case e: NoSuccess       => e
    }
  }
}
