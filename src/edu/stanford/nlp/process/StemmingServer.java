package edu.stanford.nlp.process;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;

import edu.stanford.nlp.io.EncodingPrintWriter;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.ling.WordTag;
import edu.stanford.nlp.process.Morphology;

public class StemmingServer {
  private final String charset;
  private final ServerSocket listener;

  /**
   * Creates a new stemming server on the specified port.
   *
   * @param port the port this NERServer listens on.
   * @param charset The character set for encoding Strings over the socket stream, e.g., "utf-8"
   * @throws java.io.IOException If there is a problem creating a ServerSocket
   */
  public StemmingServer(int port, String charset)
    throws IOException
  {
    listener = new ServerSocket(port);
    this.charset = charset;
  }

  /**
   * Runs this stemming server.
   */
  @SuppressWarnings({"InfiniteLoopStatement", "ConstantConditions", "null"})
  public void run() {
    Socket client = null;
    while (true) {
      try {
        client = listener.accept();
        new Session(client);
      } catch (Exception e1) {
        System.err.println("StemmingServer: couldn't accept");
        e1.printStackTrace(System.err);
        try {
          client.close();
        } catch (Exception e2) {
          System.err.println("StemmingServer: couldn't close client");
          e2.printStackTrace(System.err);
        }
      }
    }
  }

  /**
   * A single user session, accepting one request, processing it, and
   * sending back the results.
   */
  private class Session extends Thread {
    private final Socket client;
    private final BufferedReader in;
    private PrintWriter out;

    private Session(Socket socket) throws IOException {
      client = socket;
      in = new BufferedReader(new InputStreamReader(client.getInputStream(), charset));
      out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), charset));
      start();
    }

    /**
     * Runs this session by reading a string, stemming it, and writing
     * back the result.  The input should be a single line (no embedded
     * newlines), which represents a whole sentence or document.
     */
    @Override
    public void run() {
      try {
        String input = in.readLine();
        if (! (input == null)) {
          StringBuilder result = new StringBuilder();
          String[] words = input.split("\\s+");
          for (String word : words) {
            String[] split_word = word.split("/");
            result.append(split_word[0] + "/");
            result.append(Morphology.stemStatic(WordTag.valueOf(word)));
            result.append(" ");
          }

          out.print(result.toString());
          out.flush();
        }
        close();
      } catch (IOException e) {
        System.err.println("StemmingServer:Session: couldn't read input or error running Stemming");
        e.printStackTrace(System.err);
      } catch (NullPointerException npe) {
        System.err.println("StemmingServer:Session: connection closed by peer");
        npe.printStackTrace(System.err);
      }
    }

    /**
     * Terminates this session gracefully.
     */
    private void close() {
      try {
        in.close();
        out.close();
        client.close();
      } catch (Exception e) {
        System.err.println("StemmingServer:Session: can't close session");
        e.printStackTrace();
      }
    }

  } // end class Session

  private static class TaggerClient {
    private TaggerClient() {}

    private static void communicateWithStemmingServer(String host, int port, String charset) throws IOException {
      if (host == null) {
        host = "localhost";
      }

      BufferedReader stdIn = new BufferedReader(
              new InputStreamReader(System.in, charset));
      System.err.println("Input some text and press RETURN to stem it, or just RETURN to finish.");

      for (String userInput; (userInput = stdIn.readLine()) != null && ! userInput.matches("\\n?"); ) {
        try {
          Socket socket = new Socket(host, port);
          PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), charset), true);
          BufferedReader in = new BufferedReader(new InputStreamReader(
                  socket.getInputStream(), charset));
          PrintWriter stdOut = new PrintWriter(new OutputStreamWriter(System.out, charset), true);
          // send material to NER to socket
          out.println(userInput);
          // Print the results of NER

          stdOut.println(in.readLine());
          while (in.ready()) {
            stdOut.println(in.readLine());
          }
          in.close();
          socket.close();
        } catch (UnknownHostException e) {
          System.err.print("Cannot find host: ");
          System.err.println(host);
          return;
        } catch (IOException e) {
          System.err.print("I/O error in the connection to: ");
          System.err.println(host);
          return;
        }
      }
      stdIn.close();
    }
  } // end static class NERClient


  private static final String USAGE = "Usage: StemmingServer -port portNumber";

  /**
   * Starts this server on the specified port.  The classifier used can be
   * either a default one stored in the jar file from which this code is
   * invoked or you can specify it as a filename or as another classifier
   * resource name, which must correspond to the name of a resource in the
   * /classifiers/ directory of the jar file.
   * <p>
   * Usage: <code>java edu.stanford.nlp.process.StemmingServer -port portNumber</code>
   *
   * @param args Command-line arguments (described above)
   * @throws Exception If file or Java class problems with serialized classifier
   */
  @SuppressWarnings({"StringEqualsEmptyString"})
  public static void main (String[] args) throws Exception {
    if (args.length == 0) {
      System.err.println(USAGE);
      return;
    }
    // Use both Properties and TaggerConfig.  It's okay.
    Properties props = StringUtils.argsToProperties(args);
    String client = props.getProperty("client");

    String portStr = props.getProperty("port");
    if (portStr == null || portStr.equals("")) {
      System.err.println(USAGE);
      return;
    }
    int port = 0;
    try {
      port = Integer.parseInt(portStr);
    } catch (NumberFormatException e) {
      System.err.println("Non-numerical port");
      System.err.println(USAGE);
      System.exit(1);
    }

    if (client != null && ! client.equals("")) {
      // run a test client for illustration/testing
      String host = props.getProperty("host");
      String encoding = props.getProperty("encoding");
      if (encoding == null || "".equals(encoding)) {
        encoding = "utf-8";
      }
      TaggerClient.communicateWithStemmingServer(host, port, encoding);
    } else {
      new StemmingServer(port, "utf-8").run();
    }
  }

}
