package de.prob.clistarter.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.prob.clistarter.ProBConnection;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

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
		while (true) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while (br.ready()) {
					String[] str = br.readLine().split(" ");
					String prefix = str[0];
					if ("Key:".equals(prefix)) {
						key = str[1];
					} else if ("Port:".equals(prefix)) {
						cliAddress = socket.getInetAddress().getHostAddress();
						cliPort = Integer.parseInt(str[1]);
						connection = new ProBConnection(key, cliPort);
						connection.connect(cliAddress);
						System.out.println("Connected with CLI socket: " + key + ", Port: " + cliPort);
						return;
					}
				}
			} catch (UnknownHostException e1) {
				logger.error("Host unknown: ", e1);
				return;
			} catch (IOException e2) {
				logger.error("", e2);
				return;
			}
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
