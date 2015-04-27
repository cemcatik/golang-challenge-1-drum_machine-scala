package nacl

import java.net.{InetAddress, ServerSocket}

object NaCl extends App {
  case class Opts(listen: Boolean = false, port: Int = 0, message: Option[String] = None)

  val parser = new scopt.OptionParser[Opts]("nacl") {
    opt[Int]('l', "listen") valueName("port") action { (p, c) => c.copy(listen = true, port = p) } text("Listen mode. Specify port")
    arg[Int]("port") action { (p, c) => c.copy(port = p) } optional() text("Client mode. Specify port to connect to")
    arg[String]("message") action { (m, c) => c.copy(message = Some(m)) } optional() text("Client mode. Specify message to send")
    checkConfig { c =>
      if (c.listen && c.message.isDefined)     failure ("Can't send message in listen mode")
      else if (c.listen && c.port == 0)        failure ("Port can't be 0 in listen mode")
      else if (!c.listen && c.message.isEmpty) failure ("Message can't be empty in listen mode")
      else success
    }
  }

  parser.parse(args, Opts()) match {
    case Some(Opts(true, p, _)) =>
      val ss = new ServerSocket(p)
      Serve(ss)

    case Some(Opts(false, p, m)) =>
      val conn = Dial(InetAddress.getLocalHost, p)
      conn.write(m.get.getBytes)
      val resp = conn.read()
      println(new String(resp))

    case None => ()
  }

}
