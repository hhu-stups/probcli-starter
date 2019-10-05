package de.prob.clistarter;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;

public class ProBConnection {

	private static final int BUFFER_SIZE = 1024;

	private final Logger logger = LoggerFactory.getLogger(ProBConnection.class);
	private volatile boolean shutingDown;
	private volatile boolean busy;
	private final String key;
	private final int port;
	private InetAddress address;

	public ProBConnection(final String key, final int port) {
		this.key = key;
		this.port = port;
		this.address = null;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(ProBConnection.class).add("key", key)
				.add("port", port).toString();
	}

	public void connect() throws IOException {
		logger.debug("Connecting to port {} using key {}", port, key);
		this.address = InetAddress.getByName(null);
		logger.debug("Connected");
	}

	public boolean isBusy() {
		return busy;
	}

	public void disconnect() {
		shutingDown = true;
	}

	public int getPort() {
		return port;
	}

	public String getAddress() {
		return address.getHostAddress();
	}
}
