package de.prob.clistarter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MessageReader {

	public static String read(Socket socket) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		StringBuilder sb = new StringBuilder();

		while (br.ready()) {
			sb.append(br.readLine());
			if (br.ready()) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

}
