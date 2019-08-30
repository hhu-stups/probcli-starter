package de.prob.clistarter.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class CliClient {

    private static final Logger logger = LoggerFactory.getLogger(CliClient.class);

    private Socket socket = null;
    private DataInputStream console   = null;
    private DataOutputStream streamOut = null;

    public CliClient(String serverName, int serverPort) {
        System.out.println("Establishing connection. Please wait ...");
        try {
            socket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + socket);
            start();
        } catch(UnknownHostException e1) {
            logger.error("Host unknown: " + e1.getMessage());
            return;
        } catch(IOException e2) {
            logger.error(e2.getMessage());
            return;
        }
        Thread t = new Thread(() -> {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                logger.error(e.getMessage());
                return;
            }
            while (true) {
                try {
                    while(br.ready()) {
                        System.out.println(br.readLine());
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    return;
                }
            }
        });
        t.start();
    }

    public void start() throws IOException {
        console = new DataInputStream(System.in);
        streamOut = new DataOutputStream(socket.getOutputStream());
    }

    public void requestCLI() throws IOException {
        streamOut.writeBytes("Request CLI");
    }

}
