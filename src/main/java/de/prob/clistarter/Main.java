package de.prob.clistarter;

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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

public class Main {
	
	private static Injector injector = null;
	
	
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
		ProBInstance instance = getInjector().getInstance(ProBInstance.class);
		System.out.println(instance);
		try {
			InputStream is = client.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			DataOutputStream os = new DataOutputStream(client.getOutputStream());
			String body = "";
			while(br.ready()) {
				body += br.readLine() + "\n";
			}			
			System.out.println(body);
			
			os.writeBytes("TEST");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getVersion() {
		return buildProperties.getProperty("version");
	}
	
	public static void main(String args[]) {
		ServerSocket server = null;
		try {
			server = new ServerSocket(4444);
			while(true) {
				Socket client = server.accept();
				handleConnection(client);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return;
		}

	}
	
}
