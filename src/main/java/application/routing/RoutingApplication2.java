package application.routing;

import application.Application;
import iot.Environment;
import iot.lora.LoraWanPacket;
import iot.lora.MessageType;
import iot.mqtt.BasicMqttMessage;
import iot.mqtt.Topics;
import iot.mqtt.TransmissionWrapper;
import iot.networkentity.Mote;
import iot.networkentity.UserMote;
import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.GraphStructure;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RoutingApplication2 extends Application {
    // The routes stored per device
    private Map<Long, List<GeoPosition>> routes;

    // The last recorded positions of the requesting user motes
    private Map<Long, GeoPosition> lastPositions;

    // The graph with waypoints and connections
    private GraphStructure graph;

    // The route finding algorithm that is used to handle routing requests
    private PathFinder pathFinder;

    private Environment environment;




    public RoutingApplication2(PathFinder pathFinder, Environment environment) {
        super(List.of(Topics.getNetServerToApp("+", "+")));
        this.routes = new HashMap<>();
        this.lastPositions = new HashMap<>();
        this.graph = graph;
        this.pathFinder = pathFinder;
        this.environment = environment;
    }


    private void setPath(Mote mote,GeoPosition destination)
    {
        GeoPosition beginPosition = environment.getMapHelper().toGeoPosition(mote.getXPosInt(),mote.getYPosInt());
        List<GeoPosition> routeMote = this.pathFinder.retrievePath(graph,beginPosition,destination);
        mote.setPath(routeMote);
        this.routes.put(mote.getEUI(),routeMote);
    }

    /**
     * Handle a route request message by replying with (part of) the route to the requesting device.
     * @param message The message which contains the route request.
     */
    private void handleRouteRequest(LoraWanPacket message) {
        /*
        long deviceEUI = message.getSenderEUI();
        System.out.println("h");
        if(!this.routes.containsKey(deviceEUI))
        {
            environment.getMotes().stream()
                .filter(mote -> mote instanceof UserMote)
                .filter(mote -> mote.getEUI() == deviceEUI)
                .forEach(mote ->
                {
                    GeoPosition destination = ((UserMote) mote).getDestination();
                    setPath(mote, destination);
                });
        }
        */
        System.out.println("h");
        var body = Arrays.stream(Converter.toObjectType(message.getPayload()))
            .skip(1) // Skip the first byte since this indicates the message type
            .collect(Collectors.toList());
        long deviceEUI = message.getSenderEUI();

        GeoPosition motePosition;
        GeoPosition destinationPosition;
        List<GeoPosition> routeMote = new ArrayList<>();




        if (!lastPositions.containsKey(deviceEUI)) {
            // This is the first request the mote has made for a route
            //  -> both the current position as well as the destination of the mote are transmitted
            byte[] rawPositions = new byte[16];
            IntStream.range(0, 16).forEach(i -> rawPositions[i] = body.get(i));

            ByteBuffer byteBuffer = ByteBuffer.wrap(rawPositions);
            motePosition = new GeoPosition(byteBuffer.getFloat(0), byteBuffer.getFloat(4));
            destinationPosition = new GeoPosition(byteBuffer.getFloat(8), byteBuffer.getFloat(12));
            routeMote = this.pathFinder.retrievePath(graph,motePosition,destinationPosition);
            this.routes.put(deviceEUI,routeMote);
        }
        else {
            // The mote has already sent the initial request
            //  -> only the current position of the mote is transmitted (the destination has been stored already)

            byte[] rawPositions = new byte[8];
            IntStream.range(0, 8).forEach(i -> rawPositions[i] = body.get(i));
            ByteBuffer byteBuffer = ByteBuffer.wrap(rawPositions);

            motePosition = new GeoPosition(byteBuffer.getFloat(0), byteBuffer.getFloat(4));
            destinationPosition = routes.get(deviceEUI).get(routes.get(deviceEUI).size()-1);
        }
        routeMote = this.routes.get(deviceEUI);
        routeMote.remove(0);
        routeMote.remove(1);
        this.routes.put(deviceEUI,routeMote);


        int amtPositions = Math.min(routeMote.size() - 1, 3);
        ByteBuffer payloadRaw = ByteBuffer.allocate(8 * amtPositions);

        for (GeoPosition pos : routeMote.subList(1, amtPositions + 1)) {
            payloadRaw.putFloat((float) pos.getLatitude());
            payloadRaw.putFloat((float) pos.getLongitude());
        }

        List<Byte> payload = new ArrayList<>();
        for (byte b : payloadRaw.array()) {
            payload.add(b);
        }

        // Update the position of the mote if it has changed since the previous time
        if (!lastPositions.containsKey(deviceEUI) || !lastPositions.get(deviceEUI).equals(motePosition)) {
            lastPositions.put(deviceEUI, motePosition);
        }

        // Send the reply (via MQTT) to the requesting device
        BasicMqttMessage routeMessage = new BasicMqttMessage(payload);
        this.mqttClient.publish(Topics.getAppToNetServer(message.getReceiverEUI(), deviceEUI), routeMessage);
    }




    /**
     * Get a stored route for a specific mote.
     * @param mote The mote from which the cached route is requested.
     * @return The route as a list of geo coordinates.
     */
    public List<GeoPosition> getRoute(Mote mote) {
        if (routes.containsKey(mote.getEUI())) {
            return routes.get(mote.getEUI());
        }
        return new ArrayList<>();
    }
    private boolean contains(List<?> list, List<?> sublist) {
        return Collections.indexOfSubList(list, sublist) != -1;
    }


    @Override
    public void consumePackets(String topicFilter, TransmissionWrapper transmission) {

    }


    /**
     * Clean the cached routes and mote positions.
     */
    public void clean() {
        this.routes = new HashMap<>();
        this.lastPositions = new HashMap<>();
    }
}
