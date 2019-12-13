package selfadaptation.instrumentation;

import iot.SimulationRunner;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import iot.networkentity.MoteSensor;
import org.jxmapviewer.viewer.GeoPosition;
import selfadaptation.feedbackloop.GenericFeedbackLoop;
import util.Connection;
import util.Path;

import java.sql.Time;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class representing methods for probing.
 */
public class MoteProbe {
    /**
     * A list with feedBackLoops using the probe.
     */
    private GenericFeedbackLoop genericFeedbackLoop;

    /**
     * Constructs a MoteProbe with no FeedBackLoops using it.
     */
    public MoteProbe() {

    }

    /**
     * Returns a list of GenericFeedbackLoops using the probe.
     * @return  A list of GenericFeedbackLoops using the probe.
     */
    public GenericFeedbackLoop getGenericFeedbackLoop() {
        return genericFeedbackLoop;
    }

    /**
     * Sets a GenericFeedbackLoop using the probe.
     * @param genericFeedbackLoop The FeedbackLoop to set.
     */
    public void setGenericFeedbackLoop(GenericFeedbackLoop genericFeedbackLoop) {
        this.genericFeedbackLoop =genericFeedbackLoop;
    }

    /**
     * Returns the spreading factor of a given mote.
     * @param mote The mote to generate the graph of.
     * @return the spreading factor of the mote
     */
    public int getSpreadingFactor(Mote mote) {
        return mote.getSF();
    }

    /**
     * Triggers the feedback loop.
     * @param gateway
     * @param devEUI
     */
    public void trigger(Gateway gateway, long devEUI) {
        SimulationRunner.getInstance().getEnvironment().getMotes().stream()
            .filter(m -> m.getEUI() == devEUI && getGenericFeedbackLoop().isActive())
            .reduce((a, b) -> b)
            .ifPresent(m -> getGenericFeedbackLoop().adapt(m, gateway));
    }

    public Path getPath(Mote mote)
    {
        return mote.getPath();
    }

    public int getPowerSetting(Mote mote) {
        return mote.getTransmissionPower();
    }

    /**
     * Conversion function which converts a message of bytes to the corresponding mote sensor measurements
     * @param mote The mote from which the message originated.
     * @param messageBody The raw message data containing the sensor measurements.
     * @return A map from the sensor types to the data/measurements they produced.
     */
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
     * @param path The path of the Mote
     * @param time The time of asking
     * @return changement Changement of the partial path given in a percentage
     * Must be in the annaliser
     */
    public double getChangementOfPath(Path path, Time time)
    {
        return 0;
    }
    /**
     * @param connection The connection where we would receive the pollution from
     * @param time The time of asking
     * @param velocity The velocity of the mote
     * @return pollution  Pollution of connection
     */
    public double getPollutionConnection(Connection connection, Time time, double velocity)
    {
        return 0;
    }

}
