package ddist;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Thread responsible for retrieving MyTextEvents from an event queue and
 * sending them to another editor.
 */
public class EventSender implements Runnable {
    private BlockingQueue<MyTextEvent> _inEventQueue;
    private BlockingQueue<MyTextEvent> _outEventQueue;
    private Socket _socket;

    /**
     * @param sock a Socket representing the connection to the other editor
     * @param inEventQueue a BlockingQueue in which to place edit events from
     * the other editor
     * @param outEventQueue a BlockingQueue from which to take events for
     * sending to the other editor
     */
    public EventSender(Socket sock, BlockingQueue<MyTextEvent> inEventQueue,
            BlockingQueue<MyTextEvent> outEventQueue) {
        _socket        = sock;
        _inEventQueue  = inEventQueue;
        _outEventQueue = outEventQueue;
    }

    public void run() {
        try {
            // Open connection to the other editor
            ObjectOutputStream objOut
            = new ObjectOutputStream( _socket.getOutputStream() );

            // Take events arriving in the queue
            while (true) {
                MyTextEvent event = _outEventQueue.take();
                objOut.writeObject(event);

                // Cleanup and close thread if we want to disconnect
                if (event instanceof DisconnectEvent) {

                    if ( ((DisconnectEvent) event).shouldClose() ) {
                        _inEventQueue.clear();
                        _outEventQueue.clear();
                        
                        
                        _socket.close();
                    }

                    break;
                }
            }
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
