package no.gruppe15.controlpanel;

import static no.gruppe15.greenhouse.GreenhouseSimulator.PORT_NUMBER;
import static no.gruppe15.run.ControlPanelStarter.SERVER_HOST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import no.gruppe15.gui.controlpanel.CommandLineControlPanel;
import no.gruppe15.tools.Logger;

/**
 * This class represents a TCP - client. It should send and receive messages
 * between the server.
 *
 * @author Matti Kjellstadli, Håkon Svensen Karlsen, Di Xie, Adrian Faustino Johansen
 * @version 09.11.2023
 */
public class ControlPanelSocket implements CommunicationChannel {

  private final ControlPanelLogic logic;
  private Socket socket;
  private BufferedReader socketReader;
  private PrintWriter socketWriter;
  private boolean isConnected = false;
  private static final String ALGORITHM = "AES";
  private boolean secure;

  /**
   * Creates an instance of ControlPanelSocket.
   *
   * @param logic The application logic class.
   */
  public ControlPanelSocket(ControlPanelLogic logic, boolean secure) {
    this.logic = logic;
    this.secure = secure;
  }

  /**
   * This method should send a command to a specific actuator
   *
   * @param nodeId     ID of the node to which the actuator is attached
   * @param actuatorId Node-wide unique ID of the actuator
   * @param isOn       When true, actuator must be turned on; off when false.
   */
  @Override
  public void sendActuatorChange(int actuatorId, int nodeId, boolean isOn) {
    Logger.info("Sending command to actuator " + nodeId + " on node " + actuatorId);
    String on = isOn ? "0" : "1";
    String command = actuatorId + ", " + nodeId + ", " + on;

    try {
      socketWriter.println(command);
      String response = socketReader.readLine();
      Logger.info(response);
    } catch (IOException e) {
      Logger.error("Error sending command to actuator " + actuatorId + " on node " + nodeId + ": " +
          e.getMessage());
    }
  }

  /**
   * Opens a TCP socket connection to the specified server.
   *
   * @return true if the socket connection was successfully established, false otherwise.
   */
  @Override
  public boolean open() {
    try {
      socket = new Socket(SERVER_HOST, PORT_NUMBER);
      socketWriter = new PrintWriter(socket.getOutputStream(), true);
      socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      if (secure) {
        startEncryption();
      }

      Logger.info("Successfully connected to: " + SERVER_HOST + ":" + PORT_NUMBER);

      continuousSensorUpdate();
      getNodes();
      isConnected = true;
      return true;
    } catch (IOException e) {
      Logger.error("Could not connect to server: " + e.getMessage());
      return false;
    }
  }

  private SecretKeySpec generateSecretKey() {
    String keyString = "MySecretKey12345";
    byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
    return new SecretKeySpec(keyBytes, ALGORITHM);
  }

  public void startEncryption() {
    try {
      SecretKeySpec secretKey = generateSecretKey();

      Cipher encryptCipher = Cipher.getInstance(ALGORITHM);
      Cipher decryptCipher = Cipher.getInstance(ALGORITHM);
      encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
      decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);

      socketWriter = new PrintWriter(new CipherOutputStream
          (socket.getOutputStream(), encryptCipher), true);
      socketReader = new BufferedReader(new InputStreamReader
          (new CipherInputStream(socket.getInputStream(), decryptCipher)));

      Logger.info("Encryption setup completed successfully");
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
             IOException e) {
      Logger.error("Error during encryption setup: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }


  public void openCommandLine() {
    open();
    Scanner input = new Scanner(System.in);
    Thread commandThread = new Thread(() -> startCommandControl(input));
    commandThread.start();
  }

  private void startCommandControl(Scanner input) {
    CommandLineControlPanel controlPanel = new CommandLineControlPanel(this);
    controlPanel.startCommandControl(input);
  }

  /**
   * This method should close the connection to the server.
   */
  public void close() {
    try {
      if (isConnected) {
        socket.close();
        socketWriter.close();
        socketReader.close();
        Logger.info(
            "Connection with client: " + SERVER_HOST + ":" + PORT_NUMBER + " has been closed");
      }
    } catch (IOException e) {
      Logger.error("Could not close connection: " + e.getMessage());
    }
  }

  /**
   * This method should get all nodes from server, and add them to
   * the controlPanel.
   */
  public void getNodes() {
    socketWriter.println("getNodes");
    Logger.info("Requesting nodes from server...");
    String nodes;
    try {
      nodes = socketReader.readLine();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    String[] nodeList = nodes.split("/");
    for (String node : nodeList) {
      logic.onNodeAdded(logic.createSensorNodeInfoFrom(node));
    }
    Logger.info("Nodes loaded");
  }

  /**
   * This method should update the sensors continually.
   */
  public void updateSensorData() {
    socketWriter.println("updateSensor");
    String sensors = "";
    try {
      sensors = socketReader.readLine();
    } catch (IOException e) {
      Logger.info("Stopping sensor reading");
    }
    logic.sensorStringSplitter(sensors);
  }

  /**
   * This method sends requests to the server for sensor updates every 2 seconds.
   */
  public void continuousSensorUpdate() {
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        updateSensorData();
      }
    }, 0, 2000);
  }

}
