package de.prob.clistarter;

import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ProBInstance {

	private final Thread thread;

	private volatile boolean shuttingDown = false;

	final Logger logger = LoggerFactory.getLogger(ProBInstance.class);
	private final Process process;

	private ProBConnection connection;

	private String[] interruptCommand;

	private AtomicInteger processCounter;

	public ProBInstance(final Process process, final BufferedReader stream, final Long userInterruptReference,
			final ProBConnection connection, final String home, final OsSpecificInfo osInfo,
			AtomicInteger processCounter) {
		this.process = process;
		this.connection = connection;
		this.processCounter = processCounter;
		final String command = home + osInfo.getUserInterruptCmd();
		interruptCommand = new String[] { command, Long.toString(userInterruptReference) };
		thread = makeOutputPublisher(stream);
		thread.start();
	}

	private Thread makeOutputPublisher(final BufferedReader stream) {
		return new Thread(new ConsoleListener(this, stream, logger),
				String.format("ProB Output Logger for instance %x", this.hashCode()));
	}

	public void shutdown() {
		if (!shuttingDown) {
			processCounter.decrementAndGet();
		}
		shuttingDown = true;
		try {
			if (thread.isAlive()) {
				thread.interrupt();
			}
			connection.disconnect();
		} finally {
			process.destroy();
		}
	}

	public void sendInterrupt() {
		try {
			if (connection.isBusy()) {
				logger.info("sending interrupt signal");
				Runtime.getRuntime().exec(interruptCommand);
			} else {
				logger.info("ignoring interrupt signal because the connection is not busy");
			}
		} catch (IOException e) {
			logger.warn("calling the send_user_interrupt command failed", e);
		}
	}

	public boolean isShuttingDown() {
		return shuttingDown;
	}

	public ProBConnection getConnection() {
		return connection;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(ProBInstance.class).addValue(connection).toString();
	}

}
