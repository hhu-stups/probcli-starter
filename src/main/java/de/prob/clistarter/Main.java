package de.prob.clistarter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
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
	
	private static void handleConnection(Socket client) {
		handleRequestsOfClient(client);
	}

	private static void handleRequestsOfClient(Socket client)  {
		try {
			InputStream is = client.getInputStream();
			ProBInstance instance = null;
			while(true) {
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				StringBuilder sb = new StringBuilder();

				if(br.ready()) {
				    sb.append(br.readLine());
                }

				String message = sb.toString();
				if(!message.isEmpty()) {
                    System.out.println("Receive message: " + message);
                } else {
				    continue;
                }

                DataOutputStream os = new DataOutputStream(client.getOutputStream());
				if("Request CLI".equals(message)) {
					instance = createCLI(os);
				} else if("Shutdown CLI".equals(message)) {
					instance.shutdown();
					System.out.println("Shutdown CLI");
				} else if("Interrupt CLI".equals(message)) {
					instance.sendInterrupt();
					System.out.println("Interrupt CLI");
				} else {
					if(instance == null) {
						System.out.println("CONTINUE");
						continue;
					}
					String result = instance.send(message);
					System.out.println("Send result: " + result);
					os.writeBytes(result);
					os.writeBytes("\n");
                    //os.writeBytes(CRLF);
					os.flush();
				}
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private static ProBInstance createCLI(DataOutputStream os) {
		try {
			ProBInstance instance = getInjector().getInstance(ProBInstance.class);
			ProBConnection connection = instance.getConnection();

			System.out.println("Provide Key: " + connection.getKey());
			System.out.println("Provide Port: " + connection.getPort());

			os.writeBytes("Key: " + connection.getKey() + "\n" + "Port: " + connection.getPort() + "\n");
            os.flush();
			return instance;
		} catch(IOException e) {
			logger.error(e.getMessage());
			return null;
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
					Thread thread = new Thread(() -> handleConnection(client));
					thread.start();
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		});
		t.start();
	}
	
}
