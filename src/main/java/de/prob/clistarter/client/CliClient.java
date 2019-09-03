package de.prob.clistarter.client;

import de.prob.clistarter.ProBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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
    private DataInputStream console = null;

    private String cliKey;
    private int cliPort;

    @Inject
    public CliClient(){}

    public void connect(String serverName, int serverPort) {
        System.out.println("Establishing connection. Please wait ...");
        try {
            socket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + socket);
            startConnectionWithServer();
        } catch(UnknownHostException e1) {
            logger.error("Host unknown: " + e1.getMessage());
            return;
        } catch(IOException e2) {
            logger.error(e2.getMessage());
            return;
        }

    }

    private String readFromServer() {
        if(cliPort == 0 || cliKey == null) {
            readKeyAndPort();
        }
        while(true) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder sb = new StringBuilder();
                while(br.ready()) {
                    sb.append(br.readLine());
                    sb.append("\n");
                }
                String result = sb.toString();
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
        while (true) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while(br.ready()) {
                    String[] str = br.readLine().split(" ");
                    String prefix = str[0];
                    if("Key:".equals(prefix)) {
                        cliKey = str[1];
                    } else if("Port:".equals(prefix)) {
                        cliPort = Integer.parseInt(str[1]);
                    }
                }
                if(cliKey != null && cliPort != 0) {
                    connectWithCli();
                    break;
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
                return;
            }
        }
    }

    private void connectWithCli() {
        try {
            ProBConnection connection = new ProBConnection(cliKey, cliPort);
            connection.connect();
            System.out.println("Connected with CLI: " + connection);
        } catch(UnknownHostException e1) {
            logger.error("Host unknown: " + e1.getMessage());
            return;
        } catch(IOException e2) {
            logger.error(e2.getMessage());
            return;
        }
    }

    public void startConnectionWithServer() {
        console = new DataInputStream(System.in);
    }

    public void requestCLI() {
        try {
            String message = "Request CLI";
            DataOutputStream streamOut = new DataOutputStream(socket.getOutputStream());
            streamOut.write(message.getBytes());
            streamOut.writeBytes("\n");
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
