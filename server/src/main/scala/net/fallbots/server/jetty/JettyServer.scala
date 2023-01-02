package net.fallbots.server.jetty

import akka.actor.{ActorRef, ActorSystem}
import net.fallbots.server.AbstractServer
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.websocket.javax.server.config.JavaxWebSocketServletContainerInitializer

import javax.servlet.ServletContext
import javax.websocket.server.ServerContainer

// based off of embedded jetty websocket server example from here:
// https://github.com/jetty-project/embedded-jetty-websocket-examples
//object JettyServer() {

class JettyServer(actorSystem: ActorSystem, botManager: ActorRef, gameManager: ActorRef, port: Int)
    extends AbstractServer {
  private val server    = new Server
  private val connector = new ServerConnector(server)

  JettyWebsocketHandler.initialise(actorSystem, botManager, gameManager)
  connector.setPort(port)
  server.addConnector(connector)

  // Setup the basic application "context" for this application at "/"
  // This is also known as the handler tree (in jetty speak)
  val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
  context.setContextPath("/")
  server.setHandler(context)
  // Initialize javax.websocket layer
  JavaxWebSocketServletContainerInitializer.configure(
    context,
    (servletContext: ServletContext, wsContainer: ServerContainer) => {

      // This lambda will be called at the appropriate place in the
      // ServletContext initialization phase where you can initialize
      // and configure  your websocket container.
      // Configure defaults for container
      wsContainer.setDefaultMaxTextMessageBufferSize(65535)
      // Add WebSocket endpoint to javax.websocket layer
      wsContainer.addEndpoint(classOf[JettyWebsocketHandler])
    }
  )

  def start(): Unit = { server.start() }

  def shutdown(): Unit = { server.stop() }
}
