package de.prob.clistarter.client;

import de.prob.clistarter.MessageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class CliClient {

    private static final Logger logger = LoggerFactory.getLogger(CliClient.class);

    private Socket socket = null;

    @Inject
    public CliClient(){}

    public void connect(String serverName, int serverPort) {
        System.out.println("Establishing connection. Please wait ...");
        try {
            socket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + socket);
        } catch(UnknownHostException e1) {
            logger.error("Host unknown: " + e1.getMessage());
        } catch(IOException e2) {
            logger.error(e2.getMessage());
        }

    }

    private String readFromServer() {
        while(true) {
            try {
                String result = MessageReader.read(socket);
                if(!result.isEmpty()) {
                    System.out.println("Receive result: " + result);
                    return result;
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
                return "";
            }
        }
    }

    private void readKeyAndPort() {
        String cliKey = "";
        int cliPort = 0;
        while(true) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while(br.ready()) {
                    String[] str = br.readLine().split(" ");
                    String prefix = str[0];
                    if("Key:".equals(prefix)) {
                        cliKey = str[1];
                    } else if("Port:".equals(prefix)) {
                        cliPort = Integer.parseInt(str[1]);
                        System.out.println("Connected with Key: " + cliKey + " , Port: " + cliPort);
                        return;
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
                return;
            }
        }
    }

    public void requestCLI() {
        try {
            String message = "Request CLI";
            DataOutputStream streamOut = new DataOutputStream(socket.getOutputStream());
            streamOut.write(message.getBytes());
            streamOut.writeBytes("\n");
            readKeyAndPort();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void interruptCLI() {
        try {
            String message = "Interrupt CLI";
            DataOutputStream streamOut = new DataOutputStream(socket.getOutputStream());
            streamOut.write(message.getBytes());
            streamOut.writeBytes("\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public void shutdownCLI() {
        try {
            String message = "Shutdown CLI";
            DataOutputStream streamOut = new DataOutputStream(socket.getOutputStream());
            streamOut.write(message.getBytes());
            streamOut.writeBytes("\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public String sendMessage(String message) {
        try {
            DataOutputStream streamOut = new DataOutputStream(socket.getOutputStream());
            streamOut.write(message.getBytes());
            String result = readFromServer();
            System.out.println("Result received: " + result);
            return result;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return "";
    }

}
