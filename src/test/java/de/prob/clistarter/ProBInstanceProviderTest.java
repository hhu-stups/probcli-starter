package de.prob.clistarter;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProBInstanceProviderTest {

	@Test
	public void testExtractCliInformation() {
		ProBInstanceProvider factory = ProBCliStarter.getInjector().getInstance(
				ProBInstanceProvider.class);

		String text = "No file to process\nStarting Socket Server\n"
				+ "Application Path: /Users/bendisposto/.prob\nPort: 61013\n"
				+ "probcli revision: $Rev$\nuser interrupt reference id: 57124\n"
				+ "-- starting command loop --";

		BufferedReader reader = new BufferedReader(new StringReader(text));

		Map<Class<? extends AbstractCliPattern<?>>, AbstractCliPattern<?>> info = factory
				.extractCliInformation(reader);

		assertNotNull(info.get(InterruptRefPattern.class));

		assertNotNull(info.get(PortPattern.class));

	}
}
