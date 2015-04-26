package drum

import scala.util.parsing.input.{Position, Reader}

class ByteReader(source: Seq[Byte], override val offset: Int = 0) extends Reader[Byte] {
  def first = if (atEnd) 0x1a else source(offset)

  def rest = if (atEnd) this else new ByteReader(source, offset + 1)

  def atEnd = offset >= source.length

  def pos = new Position {
    def line = 1
    def column = offset + 1
    def lineContents = Integer.toHexString(first)
  }
}

object ByteReader {
  implicit def seq2reader(source: Seq[Byte]): ByteReader = new ByteReader(source)
}
