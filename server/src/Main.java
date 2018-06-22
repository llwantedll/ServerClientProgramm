import com.sun.jmx.remote.internal.ArrayQueue;
import tcp.TCPConnection;
import tcp.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Main implements TCPConnectionListener {

    private final static int BUFF_MSGs = 10;

    public static void main(String[] args) {
        new Main();
    }

    private ArrayList<TCPConnection> connections = new ArrayList<>();
    private ArrayQueue<String> lastMsg = new ArrayQueue<>(BUFF_MSGs);

    private Main(){
        try (ServerSocket serverSocket = new ServerSocket(4647)){
            System.out.println("IP = "+serverSocket.getInetAddress()+":"+serverSocket.getLocalPort());
            while (true){
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e){
                    System.out.println(e+"33");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e+"34");
        }
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        for (int i = 0; i < lastMsg.size(); i++) {
            tcpConnection.sendString(lastMsg.get(i));
        }
        sendToAllConnections(tcpConnection.getString() + " connected");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        sendToAllConnections(value);
        if(lastMsg.size()==BUFF_MSGs)
            lastMsg.remove(0);
        lastMsg.add(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnections(tcpConnection.getString() + " disconnected");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("Exception "+e);
    }

    public void sendToAllConnections(String value){
        for (TCPConnection connection : connections) {
            connection.sendString(value);
        }
    }
}
