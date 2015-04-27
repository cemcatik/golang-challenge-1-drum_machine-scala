package nacl

trait ReadWriteCloser {
  def read(): Array[Byte]
  def write(as: Array[Byte]): Unit
  def close(): Unit
}
