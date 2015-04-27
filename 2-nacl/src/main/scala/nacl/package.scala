import java.io._
import java.net._

import org.abstractj.kalium.NaCl.Sodium._
import org.abstractj.kalium.keys.{PrivateKey, KeyPair}
import org.apache.commons.io.IOUtils

package object nacl {
  implicit class RichInputStream(val i: InputStream) extends AnyVal {
    def read(len: Int): Array[Byte] = {
      require(len >= 0, "length must be positive")

      val buf = Array.ofDim[Byte](len)
      val rLen = i.read(buf)
      buf.take(rLen)
    }
  }

  object Thread extends {
    def apply(block: => Any) = {
      val t = new java.lang.Thread(new Runnable {
        def run = block
      })
      t.start()
    }
  }

  object Serve {
    def apply(ss: ServerSocket): Unit = {
      while (ss.isBound) {
        val s = ss.accept()
        Thread {
          try {
            val (reader, writer) = Dial.handshake(s)

            while (s.isConnected) {
              // Read and Echo
              val buf = Array.ofDim[Char](32 * 1024)
              val len = reader.read(buf)
              if (len != -1) {
                val read = buf.take(len)
                writer.write(read)
              }
            }
          } catch {
            case e: Exception => s.close()
          }
        }
      }
    }
  }

  object Dial {
    def apply(address: InetAddress, port: Int): ReadWriteCloser = new ReadWriteCloser {
      val socket = new Socket(address, port)
      val (reader, writer) = handshake(socket)

      def read(): Array[Byte] = {
        val buf = Array.ofDim[Char](32 * 1024)
        var len = -1
        do {
          len = reader.read(buf)
        } while (len == -1)

        buf.take(len).mkString.getBytes
      }

      def write(as: Array[Byte]): Unit = writer.write(new String(as).toCharArray)

      def close(): Unit = socket.close()
    }

    def handshake(s: Socket) = {
      val in  = s.getInputStream
      val out = s.getOutputStream

      // Generate Public/Private KeyPair
      val keyPair = new KeyPair()
      val selfPub = keyPair.getPublicKey
      val priv    = keyPair.getPrivateKey

      // Handshake - send our Public Key and Read their Public Key
      IOUtils.write(selfPub.toBytes, out)
      val otherPub = new PrivateKey(in.read(PUBLICKEY_BYTES))

      val reader = SecureReader(in,  priv.toBytes, otherPub.toBytes)
      val writer = SecureWriter(out, priv.toBytes, otherPub.toBytes)
      (reader, writer)
    }
  }

  object RawDial {
    def apply(address: InetAddress, port: Int): ReadWriteCloser = new ReadWriteCloser {
      val socket = new Socket(address, port)
      val in  = socket.getInputStream
      val out = socket.getOutputStream

      def read(): Array[Byte] = IOUtils.toString(in).getBytes

      def write(as: Array[Byte]): Unit = out.write(as)

      def close(): Unit = socket.close()
    }
  }
}
