package application.routing;

import application.routing.heuristic.RoutingHeuristic;
import application.routing.heuristic.RoutingHeuristic.HeuristicEntry;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import util.GraphStructure;
import util.MapHelper;
import util.Pair;

import java.io.Console;
import java.util.*;
import java.util.stream.Collectors;


/**
 * An class which implements the A* routing algorithm, assuming the used heuristic is consistent.
 */
public class KAStarRouter implements PathFinder {

    // The maximum amount of distance the closest waypoint should be to a given GeoPosition (in km)
    @SuppressWarnings("FieldCanBeLocal")
    private final double DISTANCE_THRESHOLD_POSITIONS = 0.05;

    // The heuristic used in the A* algorithm
    private RoutingHeuristic heuristic;


    public KAStarRouter(RoutingHeuristic heuristic) {
        this.heuristic = heuristic;
    }


    @Override
    public List<GeoPosition> retrievePath(GraphStructure graph, GeoPosition begin, GeoPosition end){
        return retrieveKPaths(graph,begin,end,1).get(0).getRight();
    }
    @Override
    public List<Pair<Double,List<GeoPosition>>> retrieveKPaths(GraphStructure graph, GeoPosition begin, GeoPosition end, Integer amountBestPaths) {
        long beginWaypointId = graph.getClosestWayPointWithinRange(begin, DISTANCE_THRESHOLD_POSITIONS)
            .orElseThrow(() -> new IllegalStateException("The mote position retrieved from the message is not located at a waypoint."));
        long endWaypointId = graph.getClosestWayPointWithinRange(end, DISTANCE_THRESHOLD_POSITIONS)
            .orElseThrow(() -> new IllegalStateException("The destination position retrieved from the message is not located at a waypoint."));

        List<Pair<Double, List<GeoPosition>>> KBestPaths = new ArrayList<>();

        Set<Long> visitedConnections = new HashSet<>();

        HashMap<Long, Integer> amountConnectionUsed = new HashMap<>();

        PriorityQueue<FringeEntry> fringe = new PriorityQueue<>();
        // Initialize the fringe by adding the first outgoing connections
        graph.getConnections().entrySet().stream()
            .filter(entry -> entry.getValue().getFrom() == beginWaypointId)
            .forEach(entry -> {
                double accumulatedCost = this.heuristic.calculateAccumulatedCost(new HeuristicEntry(graph, entry.getValue(), end));
                double distanceToDestination = MapHelper.distance(graph.getWayPoint(entry.getValue().getTo()), end);
                fringe.add(new FringeEntry(
                    List.of(entry.getKey()),
                    accumulatedCost, //+ distanceToDestination,
                    accumulatedCost
                ));
                visitedConnections.add(entry.getKey());
                if (!amountConnectionUsed.containsKey(beginWaypointId)) {
                    amountConnectionUsed.put(beginWaypointId, 1);
                } else {
                    Integer amountConnections = amountConnectionUsed.get(beginWaypointId) + 1;
                    amountConnectionUsed.put(beginWaypointId, amountConnections);
                }
            });


        // Actual A* algorithm
        try {
            while (!fringe.isEmpty()) {
                FringeEntry current = fringe.poll();
                long lastWaypointId = graph.getConnection(current.getLastConnectionId()).getTo();

                // Are we at the destination?
                if (lastWaypointId == endWaypointId) {
                    Pair<Double, List<GeoPosition>> IBestPath = new Pair(current.heuristicValue, this.getPath(current.connections, graph, end));
                    KBestPaths.add(IBestPath);
                    amountBestPaths -= 1;
                    if (amountBestPaths == 0){
                        return KBestPaths;
                    }
                else{
                        //visitedConnections.clear();
                        DeleteConnectionsInGraph(current.connections, visitedConnections, amountConnectionUsed, graph);
                    }
                }


                // Explore the different outgoing connections from the last connection in the list
                // -> Add the new possible paths (together with their new heuristic values) to the fringe
                graph.getOutgoingConnectionsById(lastWaypointId).stream()
                    .filter(connId -> !visitedConnections.contains(connId))
                    //.filter(connId -> !current.connections.contains(connId))
                    .filter(connId -> !containsWaypoints(current.connections, connId, graph))// Filter out connections which we have already considered (since these were visited in a better path first)
                    .forEach(connId -> {
                        List<Long> extendedPath = new ArrayList<>(current.connections);
                        extendedPath.add(connId);

                        double accumulatedCost = current.accumulatedCost + this.heuristic.calculateAccumulatedCost(new HeuristicEntry(graph, graph.getConnection(connId), end));
                        double distanceToDestination = MapHelper.distance(graph.getWayPoint((graph.getConnection(connId).getTo())), end);
                        double newHeuristicValue = current.accumulatedCost; //+ distanceToDestination;

                        fringe.add(new FringeEntry(extendedPath, newHeuristicValue, accumulatedCost));
                        visitedConnections.add(connId);
                        if (amountConnectionUsed.get(lastWaypointId) == null) {
                            amountConnectionUsed.put(lastWaypointId, 1);
                        } else {
                            Integer amountConnections = amountConnectionUsed.get(lastWaypointId) + 1;
                            amountConnectionUsed.put(lastWaypointId, amountConnections);
                        }

                    });
            }

            throw new RuntimeException(String.format("Could not find a path from {%s} to {%s}", begin.toString(), end.toString()));

        }
        catch(RuntimeException e)
        {
            return KBestPaths;
        }
    }

