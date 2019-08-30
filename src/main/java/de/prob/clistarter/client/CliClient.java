package de.prob.clistarter.client;

import de.prob.clistarter.ProBConnection;
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
    private DataInputStream console = null;
    private DataOutputStream streamOut = null;

    private String cliKey;
    private int cliPort;

    public CliClient(String serverName, int serverPort) {
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
        Thread t = new Thread(() -> {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                logger.error(e.getMessage());
                return;
            }
            readFromServer(br);
        });
        t.start();
    }

    private void readFromServer(BufferedReader br) {
        while (true) {
            try {
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
            Thread t = new Thread(() -> {
                readFromCli(connection);
            });
            t.start();
        } catch(UnknownHostException e1) {
            logger.error("Host unknown: " + e1.getMessage());
            return;
        } catch(IOException e2) {
            logger.error(e2.getMessage());
            return;
        }
    }

    private void readFromCli(ProBConnection connection) {
        while (true) {
            try {
                //TODO
                String str = connection.send("");
            } catch (IOException e) {
                logger.error(e.getMessage());
                return;
            }
        }
    }

    public void startConnectionWithServer() throws IOException {
        console = new DataInputStream(System.in);
        streamOut = new DataOutputStream(socket.getOutputStream());
    }

    public void requestCLI() throws IOException {
        streamOut.writeBytes("Request CLI");
    }

}
