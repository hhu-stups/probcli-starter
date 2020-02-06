package de.prob.clistarter.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.prob.clistarter.ProBConnection;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class CliClient {

	private static final Logger logger = LoggerFactory.getLogger(CliClient.class);

	private Socket socket = null;
	
	private ProBConnection connection;


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

	private void readKeyAndPort() {
		int cliPort = 0;
		String key = "";
		String cliAddress = "";
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String[] str = br.readLine().split(" ");
			key = str[1];
			
			str = br.readLine().split(" ");
			cliAddress = socket.getInetAddress().getHostAddress();
			cliPort = Integer.parseInt(str[1]);
			connection = new ProBConnection(key, cliPort);
			connection.connect(cliAddress);
			System.out.println("Connected with CLI socket: " + key + ", Port: " + cliPort);
		} catch (UnknownHostException e1) {
			logger.error("Host unknown: ", e1);
			return;
		} catch (IOException e2) {
			logger.error("", e2);
			return;
		}

	}

	private void requestCLI() {
		try {
			String message = "Request CLI";
			PrintWriter streamOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
			streamOut.println(message);
			streamOut.flush();
			readKeyAndPort();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public void interruptCLI() {
		try {
			String message = "Interrupt CLI";
			PrintWriter streamOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
			streamOut.println(message);
			streamOut.flush();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public void shutdownCLI() {
		try {
			String message = "Shutdown CLI";
			PrintWriter streamOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
			streamOut.println(message);
			streamOut.flush();
			socket.getOutputStream().close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public String sendMessage(String message) {
		try {
			String result = connection.send(message);
			return result;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return "";
	}


}
