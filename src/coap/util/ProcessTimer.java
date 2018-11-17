package coap.util;

import coap.networkcomponents.TimedComponent;

public class ProcessTimer extends Thread {

    private long duration;
    private TimedComponent parent;

    public ProcessTimer(long duration, TimedComponent parent) {
        this.duration = duration;
        this.parent = parent;
    }

    @Override
    public void run() {
        while(duration > 0) {
            try {
                duration--;
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        parent.stop();
    }
}
