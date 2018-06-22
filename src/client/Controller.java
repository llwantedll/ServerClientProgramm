package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import tcp.TCPConnection;
import tcp.TCPConnectionListener;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller implements TCPConnectionListener, Closeable {
    @FXML TextField ipText;
    @FXML TextField portText;
    @FXML Label nickText;
    @FXML TextArea listSMS;
    @FXML TextField nickname;
    @FXML TextField textInput;
    private String nick = "";
    private final String addr = "192.168.0.104";
    private final int port = 4647;
    private TCPConnection connection;

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMsg("Connection "+tcpConnection.getString()+" Ready");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMsg(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMsg("Connection close");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMsg("Exception err" + e);
    }

    public synchronized void sendMSGEdit(ActionEvent actionEvent) {
        if(!textInput.getText().isEmpty())
        {
            sendMSG(nick + ": " + textInput.getText());
            textInput.setText("");
        }

    }

    public synchronized void sendMSG(String value){
        connection.sendString(value);
    }

    private synchronized void printMsg(String value){
        Platform.runLater(() -> listSMS.appendText(value +"\n"));
    }


    public void connect(String ip, int port) {
        Thread th = new Thread(() -> {
            try {
                if(connection==null) {
                    printMsg("Trying to connect to " + ip + ":" + port + " please wait...");
                    connection = new TCPConnection(Controller.this, ip, port);
                }
                else
                    printMsg("You've already connected to another chat");
            } catch (IOException e) {
                printMsg("Failed to connect to" + ip+":"+port);
            }
        });
        th.start();

    }

    public void nickAccept(ActionEvent actionEvent) {
        String checkString = nickname.getText();
        if(checkString.length()>=4 && checkString.length()<=16) {
            nick = checkString;
            nickText.setText(nick);
        }
    }

    public void addressConnect(ActionEvent actionEvent) {
        String ip = ipText.getText();
        int port;
        String regexp = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(ip);
        System.out.println(ip+":"+portText.getText());
        if(portText.getText().matches("[-+]?\\d+") && matcher.matches()) {
            port = Integer.parseInt(portText.getText());
            connect(ipText.getText(), port);
        }
    }

    public void standartConnect(ActionEvent actionEvent) {
        connect(addr, port);
    }

    public void disconnect(ActionEvent actionEvent) {

    }

    @Override
    public void close() throws IOException {
        connection.disconnect();
    }
}
