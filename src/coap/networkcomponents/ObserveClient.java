package coap.networkcomponents;

import coap.util.ProcessTimer;
import coap.util.ResultWriter;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.network.stack.congestioncontrol.*;

public class ObserveClient implements TimedComponent {
    private long counter = 0;
    private long latencyCounter = 0;
    private static final int DEFAULT_PORT = 5683;
    private static String serverAddress;
    private static int numPorts;
    private ResultWriter resultWriter;
    private ProcessTimer timer;
    private long duration;

    public ObserveClient(String congControl, int nStart, long duration) {
        this.duration = duration;
        setUpClient(congControl, nStart);
    }

    private void setUpClient(String congestionControlAlg, int nStart) {
        NetworkConfig config = new NetworkConfig().setInt(NetworkConfig.Keys.NSTART, nStart);
        if (!congestionControlAlg.isEmpty()) {
                    config
                    // enable congestion control (can also be done via Californium.properties)
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
        timer = new ProcessTimer(duration, this);
        timer.start();
    }

    private void startObserving() {
        CoapClient client = new CoapClient();
        int finalPort = DEFAULT_PORT + numPorts;

        for (int port = DEFAULT_PORT; port < finalPort; port++) {

            client.setURI("coap://" + serverAddress + ":" + port + "/obs");

            CoapObserveRelation relation = client.observe(
                    new CoapHandler() {
                        @Override
                        public void onLoad(CoapResponse response) {
                            String content = response.getResponseText();
                            long latency = (System.currentTimeMillis() - Long.parseLong(content.split("-")[0]));
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

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        resultWriter.setRunning(false);
        resultWriter.reportStats();
        System.out.println("Messages successfully received: " + counter);
        System.out.println("With an average latency per message of " + (latencyCounter / counter));
        System.exit(0);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: [server IP address] [optional: congestion control algorithm name] " +
                    "[NSTART value] [number of transmitting server ports] [run duration (in ms)]");
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
        ObserveClient observeClient = new ObserveClient(congestionControl, Integer.parseInt(args[2]), Long.parseLong(args[4]));
        observeClient.startObserving();
    }
    //nstart default 4 for cocoa, 1 for "normal" in original code

}
// response.advanced().getRTT() and .getTimestamp() don't work in observe mode; they return the relationship's duration (initial GET
// is used as the start of the RTT counter or timestamp).

//nstart is the maximum number of outstanding notification/response a server can have pending for a client. Only
//n messages may be sent per RTT.