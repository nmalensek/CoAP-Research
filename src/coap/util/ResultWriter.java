package coap.util;

import coap.networkcomponents.ObserveClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ResultWriter extends Thread {
    private String writeDirectory = "../CoAPResults/";
    private ConcurrentLinkedQueue<String> resultQueue = new ConcurrentLinkedQueue<>();
    private boolean running = true;
    private long startTime;
    private ObserveClient parent;
    private long totalMessages;
    private long totalLatency;

    public ResultWriter(ObserveClient parent) {
        this.parent = parent;
        startTime = System.currentTimeMillis();
        File dir = new File(writeDirectory);
        dir.mkdirs();
    }

    public void writeResults(String results) {
        try (FileOutputStream stream = new FileOutputStream(writeDirectory + "results" + startTime,
                true)) {
            stream.write((results + "\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running) {
//            String element = resultQueue.poll();
//            if (element != null) {
//                writeResults(element);
//            }
        }
//        try {
//            Thread.sleep(100);
//            while (!resultQueue.isEmpty()) {
//                writeResults(resultQueue.poll());
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public ConcurrentLinkedQueue<String> getResultQueue() {
        return resultQueue;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public synchronized void addMessage() {
        totalMessages++;
    }

    public synchronized void addLatency(long addition) {
        totalLatency += addition;
    }

    public void reportStats() {
        System.out.println("Received messages: " + totalMessages);
        System.out.println("Total latency: " + totalLatency);
        System.out.println("Average latency: " + (totalLatency/totalMessages));
    }
}
