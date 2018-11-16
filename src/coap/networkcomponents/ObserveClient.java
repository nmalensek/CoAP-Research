package coap.networkcomponents;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import coap.util.ResultWriter;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.network.stack.congestioncontrol.*;

public class ObserveClient {
    private long counter = 0;
    private long latencyCounter = 0;
    private static final int DEFAULT_PORT = 5683;
    private static String serverAddress;
    private static int numPorts;
    private ResultWriter resultWriter;

    public ObserveClient(String congControl, int nStart) {
        setUpClient(congControl, nStart);
    }

    private void setUpClient(String congestionControlAlg, int nStart) {
        NetworkConfig config = new NetworkConfig().setInt(NetworkConfig.Keys.NSTART, nStart);
        if (!congestionControlAlg.isEmpty()) {
                    config
                    // enable congestion control (can also be done cia Californium.properties)
                    .setBoolean(NetworkConfig.Keys.USE_CONGESTION_CONTROL, true)
                    // see class names in org.eclipse.californium.core.network.stack.congestioncontrol
                    .setString(NetworkConfig.Keys.CONGESTION_CONTROL_ALGORITHM, congestionControlAlg);
        }

        // create an endpoint with this configuration
        CoapEndpoint customEndpoint = new CoapEndpoint(config);
        // all CoapClients will use the default endpoint (unless CoapClient#setEndpoint() is used)
        EndpointManager.getEndpointManager().setDefaultEndpoint(customEndpoint);

        resultWriter = new ResultWriter(this);
        resultWriter.start();
    }

    private void startObserving() {
        CoapClient client = new CoapClient();
        System.out.println("OBSERVE (press enter to exit)");
        int finalPort = DEFAULT_PORT + numPorts;

        for (int port = DEFAULT_PORT; port < finalPort; port++) {

            client.setURI("coap://" + serverAddress + ":" + port + "/obs");

            CoapObserveRelation relation = client.observe(
                    new CoapHandler() {
                        @Override
                        public void onLoad(CoapResponse response) {
                            String content = response.getResponseText();
                            long latency = (System.currentTimeMillis() - Long.parseLong(content.split("-")[0]));
                            System.out.println("SERVER" + response.advanced().getSourcePort() + ": " + content + "\t" + "|" + "\t" +
                                    "Latency: " + latency);
                            counter++;
                            latencyCounter += latency;
                            resultWriter.addMessage();
                            resultWriter.addLatency(latency);
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
        resultWriter.setRunning(false);
        resultWriter.reportStats();
        System.out.println("Messages successfully received: " + counter);
        System.out.println("With an average latency per message of " + (latencyCounter / counter));
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: [server IP address] [optional: congestion control algorithm name] " +
                    "[NSTART value] [number of transmitting server ports]");
            System.out.println("Congestion control options: cocoa, cocoaStrong, rto, linuxrto, peakrto, none");
            System.exit(0);
        }
        serverAddress = args[0];
        numPorts = Integer.parseInt(args[3]);
        String congestionControl = "";
         switch (args[1]) {
             case "cocoa":
                 congestionControl = Cocoa.class.getSimpleName();
                 break;
             case "cocoaStrong":
                 congestionControl = CocoaStrong.class.getSimpleName();
                 break;
             case "rto":
                 congestionControl = BasicRto.class.getSimpleName();
                 break;
             case "linuxrto":
                 congestionControl = LinuxRto.class.getSimpleName();
                 break;
             case "peakrto":
                 congestionControl = PeakhopperRto.class.getSimpleName();
                 break;
             case "none":
                 break;
         }
        ObserveClient observeClient = new ObserveClient(congestionControl, Integer.parseInt(args[2]));
        observeClient.startObserving();
        observeClient.waitUntilFinished();
    }
    //nstart default 4 for cocoa, 1 for "normal" in original code

}
// response.advanced().getRTT() and .getTimestamp() don't work in observe mode; they return the relationship's duration (initial GET
// is used as the start of the RTT counter or timestamp).