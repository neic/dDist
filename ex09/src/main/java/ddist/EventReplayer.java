package ddist;

import java.awt.EventQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.text.AbstractDocument;

/**
 *
 * Takes the event recorded by the DocumentEventCapturer and replays
 * them in a JTextArea.
 *
 * @author Jesper Buus Nielsen
 *
 */
public class EventReplayer implements Runnable {
    private DocumentEventCapturer documentEventCapturer;
    private BlockingQueue<Event> eventQueue;
    private JTextArea area;
    private JFrame frame;
    private DocumentEventCapturer filter;

    /**
     * @param eventQueue the blocking queue from which to take events to
     * replay them in the on the second argument
     * @param area the text area in which to replay the events
     * @param frame the overall frame of the program (might be done better)
     */
    public EventReplayer(DocumentEventCapturer dec, BlockingQueue<Event>
            eventQueue, JTextArea area, JFrame frame) {
        this.documentEventCapturer = dec;
        this.eventQueue = eventQueue;
        this.area = area;
        this.frame = frame;

        AbstractDocument doc = (AbstractDocument) area.getDocument();
        this.filter = (DocumentEventCapturer) doc.getDocumentFilter();
    }

    public void run() {
        boolean wasInterrupted = false;
        while (!wasInterrupted) {
            try {
                Event mte = eventQueue.take();
                if (mte instanceof TextInsertEvent) {
                    final TextInsertEvent tie = (TextInsertEvent)mte;
                    EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    /*
                                     * The text area should be modified only
                                     * by user input and this EventReplayer.
                                     * Both use the EDT, so the filter won't
                                     * be accessed by anything else between
                                     * disabling and enabling the event
                                     * generation.
                                     */
                                    filter.disableEventGeneration();
                                    area.insert(tie.getText(), tie.getOffset());
                                    filter.enableEventGeneration();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    /* We catch all axceptions, as an uncaught
                                     * exception would make the EDT unwind,
                                     * which is now healthy.
                                     */
                                }
                            }
                        });
                } else if (mte instanceof TextRemoveEvent) {
                    final TextRemoveEvent tre = (TextRemoveEvent)mte;
                    EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    // See comment on TextInsertEvent above.
                                    filter.disableEventGeneration();
                                    area.replaceRange(null, tre.getOffset(),
                                                      tre.getOffset() +
                                                      tre.getLength());
                                    filter.enableEventGeneration();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    /* We catch all axceptions, as an uncaught
                                     * exception would make the EDT unwind,
                                     * which is now healthy.
                                     */
                                }
                            }
                        });
                }
                else if (mte instanceof DisconnectEvent) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            JOptionPane.showMessageDialog(
                                frame, "Disconnected.");
                            frame.setTitle("Disconnected");
                            area.setText("");
                            documentEventCapturer.disableEventGeneration();
                        }
                    } );
                }
                else {
                    System.err.println("Illegal event received.");
                    System.exit(1);
                }
            } catch (Exception _) {
                wasInterrupted = true;
            }
        }
        System.out.println(
            "I'm the thread running the EventReplayer, now I die!");
    }
}
