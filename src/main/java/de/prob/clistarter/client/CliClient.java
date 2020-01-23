package de.prob.clistarter.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class CliClient {

	private static final int BUFFER_SIZE = 1024;

	private static final Logger logger = LoggerFactory.getLogger(CliClient.class);

	private Socket socket = null;

	private Socket cliSocket = null;

	private volatile boolean busy;

	@Inject
	public CliClient() {
	}

	public void connect(String serverName, int serverPort) {
		System.out.println("Establishing connection. Please wait ...");
		try {
			socket = new Socket(serverName, serverPort);
			System.out.println("Connected: " + socket);
			requestCLI();
		} catch (UnknownHostException e1) {
			logger.error("Host unknown: " + e1.getMessage());
		} catch (IOException e2) {
			logger.error(e2.getMessage());
		}

	}

	private String readFromCli() {
		try {
			String result = readAnswer(cliSocket);
			return result;
		} catch (IOException e) {
			logger.error(e.getMessage());
			return "";
		}
	}

	protected String readAnswer(Socket cliSocket) throws IOException {
		final StringBuilder result = new StringBuilder();
		final byte[] buffer = new byte[BUFFER_SIZE];
		boolean done = false;

		while (!done) {
			/*
			 * It might be necessary to check for inputStream.available() > 0.
			 * Or add some kind of timer to prevent the thread blocks forever.
			 * See task#102
			 */
			busy = true;
			int count = cliSocket.getInputStream().read(buffer);
			busy = false; // as soon as we read something, we know that the
			// Prolog has been processed and we do not want to
			// allow interruption
			if (count > 0) {
				final byte length = 1;

				// check for end of transmission (i.e. last byte is 1)
				if (buffer[count - length] == 1) {
					done = true;
					count--; // remove end of transmission marker
				}

				// trim white spaces and append
				// instead of removing the last byte trim is used, because on
				// windows prob uses \r\n as new line.
				String s = new String(buffer, 0, count, "utf8");
				result.append(s.replace("\r", "").replace("\n", ""));
			} else {
				done = true;
			}
		}
		return result.length() > 0 ? result.toString() : null;
	}

	private void readKeyAndPort() {
		int cliPort = 0;
		String cliAddress = "";
		try {
			String answer = readAnswer(socket);
			String[] str = answer.split(",")[1].split(" ");
			cliPort = Integer.parseInt(str[1]);
			cliSocket = new Socket(cliAddress, cliPort);
			System.out.println("Connected with CLI socket: " + cliAddress + ", Port: " + cliPort);
		} catch (UnknownHostException e1) {
			logger.error("Host unknown: ", e1);
		} catch (IOException e2) {
			logger.error("", e2);
		}
	}

	private void requestCLI() {
		try {
			String message = "Request CLI";
			DataOutputStream streamOut = new DataOutputStream(socket.getOutputStream());
			streamOut.write(message.getBytes());
			streamOut.writeBytes("\n");
			readKeyAndPort();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public void interruptCLI() {
		try {
			String message = "Interrupt CLI";
			DataOutputStream streamOut = new DataOutputStream(socket.getOutputStream());
			streamOut.write(message.getBytes());
			streamOut.writeBytes("\n");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public void shutdownCLI() {
		try {
			String message = "Shutdown CLI";
			DataOutputStream streamOut = new DataOutputStream(socket.getOutputStream());
			streamOut.write(message.getBytes());
			streamOut.writeBytes("\n");
			socket.getOutputStream().close();
			cliSocket.getOutputStream().close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public String sendMessage(String message) {
		try {
			DataOutputStream streamOut = new DataOutputStream(cliSocket.getOutputStream());
			streamOut.write(message.getBytes());
			streamOut.writeBytes("\n");
			String result = readFromCli();
			return result;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return "";
	}

	public boolean isBusy() {
		return busy;
	}
}
