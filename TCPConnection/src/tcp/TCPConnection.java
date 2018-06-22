package tcp;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.TimeoutException;

public class TCPConnection{
    private Thread rxThread;
    private Socket socket;
    private TCPConnectionListener tcpl;
    private BufferedReader in;
    private BufferedWriter out;

    public TCPConnection(TCPConnectionListener tcpl, String addr, int port) throws IOException {
        this(tcpl, new Socket(addr, port));
    }

    public TCPConnection(TCPConnectionListener tcpl, Socket socket) throws IOException {
        this.socket = socket;
        this.tcpl = tcpl;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        rxThread = new Thread(() -> {
            try {
                tcpl.onConnectionReady(TCPConnection.this);
                while (!rxThread.isInterrupted())
                    tcpl.onReceiveString(TCPConnection.this, in.readLine());
            } catch (IOException e) {
                tcpl.onException(TCPConnection.this, e);
            } finally {
                tcpl.onDisconnect(TCPConnection.this);
            }
        });
        rxThread.start();
    }

    public synchronized void sendString(String value){
        try {
            out.write(value + "\r\n");
            out.flush();
        } catch (IOException e) {
            tcpl.onException(this, e);
            disconnect();
        }
    }

    public void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            tcpl.onException(TCPConnection.this, e);
        }
    }

    public String getString(){
        return socket.getInetAddress()+ ":" +socket.getPort();
    }
}