    private boolean containsWaypoints(List<Long> connections,Long connectionId,GraphStructure graph)
    {
        for(Long conn: connections)
        {
            if(graph.getConnection(conn).getFrom() == graph.getConnection(connectionId).getTo())
            {
                return true;
            }
        }
        return false;
    }
    private void DeleteConnectionsInGraph(List<Long> connections, Set<Long> visitedConnections,HashMap<Long,Integer> amountConnectionsUsed,GraphStructure graph) {
        long waypointId = -1;
        for (int i = connections.size()-1;i>=0;i--){
            //System.out.println(cijfer);
            //System.out.println(amountConnectionsUsed.get(cijfer));
                if(waypointId == -1 && amountConnectionsUsed.get(graph.getConnection(connections.get(i)).getFrom()) >1)
                {
                    waypointId = graph.getConnection(connections.get(i)).getFrom();
                    //System.out.println(waypointId);
                    //System.out.println(amountConnectionsUsed.get(waypointId));
                    Integer amountConnections = amountConnectionsUsed.get(waypointId) - 1;
                    amountConnectionsUsed.put(waypointId, amountConnections);
                    //System.out.println(amountConnectionsUsed.get(waypointId));
                    visitedConnections.remove(connections.get(i));
                }
                else {
                    if (!(waypointId == -1)) {
                        waypointId = graph.getConnection(connections.get(i)).getFrom();
                    } else {
                        //System.out.println("ok");
                        //Integer amountConnections = amountConnectionsUsed.get(graph.getConnection(connections.get(i)).getFrom()) ;
                        //System.out.println(amountConnections);
                        //if (amountConnections == 0) {
                            visitedConnections.remove(connections.get(i));
                            amountConnectionsUsed.remove(graph.getConnection(connections.get(i)).getFrom());
                        //} else {
                        //    amountConnectionsUsed.put(graph.getConnection(connections.get(i)).getFrom(), amountConnections);
                        //}
                    }
                }
            }
    }


    /**
     * Convert a list of connection Ids to a list of the respective GeoPositions of those connections.
     * @param connectionIds The list of connections Ids which are used for the conversion.
     * @param graph The graph which contains all the connections and waypoints.
     * @return A list of GeoPositions which correspond to the connections in {@code connectionIds}.
     */
    private List<GeoPosition> getPath(List<Long> connectionIds, GraphStructure graph,GeoPosition end) {
        List<GeoPosition> points = connectionIds.stream()
            .map(o -> graph.getConnections().get(o).getFrom())
            .map(graph::getWayPoint)
            .collect(Collectors.toList());

        // Don't forget the final waypoint
        long lastWaypointId = graph.getConnections().get(connectionIds.get(connectionIds.size()-1)).getTo();
        points.add(graph.getWayPoint(lastWaypointId));


        return points;
    }


    /**
     * Class used in the priority queue, providing an order for the A* algorithm based on the
     * accumulated heuristic values for the evaluated path.
     */
    private static class FringeEntry implements Comparable<FringeEntry> {
        List<Long> connections;
        double heuristicValue;
        double accumulatedCost;

        FringeEntry(List<Long> connections, double heuristicValue,double accumulatedCost) {
            this.connections = connections;
            this.heuristicValue = heuristicValue;
            this.accumulatedCost = accumulatedCost;
        }

        long getLastConnectionId() {
            return connections.get(connections.size() - 1);
        }

        @Override
        public int compareTo(@NotNull FringeEntry fringeEntry) {
            return Double.compare(this.heuristicValue, fringeEntry.heuristicValue);
        }
    }
}
