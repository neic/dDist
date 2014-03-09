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

    public Server(int port, String initialText, int oldClientsCount) {
        _port             = port;
        _eventDistributor = new ServerEventDistributor(
                                initialText,
                                oldClientsCount
                            );
    }

    public Server(int port) {
        _port             = port;
        _eventDistributor = new ServerEventDistributor();
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

    public void stop() {
        _eventDistributor.stop();
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
