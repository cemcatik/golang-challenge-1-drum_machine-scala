package nacl

import java.io._

import nacl.NaClSpec._
import org.apache.commons.io.IOUtils

import org.specs2.mutable.Specification

class NaClSpec extends Specification {
  "nacl.Nacl" should {
    "ReadWriterPing" in {
      val priv = Array.ofDim[Byte](32).initialize('p', 'r', 'i', 'v')
      val pub  = Array.ofDim[Byte](32).initialize('p', 'u', 'b')

      val (r, w) = Pipe

      val secureR = SecureReader(r, priv, pub)
      val secureW = SecureWriter(w, priv, pub)

      // Encrypt hello world
      secureW.write("hello world\n")
      secureW.close()

      // Decrypt message
      val read = IOUtils.toString(secureR)
      read must_== "hello world\n"
    }

    "SecureWriter" in {
      val priv = Array.ofDim[Byte](32).initialize('p', 'r', 'i', 'v')
      val pub  = Array.ofDim[Byte](32).initialize('p', 'u', 'b')

      val (r, w) = Pipe

      val secureR = SecureReader(r, priv, pub)
      val secureW = SecureWriter(w, priv, pub)

      // Make sure we are secure
      // Encrypt hello world
      secureW.write("hello world\n")
      secureW.close()

      // Read from the underlying transport instead of the decoder
      // Make sure we don't read the plain text message.
      val buf = IOUtils.toString(r)
      buf must_!= "hello world\n"

      val (r2, w2) = Pipe
      val secureW2 = SecureWriter(w2, priv, pub)

      // Make sure we are unique
      // Encrypt hello world
      secureW2.write("hello world\n")
      secureW2.close()

      // Read from the underlying transport instead of the decoder
      // Make sure the encrypted message is unique.
      val buf2 = IOUtils.toString(r2)
      buf must_!= buf2
    }

    "SecureEchoServer" in {
      pending
    }

    "SecureServe" in {
      pending
    }

    "SecureDial" in {
      pending
    }
  }
}

object NaClSpec {
  implicit class RichArray[T](val a: Array[T]) extends AnyVal {
    def initialize(as: T*): Array[T] = {
      as.zipWithIndex.foreach {
        case (c, i) => a(i) = c
      }
      a
    }
  }

  def Pipe = {
    val r = new PipedInputStream()
    val w = new PipedOutputStream(r)
    (r, w)
  }
}
