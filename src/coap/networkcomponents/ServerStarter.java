package coap.networkcomponents;

import org.eclipse.californium.core.CoapServer;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

public class ServerStarter {

    private static final int START_PORT = 5683;
    private static ArrayList<CoapServer> serverList = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: [number of servers] [message publish frequency in ms]");
            System.exit(0);
        }

        int numServers = Integer.parseInt(args[0]);
        int messageInterval = Integer.parseInt(args[1]);
        int finalPort = START_PORT + numServers;

        for (int i = START_PORT; i < finalPort; i++) {
            CoapServer s = new CoapServer(i);
            serverList.add(s);
            s.add(new ObserveServer("obs", messageInterval))
                    .start();
        }
        Scanner scanner = new Scanner(new InputStreamReader(System.in));
        scanner.next();
        long totalMessages = 0;
        for (CoapServer s : serverList) {
            s.stop();
            ObserveServer observeServer = (ObserveServer) s.getRoot().getChild("obs");
            totalMessages += observeServer.getMessageCounter();
            System.out.println(observeServer.reportStatistics());
        }
        System.out.println("Total sent messages: " + totalMessages);
    }
}
