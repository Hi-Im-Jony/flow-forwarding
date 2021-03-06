/*
    @author Cornel Jonathan Cicai
    Student Number: 19335265
*/

package Controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Controller {

    // header items
    final static int CONTROLLER_REPLY = -1; // wraps header to signify packet is a reply from Controller
    final static int APP_ALERT = -15; // an alert from an App to a FS that it wants to receive stuff
        final static int UPDATE = -2; // always length 2, first value is id of router to change, second value is updated data
            final static int UPDATE_KEY = -3; // ID of router that will be updated
            final static int UPDATED_VAL = -4; // value to update to
    
    final static int FS_REQUEST = -5; // wraps header to signify that packet is a request from a Forwarding Service
        final static int QUERY = -6; // appName of router we are asking about
        // include REQUESTOR_NAME (declared further below)
        
        final static int PACKET_HEADER = -7; // wraps packets header info
            final static int DESTINATION_ID = -8; // ID of final destination
            final static int SOURCE_ID = -9; // ID of initial source
            final static int PACKET_TYPE = -10; // Type of packet being transmitted (irrelevant for assignment but need irl)
            final static int PACKET = -11; // the actual packet

    final static int CONNECTION_REQUEST = -12; // request from router to connect to another router / make presence known
        final static int REQUESTOR_NAME = -13; // ID of FS
        final static int CONNECT_TO = -14; // router to connect to
    
    private static HashMap<String, ArrayList<String>> connections;   
    private static HashMap<String, InetAddress> addresses; 
    
    static DatagramSocket socket;
    
    final static int FS_PORT = 51510;
    final static int MTU = 1460;
    public static void main(String[] args) throws IOException {
        // init stuff
        connections = new HashMap<>();
        addresses = new HashMap<>();
        socket = new DatagramSocket(42);

        System.out.println("Hello from Controller");
        receive();
    }

    private static class ControllerThread extends Thread{
        @Override
        public void run() {
            try {
                receive();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void executeFsRequest(byte[] data, InetAddress reqAddress) throws IOException{
        int index = 0;
        if(data[index++]!=FS_REQUEST)
            return;
        index++; // can skip L bit

        if(data[index++]!= QUERY)
            return;
        int queryLen = data[index++];
        byte[] queryB = new byte[queryLen];
        for(int i = 0; i<queryB.length;i++)
            queryB[i] = data[index++];
        String query= new String(queryB);

        if(data[index++] != REQUESTOR_NAME)
            return;
        int reqLen = data[index++];
        byte[] reqB = new byte[reqLen];
        for(int i = 0; i< reqB.length;i++){
            reqB[i] = data[index++];
        }
        String requestor = new String(reqB);

        System.out.println("Requestor router: "+requestor);
        System.out.println("Dest router: "+query);

        String next = findNext(requestor,query);
        System.out.println("Finished findNext()");
        if(next == null)
            return; 
        System.out.println("Next node is: "+next);
        InetAddress nextAddress = addresses.get(next);
        // create reply
        byte[] reply = new byte[2+(2+(2+query.length())+(2+next.length())+data.length-index)];
        int replyIndex = 0;
        reply[replyIndex++] = CONTROLLER_REPLY;
        reply[replyIndex++] = 0;

        reply[replyIndex++] = UPDATE;
        reply[replyIndex++] = 0;

        reply[replyIndex++] = UPDATE_KEY;
        reply[replyIndex++] = (byte) query.length();
        for(int i = 0; i< queryB.length;i++)
            reply[replyIndex++] = queryB[i];
        
        reply[replyIndex++] = UPDATED_VAL;
        
        byte[] nextB = nextAddress.getAddress();
        reply[replyIndex++] = (byte) nextB.length;
        for(int i = 0; i<nextB.length;i++){
            reply[replyIndex++] =nextB[i];
        }

        byte packetHeaderLen = data[index+1];
        // copy rest of data into reply
        for(int i = index; i<packetHeaderLen+index;i++){
            byte x = data[i];
            reply[replyIndex++] = x;
        }

        send(reply, reqAddress, FS_PORT);

    }

    private static String findNext(String start, String end){
        if(!(addresses.containsKey(start) && addresses.containsKey(end)))
            return null;
        
        // prep work for dijkstra
        HashMap<String, Boolean> visitedNodes = new HashMap<>();
        for(Entry<String, InetAddress> pair : addresses.entrySet())
            visitedNodes.put(pair.getKey(), false);

        HashMap<String, HashMap<String,Double>> distances = new HashMap<>();
        for(Entry<String, InetAddress> map : addresses.entrySet()){
            HashMap<String, Double> distanceMap = new HashMap<>(); // create new map
                for(Entry<String, InetAddress> node : addresses.entrySet())
                    distanceMap.put(node.getKey(), Double.POSITIVE_INFINITY); // init all values to infinity
            distances.put(map.getKey(), distanceMap); // add to map of maps
        }

        // let distance from start to start = 0
        distances.get(start).put(start, 0.0);
        
        HashMap<String,String> previousNodes = new HashMap<>();
        for(Entry<String, InetAddress> pair : addresses.entrySet())
            previousNodes.put(pair.getKey(), "");

        runDijkstra(start, start, end, visitedNodes, distances, previousNodes);
        
        // get path from previousNodes
        String currentNode = end;
        String previousNode = null;
        int counter = 0;
        while(counter<previousNodes.size()){
            previousNode = previousNodes.get(currentNode);
            if(previousNode != null && previousNode.equals(start))
                return currentNode;
            currentNode = previousNodes.get(currentNode);
            counter++;
        }
        System.out.println("Routing failed");
        return null;
    }


    private static void runDijkstra(String start, String current, String end, 
    HashMap<String, Boolean> visitedNodes, HashMap<String, HashMap<String,Double>> distances, HashMap<String,String> previousNodes){
        visitedNodes.put(current, true);

        System.out.println("Current is: "+current);
        if(current.equals(end))
            return;

        ArrayList<String> neighbours = connections.get(current);
                
        if(neighbours!=null)
        for(String neighbour: neighbours){
            
            double newDistance = distances.get(start).get(current)+1; // get current distance
            double knownDistance = distances.get(start).get(neighbour); // get known distance
            

            double smallestDistance = (knownDistance>newDistance)? newDistance:knownDistance; // check which is smallest
            distances.get(start).put(neighbour, smallestDistance); // update table

            if(smallestDistance<knownDistance)
                previousNodes.put(neighbour, current); // update previous vertex if needed
        }

        // check if we are done
        boolean allVisited = true;
        for(boolean visited : visitedNodes.values())
            allVisited = allVisited && visited;

        if(allVisited)
            return;

        // find closest unvisited
        String closest ="";
        for(String node : visitedNodes.keySet()){
            if(!visitedNodes.get(node)){
                if(closest.equals(""))
                    closest = node;
                else if(distances.get(start).get(closest)>distances.get(start).get(node))
                    closest = node;
            }
        }
        current = closest;
        runDijkstra(start, current, end, visitedNodes, distances, previousNodes);
        
    }

    

    private static void connect(byte[] data, InetAddress reqAddress){
        int index = 0;
        if(data[index++]!=CONNECTION_REQUEST)
            return;

        int numOfConnections = data[index++];

        if(data[index++]!=REQUESTOR_NAME)
            return;
        int requestorLen = data[index++];
        byte[] requestor = new byte[requestorLen];
        for(int i = 0;i<requestorLen;i++){
            requestor[i] = data[index++];
        }
        

        // prepare to add connections
        ArrayList<String> routersConnections = connections.get(new String(requestor));
        if(routersConnections==null)
            routersConnections = new ArrayList<>();

        for(int i = 0; i<numOfConnections;i++){
            if(data[index++] != CONNECT_TO)
                return;
            int nameLen = data[index++];
            byte[] name = new byte[nameLen] ;
            for(int j = 0; j<nameLen;j++){
                name[j] = data[index++];
            }
                
            
            String connection = new String(name);
            System.out.println("Connecting: "+new String(requestor)+" to "+connection);
            routersConnections.add(connection);
        }

        connections.put(new String(requestor), routersConnections);
        addresses.put(new String(requestor), reqAddress);
    }

    private static void receive() throws IOException{
        ControllerThread backup = new ControllerThread();

        byte[] data= new byte[MTU];
        DatagramPacket packet= new DatagramPacket(data, data.length);
    
        socket.receive(packet);
        backup.start();

        // extract data from packet
        data= packet.getData();
        System.out.println("Controller received: \""+new String(data)+",\" from: "+packet.getAddress());

        int dataType = data[0];
        
        if(dataType==FS_REQUEST)
            executeFsRequest(data, packet.getAddress());
        else if(dataType==CONNECTION_REQUEST)
            connect(data,packet.getAddress());
    }
    private static void send(byte[] data, InetAddress address, int port) throws IOException{
        
        // create packet addressed to destination
        DatagramPacket packet= new DatagramPacket(data, data.length, address, port);
        socket.send(packet);
    }

}
