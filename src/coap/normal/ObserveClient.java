package coap.normal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

public class ObserveClient {

    static int[] portArray = new int[] {
            5683,
            5684,
            5685
    };

    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        for (int i = 0; i < portArray.length; i++) {
            CoapClient client = new CoapClient("coap://localhost:" + portArray[i] + "/obs");

            System.out.println("OBSERVE (press enter to exit)");

            CoapObserveRelation relation = client.observe(
                    new CoapHandler() {
                        @Override public void onLoad(CoapResponse response) {
                            String content = response.getResponseText();
                            System.out.println("SERVER" + response.advanced().getSourcePort() +": " + content + "\t" + "|" + "\t" +
                                    "Latency: " + (System.currentTimeMillis() - Long.parseLong(content.split("-")[0])));
                        }

                        @Override public void onError() {
                            System.err.println("OBSERVING FAILED (press enter to exit)");
                        }
                    });
        }

        br.readLine();

    }

}
