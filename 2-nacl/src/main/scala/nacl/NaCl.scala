package nacl

import java.net.{InetAddress, ServerSocket}

object NaCl {
  case class Opts(listen: Boolean = false, port: Option[Int] = None, message: Option[String] = None)

  val parser = new scopt.OptionParser[Opts]("nacl") {
    opt[Unit]('l', "listen") action { (_, c) => c.copy(listen = true) }     text("Listen mode")                           optional()
    arg[Int]("port")         action { (p, c) => c.copy(port = Some(p)) }    text("Specify port to connect to")
    arg[String]("message")   action { (m, c) => c.copy(message = Some(m)) } text("Client mode - specify message to send") optional()
    checkConfig { c =>
      if (c.listen && c.message.isDefined)     failure("Can't send message in listen mode")
      else if (!c.listen && c.message.isEmpty) failure("Message can't be empty in client mode")
      else success
    }
  }
}

object Main extends App {
  import NaCl._

  parser.parse(args, Opts()) match {
    case Some(Opts(true, Some(p), _)) =>
      val ss = new ServerSocket(p)
      Serve(ss)

    case Some(Opts(false, Some(p), Some(m))) =>
      val conn = Dial(InetAddress.getLocalHost, p)
      conn.write(m.getBytes)
      val resp = conn.read()
      println(new String(resp))

    case _ => ()
  }

}
