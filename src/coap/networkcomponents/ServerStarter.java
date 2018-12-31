package coap.networkcomponents;

import coap.util.ProcessTimer;
import org.eclipse.californium.core.CoapServer;

import java.util.ArrayList;

public class ServerStarter implements TimedComponent{

    private static final int START_PORT = 5683;
    private static ArrayList<CoapServer> serverList = new ArrayList<>();
    private long totalMessages = 0;
    private int numServers;
    private int messageInterval;
    private long duration;
    private ProcessTimer timer;

    public ServerStarter(int numServers, int messageInterval, long duration) {
        this.numServers = numServers;
        this.messageInterval = messageInterval;
        this.duration = duration;
    }

    @Override
    public void start() {
        int finalPort = START_PORT + numServers;
        for (int i = START_PORT; i < finalPort; i++) {
            CoapServer s = new CoapServer(i);
            serverList.add(s);
            s.add(new ObserveServer("obs", messageInterval))
                    .start();
        }

        timer = new ProcessTimer(duration, this);
        timer.start();
    }

    @Override
    public void stop() {
        for (CoapServer s : serverList) {
            s.stop();
            ObserveServer observeServer = (ObserveServer) s.getRoot().getChild("obs");
            totalMessages += observeServer.getMessageCounter();
            System.out.println(observeServer.reportStatistics());
        }
        System.out.println("Total sent messages: " + totalMessages);
        System.exit(0);
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: [number of servers] [message publish frequency in ms] " +
                    "[process duration (in ms)]");
            System.exit(0);
        }
        int numServers = Integer.parseInt(args[0]);
        int messageInterval = Integer.parseInt(args[1]);
        long duration = Long.parseLong(args[2]);

        ServerStarter starter = new ServerStarter(numServers, messageInterval, duration);
        starter.start();
    }
}
