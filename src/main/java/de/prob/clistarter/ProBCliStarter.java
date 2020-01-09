package de.prob.clistarter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
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

@Singleton
public class ProBCliStarter {
	
	private static Injector injector = null;

	private static final Logger logger = LoggerFactory.getLogger(ProBCliStarter.class);
	
	private static final Properties buildProperties;

	private final ServerSocket server;

	private Thread thread;
	
	static {
		buildProperties = new Properties();
		final InputStream is = ProBCliStarter.class.getResourceAsStream("build.properties");
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

	public ProBCliStarter(int port) throws IOException {
		this.server = new ServerSocket(port);
	}

	public void start() {
		thread = new Thread(() -> {
			try {
				if(thread.isInterrupted()) {
					return;
				}
				while (true) {
					Socket client = server.accept();
					Thread thread = new Thread(() -> handleRequestsOfClient(client));
					thread.start();
				}
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		});
        thread.start();
	}

	public void shutdown() {
		try {
			server.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		thread.interrupt();
	}

	private void handleRequestsOfClient(Socket client)  {
		try {
            ProBInstance instance = null;
			while(true) {
				String message = MessageReader.read(client);
				if (!message.isEmpty()) {
					//System.out.println("Receive message: " + message);
				} else {
					continue;
				}

				if("Request CLI".equals(message)) {
                    instance = handleCLIRequest(client);
                } else {
                    handleCLI(instance, message);
                }
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private ProBInstance handleCLIRequest(Socket client) throws IOException {
        DataOutputStream os = new DataOutputStream(client.getOutputStream());
        ProBInstance instance = getInjector().getInstance(ProBInstance.class);
        ProBConnection connection = instance.getConnection();

        os.writeBytes("Address: " + connection.getAddress() + "\n" + "Port: " + connection.getPort() + "\n");
        os.flush();

		System.out.println("Provide Address: " + connection.getAddress());
		System.out.println("Provide Port: " + connection.getPort());
        return instance;
    }

    private void handleCLIShutdown(ProBInstance instance) {
        instance.shutdown();
        System.out.println("Shutdown CLI: " + instance);
    }

    private void handleCLIInterrupt(ProBInstance instance) {
	    instance.sendInterrupt();
        System.out.println("Interrupt CLI: " + instance);
    }

	private void handleCLI(ProBInstance instance, String message) {
		switch(message) {
			case "Shutdown CLI":
				handleCLIShutdown(instance);
				break;
			case "Interrupt CLI":
				handleCLIInterrupt(instance);
				break;
		}
	}

	public static String getVersion() {
		return buildProperties.getProperty("version");
	}
	
	public static void main(String[] args) {
		ProBCliStarter cliStarter;
		try {
			cliStarter = new ProBCliStarter(11312);
		} catch (IOException e) {
			logger.error(e.getMessage());
			return;
		}
		cliStarter.start();
	}
	
}
