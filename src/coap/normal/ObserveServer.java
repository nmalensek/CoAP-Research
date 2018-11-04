package coap.normal;

import static org.eclipse.californium.core.coap.CoAP.ResponseCode.*;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import coap.sensors.TempAndHumiditySensor;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class ObserveServer extends CoapResource {

    private ArrayList<TempAndHumiditySensor> sensors = new ArrayList<>();
    private StringBuilder builder = new StringBuilder();
    private String currentStatus;
    private int counter = 1;

    public ObserveServer(String name) {
        super(name);
        setObservable(true); // enable observing
        setObserveType(Type.CON); // configure the notification type to CONs
        getAttributes().setObservable(); // mark observable in the Link-Format

        sensors.add(new TempAndHumiditySensor(1L));
        sensors.add(new TempAndHumiditySensor(2L));

        // schedule a periodic update task, otherwise let events call changed()
        Timer timer = new Timer();
        timer.schedule(new UpdateTask(), 0, 500);
    }

    private class UpdateTask extends TimerTask {
        @Override
        public void run() {
            counter = counter % 2 == 0 ? 1 : 2;
            try {
                currentStatus = System.currentTimeMillis() + "-" + sensors.get(counter-1).call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            changed(); // notify all observers
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        exchange.setMaxAge(1); // the Max-Age value should match the update interval
        exchange.respond(currentStatus);
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        delete(); // will also call clearAndNotifyObserveRelations(ResponseCode.NOT_FOUND)
        exchange.respond(DELETED);
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        exchange.respond(CHANGED);
        changed(); // notify all observers
    }

    public static void main(String[] args) {
        CoapServer server = new CoapServer(5684);
        server.add(new ObserveServer("obs"));
        server.start();
    }

}