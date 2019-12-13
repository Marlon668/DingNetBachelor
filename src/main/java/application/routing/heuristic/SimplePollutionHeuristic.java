package application.routing.heuristic;

import application.pollution.PollutionGrid;
import org.jxmapviewer.viewer.GeoPosition;
import util.MapHelper;


/**
 * A simple routing heuristic which also takes the pollution over a given connection into account.
 */
public class SimplePollutionHeuristic implements RoutingHeuristic {
    private final PollutionGrid pollutionGrid;

    public SimplePollutionHeuristic(PollutionGrid pollutionGrid) {
        this.pollutionGrid = pollutionGrid;
    }

    @Override
    public double calculateAccumulatedCost(HeuristicEntry entry) {
        GeoPosition begin = entry.graph.getWayPoint(entry.connection.getFrom());
        GeoPosition end = entry.graph.getWayPoint(entry.connection.getTo());

        double pollutionValue = this.pollutionGrid.getPollutionLevel(MapHelper.meanPosition(begin, end)).getPollutionFactor();

        double factor = (0.0 <= pollutionValue && pollutionValue < 0.2) ? 1 :
                        (0.2 <= pollutionValue && pollutionValue < 0.4) ? 2 :
                        (0.4 <= pollutionValue && pollutionValue < 0.6) ? 3 : 10;

        // The lower the pollution level, the better the heuristic
        return (0.70*pollutionValue + 0.30*MapHelper.distance(begin,end)/0.350);
        //factor*MapHelper.distance(begin,end)*MapHelper.distance(begin,end);
    }
    @Override
    public double calculateHeuristic(HeuristicEntry entry) {
        double accumulatedCost = calculateAccumulatedCost(entry);
        GeoPosition begin = entry.graph.getWayPoint(entry.connection.getFrom());
        return  accumulatedCost + MapHelper.distance(begin,entry.destination);
    }
}
