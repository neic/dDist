package ddist;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Server {
    private final int _port;
    private InetAddress _address;
    final ServerEventDistributor _eventDistributor;
    private final Semaphore _readyForAccepting = new Semaphore(0);

    public Server(int port, String initialText, int oldClientsCount, Callbacks
            callbacks) {
        _port             = port;
        _eventDistributor = new ServerEventDistributor(
                                initialText,
                                oldClientsCount,
                                callbacks
                            );
    }

    public Server(int port, Callbacks callbacks) {
        _port             = port;
        _eventDistributor = new ServerEventDistributor(callbacks);
    }

    public void start() {
        listenForConnection();
        startEventDistributor();

        try {
            _readyForAccepting.acquire();
        }
        catch (InterruptedException e) {
            throw new AssertionError();
        }
    }

    private void listenForConnection() {
        // Asynchronously wait for a connection
        new Thread( new Runnable() {
                public void run() {
                    try {
                        @SuppressWarnings("resource")
                        ServerSocket servSock = new ServerSocket(_port);
                        _address = servSock.getInetAddress();
                        _readyForAccepting.release();

                        while (true) {
                            // Wait for an incoming connection
                            Socket socket = null;
                            socket = servSock.accept();
                            _eventDistributor.addClient(socket);
                        }
                    }
                    catch (IOException ex) {
                        System.out.println("Cannot listen."); //TODO: GUI
                        ex.printStackTrace();
                        return;
                    }
                }
            } ).start();
    }

    public InetAddress getListenAddress() {
        return _address;
    }

    public int getListenPort() {
        return _port;
    }

    private void startEventDistributor() {
        // Start thread containing the Jupiter client/server
        Thread eventDistributorThread = new Thread(_eventDistributor);
        eventDistributorThread.start();
    }
}
