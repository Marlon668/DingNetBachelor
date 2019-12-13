package unit;

import application.pollution.PollutionGrid;
import application.pollution.PollutionLevel;
import application.pollution.PollutionMonitor;
import application.routing.AStarRouter;
import application.routing.KAStarRouter;
import application.routing.RoutingApplication;
import application.routing.heuristic.SimplePollutionHeuristic;
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
import util.Pair;
import util.Path;
import util.xml.ConfigurationReader;
import util.xml.IdRemapping;


import java.io.File;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestKPaths {
    @Test
    void LongestEdge() {
        SimulationRunner simulationRunner = SimulationRunner.getInstance();
        IdRemapping idRemapping = new IdRemapping();
        File configuration = new File("/Users/marlo/Desktop/DingNet-1.2.0/settings/configurations/leuvenCity_bidirectional.xml");
        simulationRunner.loadConfigurationFromFile(configuration);
        ConfigurationReader.loadConfiguration(configuration,simulationRunner);
        PollutionGrid grid = new PollutionGrid();
        GeoPosition begin = simulationRunner.getEnvironment().getGraph().getWayPoint(simulationRunner.getEnvironment().getMotes().get(1).getEUI());
        GeoPosition end = simulationRunner.getEnvironment().getGraph().getWayPoint(simulationRunner.getEnvironment().getMotes().get(3).getEUI());
        //grid.addMeasurement(1L, new GeoPosition(5, 5), new PollutionLevel(0.5));
        //grid.addMeasurement(2L, new GeoPosition(10, 10), new PollutionLevel(0.8));
        //grid.addMeasurement(2L, new GeoPosition(10, 10), new PollutionLevel(0.1));
        //Environment environment = new Environment(new Characteristic[0][1], new GeoPosition(5, 5), 1, new HashMap<>(), new HashMap<>());
        KAStarRouter kassatar = new KAStarRouter(new SimplePollutionHeuristic(simulationRunner.getPollutionGrid()));
        List<Pair<Double,List<GeoPosition>>> result = kassatar.retrieveKPaths(simulationRunner.getEnvironment().getGraph(),begin,end,2);
        assertEquals(result.size(),2);
    }
}
