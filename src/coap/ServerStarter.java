package coap;

import coap.normal.ObserveServer;
import org.eclipse.californium.core.CoapServer;

public class ServerStarter {

    static CoapServer[] serverArray = new CoapServer[] {
      new CoapServer(),
      new CoapServer(5684),
      new CoapServer(5685)
    };

    public static void main(String[] args) {
        for (CoapServer s : serverArray) {
            s.add(new ObserveServer("obs"));
            s.start();
        }
    }
}
