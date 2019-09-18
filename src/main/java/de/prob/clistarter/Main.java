package de.prob.clistarter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final String CRLF = "\r\n";;
	
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

	private static void handleRequestsOfClient(Socket client)  {
		try {
            ProBInstance instance = null;
			while(true) {
				String message = MessageReader.read(client);
				if (!message.isEmpty()) {
					System.out.println("Receive message: " + message);
				} else {
					continue;
				}

				if("Request CLI".equals(message)) {
                    instance = handleCLIRequest(client);
                } else {
                    handleCLI(instance, message, client);
                }
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private static ProBInstance handleCLIRequest(Socket client) throws IOException {
        DataOutputStream os = new DataOutputStream(client.getOutputStream());
        ProBInstance instance = getInjector().getInstance(ProBInstance.class);
        ProBConnection connection = instance.getConnection();

        System.out.println("Provide Key: " + connection.getKey());
        System.out.println("Provide Port: " + connection.getPort());

        os.writeBytes("Key: " + connection.getKey() + "\n" + "Port: " + connection.getPort() + "\n");
        os.flush();
        return instance;
    }

    private static void handleCLIShutdown(ProBInstance instance) {
        instance.shutdown();
        System.out.println("Shutdown CLI: " + instance);
    }

    private static void handleCLIInterrupt(ProBInstance instance) {
	    instance.sendInterrupt();
        System.out.println("Interrupt CLI: " + instance);
    }

    private static void handleMessage(ProBInstance instance, String message, Socket client) throws IOException {
        String result = instance.send(message);
        System.out.println("Send result: " + result);
        DataOutputStream os = new DataOutputStream(client.getOutputStream());
        os.writeBytes(result);
        os.writeBytes("\n");
        os.flush();
    }

	private static void handleCLI(ProBInstance instance, String message, Socket client) {
		try {
			switch(message) {
                case "Shutdown CLI":
                    handleCLIShutdown(instance);
                    break;
                case "Interrupt CLI":
                    handleCLIInterrupt(instance);
                    break;
                default:
                    handleMessage(instance, message, client);
                    break;
            }
		} catch(IOException e) {
			logger.error(e.getMessage());
		}
	}

	public static String getVersion() {
		return buildProperties.getProperty("version");
	}
	
	public static void main(String[] args) {
		Thread t = new Thread(() -> {
			try {
				ServerSocket server = new ServerSocket(4444);
				while (true) {
					Socket client = server.accept();
					Thread thread = new Thread(() -> handleRequestsOfClient(client));
					thread.start();
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		});
		t.start();
	}
	
}
