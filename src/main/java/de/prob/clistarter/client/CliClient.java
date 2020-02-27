package de.prob.clistarter.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import com.google.inject.Singleton;

import de.prob.clistarter.ProBConnection;

import java.io.IOException;
import java.net.UnknownHostException;

@Singleton
public class CliClient {

	private static final Logger logger = LoggerFactory.getLogger(CliClient.class);

	private ZContext context;
	
	private Socket socket = null;
	
	private String serverName;
	
	private int serverPort;
	
	private int cliPort;
	
	private ProBConnection connection;
	
	
	public void connect(String serverName, int serverPort) {
		this.context = new ZContext();
		this.serverName = serverName;
		this.serverPort = serverPort;
		requestCLI();

	}

	private void requestCLI() {
		socket = context.createSocket(SocketType.REQ);
		socket.connect(String.format("tcp://%s:%d", serverName, serverPort));
		System.out.println("Establishing connection. Please wait ...");
		System.out.println("Connected: " + serverName + ", Port:" + serverPort);
		socket.send("Request CLI");
		System.out.println("A");
		String msg = socket.recvStr();
		try {
			System.out.println("B");
			String[] lines = msg.split("\n");
			String key = lines[0].split(" ")[1];
			cliPort = Integer.parseInt(lines[1].split(" ")[1]);
			connection = new ProBConnection(key, cliPort);
			connection.connect(serverName);
			System.out.println("C");
			System.out.println("Connected with CLI socket: " + key + ", Port: " + cliPort);
		} catch (UnknownHostException e1) {
			logger.error("Host unknown: ", e1);
			return;
		} catch (IOException e2) {
			logger.error("", e2);
			return;
		}
	}

	public void interruptCLI() {
		socket.send("Interrupt CLI:" + cliPort);
		System.out.println("Send Interrupt CLI: " + cliPort);
	}

	public void shutdownCLI() {
		socket.send("Shutdown CLI:" + cliPort);
		System.out.println("Send Shutdown CLI: " + cliPort);
		context.close();
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
