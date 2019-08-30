package de.prob.clistarter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.clistarter.client.CliClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Main {
	
	private static Injector injector = null;

	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	
	
	private static final Properties buildProperties;
	
	static {
		buildProperties = new Properties();
		final InputStream is = Main.class.getResourceAsStream("build.properties");
		if (is == null) {
			throw new IllegalStateException("Build properties not found, this should never happen!");
		} else {
			try (final Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
				buildProperties.load(r);
			} catch (IOException e) {
				throw new UncheckedIOException("IOException while loading build properties, this should never happen!", e);
			}
		}
	}
	
	
	public static synchronized Injector getInjector() {
		if (injector == null) {
			injector = Guice.createInjector(Stage.PRODUCTION, new ModuleCli());
		}
		return injector;
	}
	
	private static void handleConnection(Socket client) {
		handleRequestsOfClient(client);
	}

	private static void handleRequestsOfClient(Socket client)  {
		try {
			InputStream is = client.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while(true) {
				StringBuilder messageBuilder = new StringBuilder();
				while (br.ready()) {
					messageBuilder.append((char) br.read());
				}
				String message = messageBuilder.toString();
				if (!message.isEmpty()) {
					System.out.println("Receive message: " + message);
				}


				if("Request CLI".equals(message)) {
					createCLI(client);
				}
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private static void createCLI(Socket client) {
		try {
			DataOutputStream os = new DataOutputStream(client.getOutputStream());
			ProBInstance instance = getInjector().getInstance(ProBInstance.class);
			ProBConnection connection = instance.getConnection();

			System.out.println("Provide Key: " + connection.getKey());
			System.out.println("Provide Port: " + connection.getPort());

			os.writeBytes("Key: " + connection.getKey());
			os.writeBytes("\n");
			os.writeBytes("Port: " + connection.getPort());
			os.writeBytes("\n");
		} catch(IOException e) {
			logger.error(e.getMessage());
		}
	}

	public static String getVersion() {
		return buildProperties.getProperty("version");
	}
	
	public static void main(String[] args) throws IOException {
		Thread t = new Thread(() -> {
			try {
				ServerSocket server = new ServerSocket(4444);
				while (true) {
					Socket client = server.accept();
					handleConnection(client);
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		});
		t.start();

		CliClient client = new CliClient("localhost", 4444);
		client.start();
		client.requestCLI();
	}
	
}
