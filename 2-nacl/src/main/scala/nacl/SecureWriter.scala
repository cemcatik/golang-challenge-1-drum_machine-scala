package nacl

import java.io.{Writer, OutputStream}

import org.abstractj.kalium.{NaCl => KNaCl}
import org.abstractj.kalium.crypto.{Random, Box}

class SecureWriter(o: OutputStream, priv: Array[Byte], pub: Array[Byte]) extends Writer {
  val box = new Box(pub, priv)
  val random = new Random()

  def write(cbuf: Array[Char], off: Int, len: Int): Unit = {
    val nonce = random.randomBytes(KNaCl.Sodium.NONCE_BYTES)
    val buf = cbuf.slice(off, off + len)
    val encrypted = box.encrypt(nonce, buf.mkString.getBytes)
    o.write(nonce)
    o.write(encrypted.length)
    o.write(encrypted)
  }

  def flush(): Unit = o.flush()

  def close(): Unit = o.close()
}

object SecureWriter {
  def apply(o: OutputStream, priv: Array[Byte], pub: Array[Byte]) = new SecureWriter(o, priv, pub)
}