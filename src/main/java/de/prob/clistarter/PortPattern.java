package de.prob.clistarter;

import java.util.regex.Matcher;

import de.prob.clistarter.exception.CliError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link AbstractCliPattern} looks for a network port number where the
 * executable listens for commands.
 * 
 * If no port number is found, {@link #notifyNotFound()} throws a
 * {@link CliError}
 * 
 * @author plagge
 */
class PortPattern extends AbstractCliPattern<Integer> {
	private static final Logger logger = LoggerFactory.getLogger(PortPattern.class);

	private int port;

	PortPattern() {
		super("Port: (\\d+)$");
	}

	@Override
	protected void setValue(final Matcher matcher) {
		port = Integer.parseInt(matcher.group(1));
		logger.info("Server has started and listens on port {}", port);
	}

	/**
	 * Returns the port number.
	 */
	@Override
	public Integer getValue() {
		return port;
	}

	@Override
	public void notifyNotFound() {
		logger.error("Could not determine port of ProB server");
	}

	@Override
	public boolean notFoundIsFatal() {
		return true;
	}

}
