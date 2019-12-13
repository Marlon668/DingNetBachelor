package application.routing;

import iot.mqtt.MQTTClientFactory;
import iot.mqtt.MqttClientBasicApi;
import iot.mqtt.TransmissionWrapper;
import iot.networkentity.Mote;
import iot.networkentity.MoteSensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Analiser {

    protected Analiser(double treshold) {

    }



    protected Map<MoteSensor, Byte[]> retrieveSensorData(Mote mote, List<Byte> messageBody) {
        Map<MoteSensor, Byte[]> sensorData = new HashMap<>();

        for (var sensor : mote.getSensors()) {
            int amtBytes = sensor.getAmountOfData();
            sensorData.put(sensor, messageBody.subList(0, amtBytes).toArray(Byte[]::new));
            messageBody = messageBody.subList(amtBytes, messageBody.size());
        }

        return sensorData;
    }


    /**
     * Destructor which can be used to properly destruct analisers
     */
    public void destruct() {}
}

