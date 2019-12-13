package selfadaptation.instrumentation;

import application.routing.PathFinder;
import iot.Environment;
import iot.networkentity.Mote;
import iot.networkentity.UserMote;
import org.jxmapviewer.viewer.GeoPosition;
import util.GraphStructure;
import util.Path;

import java.util.List;

/**
 * A class to allow self-adaptation software to edit mote settings.
 */
public class MoteEffector {

    /**
     * Constructs a MoteEffector.
     */
    public MoteEffector() {
    }

    /**
     * A method to set the power of a mote.
     * @param mote The mote to set the power of.
     * @param power The power to set.
     */
    public void setPower(Mote mote, int power) {
        mote.setTransmissionPower(power);
    }

    /**
     * A method to set the spreading factor of a mote.
     * @param mote The mote to set the spreading factor of.
     * @param spreadingFactor The spreading factor to set.
     */
    public void setSpreadingFactor(Mote mote, int spreadingFactor) {
        mote.setSF(spreadingFactor);
    }

    /**
     * A method to set the movement speed of a mote.
     * @param mote The mote to set the movement speed of.
     * @param movementSpeed The movement speed to set.
     */
    public void setMovementSpeed(Mote mote, double movementSpeed) {
        mote.setMovementSpeed(movementSpeed);
    }

    /**
     * A method to change the path of a mote.
     * Used in the method of noAdaptation
     * @param mote The mote to change the path of.
     * @param pathFinder The pathfinder that is used.
     * @param environment The environment of simulation
     */
    public void changePath(Mote mote, PathFinder pathFinder, Environment environment)
    {
        if (mote instanceof  UserMote)
        {
            List<GeoPosition> positions = pathFinder.retrievePath(environment.getGraph(),environment.getMapHelper().toGeoPosition(mote.getPosInt()),((UserMote) mote).getDestination());
            Path newPath = new Path(positions,environment.getGraph());
            mote.setPath(newPath);
        }

    }

    /**
     * A method to set the energy level of a mote.
     * @param mote The mote to set the energy level of.
     * @param energyLevel The energy level to set.
     */
    public void setEnergyLevel(Mote mote, int energyLevel) {
        mote.setEnergyLevel(energyLevel);
    }


}
