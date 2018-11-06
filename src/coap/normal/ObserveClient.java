package coap.normal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.network.stack.congestioncontrol.Cocoa;

public class ObserveClient {
    private static boolean cocoa;
    private long counter = 0;
    private int[] portArray = new int[]{
            5683,
            5684,
            5685
    };

    public ObserveClient(boolean useCocoa) {
        if (useCocoa) { setCocoaBackoff(); }
    }

    private void startObserving() {
        CoapClient client = new CoapClient();
        System.out.println("OBSERVE (press enter to exit)");

        for (int i = 0; i < portArray.length; i++) {

            client.setURI("coap://localhost:" + portArray[i] + "/obs");

            CoapObserveRelation relation = client.observe(
                    new CoapHandler() {
                        @Override
                        public void onLoad(CoapResponse response) {
                            String content = response.getResponseText();
                            System.out.println("SERVER" + response.advanced().getSourcePort() + ": " + content + "\t" + "|" + "\t" +
                                    "Latency: " + (System.currentTimeMillis() - Long.parseLong(content.split("-")[0])));
                            counter += 1;
                        }

                        @Override
                        public void onError() {
                            System.err.println("OBSERVING FAILED (press enter to exit)");
                        }
                    });
        }
    }

    private void waitUntilFinished() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        br.readLine();
        System.out.println("Messages successfully received: " + counter);
    }

    private void setCocoaBackoff() {
        NetworkConfig config = new NetworkConfig()
                // enable congestion control (can also be done cia Californium.properties)
                .setBoolean(NetworkConfig.Keys.USE_CONGESTION_CONTROL, true)
                // see class names in org.eclipse.californium.core.network.stack.congestioncontrol
                .setString(NetworkConfig.Keys.CONGESTION_CONTROL_ALGORITHM, Cocoa.class.getSimpleName())
                // set NSTART to four
                .setInt(NetworkConfig.Keys.NSTART, 4);

        // create an endpoint with this configuration
        CoapEndpoint cocoaEndpoint = new CoapEndpoint(config);
        // all CoapClients will use the default endpoint (unless CoapClient#setEndpoint() is used)
        EndpointManager.getEndpointManager().setDefaultEndpoint(cocoaEndpoint);
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            cocoa = Boolean.parseBoolean(args[0]);
        }
        ObserveClient observeClient = new ObserveClient(cocoa);
        observeClient.startObserving();
        observeClient.waitUntilFinished();
    }

}
// response.advanced().getRTT() and .getTimestamp() don't work in observe mode; they return the relationship's duration (initial GET
// is used as the start of the RTT counter or timestamp).