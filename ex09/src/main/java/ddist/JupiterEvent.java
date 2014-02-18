package ddist;

/**
 * Events exchanged between Jupiter clients and the server.
 */
class JupiterEvent implements Event {
    private static final long serialVersionUID = 8124416967036144277L;

    private final Event _containedEvent;
    private final JupiterTime _timestamp;
    private final boolean _isFromServer;

    public JupiterEvent(Event event, JupiterTime time, boolean isFromServer) {
        _containedEvent = event;
        _timestamp      = time;
        _isFromServer   = isFromServer;
    }

    /**
     * This event was sent out when its sender already knew about the other
     * event.
     */
    public boolean knowsAbout( JupiterEvent other ) {
        return _timestamp.knowsAbout( other.getTimestamp() );
    }

    public JupiterTime getTimestamp() {
        return _timestamp;
    }

    public Event getContainedEvent() {
        return _containedEvent;
    }

    public boolean isFromServer() {
        return _isFromServer;
    }
}
