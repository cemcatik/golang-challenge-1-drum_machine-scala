package nacl

import java.io.{Reader, InputStream}

import org.abstractj.kalium.{NaCl => KNaCl}
import org.abstractj.kalium.crypto.Box

class SecureReader(i: InputStream,  priv: Array[Byte], pub: Array[Byte]) extends Reader {
  val box = new Box(pub, priv)

  def read(cbuf: Array[Char], off: Int, len: Int): Int = {
    if (i.available == 0) {
      -1
    } else {
      val nonce = i.read(KNaCl.Sodium.NONCE_BYTES)
      val wLen  = i.read()
      val msg   = i.read(wLen)
      val decrypted = new String(box.decrypt(nonce, msg))
      decrypted.zipWithIndex.foreach {
        case (c, i) => cbuf(off + i) = c
      }
      decrypted.length
    }
  }

  def close(): Unit = i.close()
}

object SecureReader {
  def apply(i: InputStream, priv: Array[Byte], pub: Array[Byte]) = new SecureReader(i, priv, pub)
}
