package de.prob.clistarter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Singleton
public class ProBCliStarter {

	private static Injector injector = null;

	private static final Logger logger = LoggerFactory.getLogger(ProBCliStarter.class);

	private static final Properties buildProperties;
	
	private final ZContext context;
	
	private final Map<Integer, ProBInstance> instances;
	
	private Socket serverSocket;

	static {
		buildProperties = new Properties();
		final InputStream is = ProBCliStarter.class.getResourceAsStream("build.properties");
		if (is == null) {
			throw new IllegalStateException("Build properties not found, this should never happen!");
		} else {
			try (final Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
				buildProperties.load(r);
			} catch (IOException e) {
				throw new UncheckedIOException("IOException while loading build properties, this should never happen!",
						e);
			}
		}
	}
	
	public ProBCliStarter() {
		this.context = new ZContext();
		this.instances = new HashMap<>();
		Thread thread = new Thread(() -> {
			serverSocket = context.createSocket(SocketType.REP);
			serverSocket.bind("tcp://*:11312");
			while(!Thread.currentThread().isInterrupted()) {
				handleRequestsOfClient();
			}
			context.close();
			System.out.println("CLI Server terminated");
		});
		thread.start();
	}

	public static synchronized Injector getInjector() {
		if (injector == null) {
			injector = Guice.createInjector(Stage.PRODUCTION, new ModuleCli());
		}
		return injector;
	}

	private void handleRequestsOfClient() {
		try {
			while (true) {
				String message = serverSocket.recvStr();
				String[] messageSplitted = message.split(":");
				String messagePrefix = messageSplitted[0];
				switch (messagePrefix) {
					case "Request CLI":
						handleCLIRequest();
						break;
					case "Shutdown CLI": {
						int port = Integer.parseInt(messageSplitted[messageSplitted.length - 1]);
						ProBInstance instance = instances.get(port);
						handleCLIShutdown(instance);
						return;
					}
					case "Interrupt CLI": {
						int port = Integer.parseInt(messageSplitted[messageSplitted.length - 1]);
						ProBInstance instance = instances.get(port);
						handleCLIInterrupt(instance);
						break;
					}
					default:
						break;
				}
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private void handleCLIRequest() throws IOException {
		ProBInstance instance = getInjector().getInstance(ProBInstance.class);
		ProBConnection connection = instance.getConnection();
		serverSocket.send("Key: " + connection.getKey() + "\n" + "Port: " + connection.getPort());
		System.out.println("Provide Key: " + connection.getKey());
		System.out.println("Provide Port: " + connection.getPort());
		instances.put(connection.getPort(), instance);
	}

	private void handleCLIShutdown(ProBInstance instance) {
		serverSocket.send("OK");
		instance.shutdown();
		System.out.println("Shutdown CLI: " + instance);
	}

	private void handleCLIInterrupt(ProBInstance instance) {
		serverSocket.send("OK");
		instance.sendInterrupt();
		System.out.println("Interrupt CLI: " + instance);
	}

	public static String getVersion() {
		return buildProperties.getProperty("version");
	}

	public static void main(String[] args) {
		new ProBCliStarter();
	}

}
