package coap.sensors;

import java.util.Random;
import java.util.concurrent.Callable;

public class TempAndHumiditySensor implements Callable<String>, Sensor {
    private long sensorId;
    private Random random = new Random();
    private StringBuilder builder = new StringBuilder();

    public TempAndHumiditySensor(long sensorId) {
        this.sensorId = sensorId;
    }

    @Override
    public String call() throws Exception {
        return prepareMessage();
    }

    private String prepareMessage() {
        builder.append("sensor");
        builder.append(sensorId);
        builder.append("-");
        builder.append(readTemperature(70, 6));
        builder.append("-");
        builder.append(readHumidity(50, 10));

        String payload = builder.toString();
        builder.setLength(0);

        return payload;
    }

    private String readTemperature(int initial, int changeBound) {
        return String.format("T:%.2fF", takeReading(initial, changeBound, 8));
    }

    private String readHumidity(int initial, int changeBound) {
        return String.format("H:%.2f%%", takeReading(initial, changeBound, 5));
    }

    private double takeReading(int initial, int changeBound, int stability) {
        int plusMinus = random.nextInt(21) % stability == 0 ? 1 : -1;
        double change = random.nextInt(changeBound) + random.nextDouble();

        return initial + (change * plusMinus);
    }

    @Override
    public Long getId() {
        return sensorId;
    }
}
