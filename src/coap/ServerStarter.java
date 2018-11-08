package coap;

import coap.normal.ObserveServer;
import org.eclipse.californium.core.CoapServer;

import java.io.InputStreamReader;
import java.util.Scanner;

public class ServerStarter {

    static CoapServer[] serverArray = new CoapServer[] {
      new CoapServer(),
      new CoapServer(5684),
      new CoapServer(5685),
      new CoapServer(5686),
      new CoapServer(5687),
      new CoapServer(5688),
      new CoapServer(5689),
      new CoapServer(5690),
      new CoapServer(5691),
      new CoapServer(5692),
    };

    public static void main(String[] args) {
        for (CoapServer s : serverArray) {
            s.add(new ObserveServer("obs"));
            s.start();
        }
        Scanner scanner = new Scanner(new InputStreamReader(System.in));
        scanner.next();
        long totalMessages = 0;
        for (CoapServer s : serverArray) {
            s.stop();
            ObserveServer observeServer = (ObserveServer) s.getRoot().getChild("obs");
            totalMessages += observeServer.getMessageCounter();
            System.out.println(observeServer.reportStatistics());
        }
        System.out.println("Total sent messages: " + totalMessages);
    }
}
