/*
 * MIT License
 *
 * Copyright (c) 2018 Danilo Oliveira
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.sourceforge.kleinlisp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * TCP server that accepts connections and spawns REPL sessions for each client.
 *
 * <p>This enables Emacs/Geiser to connect to a running KleinLisp instance via geiser-connect.
 *
 * @author Danilo Oliveira
 */
public class SocketReplServer {

  public static final int DEFAULT_PORT = 37146; // "kL" in ASCII (107 + 76 * 256 + some offset)

  private final int port;
  private final Supplier<Lisp> lispFactory;
  private ServerSocket serverSocket;
  private ExecutorService executor;
  private volatile boolean running;

  /**
   * Creates a new socket REPL server with the default port.
   *
   * @param lispFactory factory function to create Lisp instances for each connection
   */
  public SocketReplServer(Supplier<Lisp> lispFactory) {
    this(DEFAULT_PORT, lispFactory);
  }

  /**
   * Creates a new socket REPL server.
   *
   * @param port the TCP port to listen on
   * @param lispFactory factory function to create Lisp instances for each connection
   */
  public SocketReplServer(int port, Supplier<Lisp> lispFactory) {
    this.port = port;
    this.lispFactory = lispFactory;
  }

  /**
   * Starts the server and blocks until shutdown. Each incoming connection gets its own REPL session
   * with an isolated Lisp environment.
   *
   * @throws IOException if the server socket cannot be created
   */
  public void start() throws IOException {
    serverSocket = new ServerSocket(port);
    executor = Executors.newCachedThreadPool();
    running = true;

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  try {
                    stop();
                  } catch (Exception e) {
                    // Ignore shutdown errors
                  }
                }));

    acceptConnections();
  }

  /** Stops the server and closes all active connections. */
  public void stop() {
    running = false;

    if (serverSocket != null && !serverSocket.isClosed()) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        // Ignore close errors
      }
    }

    if (executor != null && !executor.isShutdown()) {
      executor.shutdown();
      try {
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
          executor.shutdownNow();
        }
      } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }

  private void acceptConnections() {
    while (running) {
      try {
        Socket clientSocket = serverSocket.accept();
        Lisp lisp = lispFactory.get();
        SocketRepl socketRepl = new SocketRepl(clientSocket, lisp);
        executor.submit(socketRepl);
      } catch (IOException e) {
        if (running) {
          System.err.println("Error accepting connection: " + e.getMessage());
        }
        // If not running, the socket was closed intentionally during shutdown
      }
    }
  }

  /**
   * Returns the port this server is listening on.
   *
   * @return the TCP port number
   */
  public int getPort() {
    return port;
  }

  /**
   * Returns whether the server is currently running.
   *
   * @return true if the server is running
   */
  public boolean isRunning() {
    return running;
  }
}
