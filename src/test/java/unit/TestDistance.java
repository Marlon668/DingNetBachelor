package unit;

import iot.Characteristic;
import iot.Environment;
import iot.SimulationRunner;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import iot.strategy.response.gateway.DummyResponse;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;
import util.GraphStructure;
import util.Path;
import util.xml.ConfigurationReader;
import util.xml.IdRemapping;

import java.io.File;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestDistance {
    @Test
    void LongestEdge() {
        SimulationRunner simulationRunner = SimulationRunner.getInstance();
        IdRemapping idRemapping = new IdRemapping();
        File configuration = new File("/Users/marlo/Desktop/DingNet-1.2.0/settings/configurations/leuvenCity_bidirectional.xml");
        ConfigurationReader.loadConfiguration(configuration,simulationRunner);
        assertTrue(ConfigurationReader.getLongestEdge()>0.34);
    }
}

