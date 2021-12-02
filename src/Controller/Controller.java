package Controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class Controller {
    // header types
    final static int CONTROLLER_REPLY = 0; // wraps header to signify packet is a reply from Controller
        final static int UPDATE = 1; // always length 2, first value is id of router to change, second value is updated data
            final static int ROUTER_ID = 2; // ID of router that will be updated
            final static int UPDATED_VAL = 3; // value to update to
    
    final static int FS_REQUEST = 4; // wraps header to signify that packet is a request from a Forwarding Service
        final static int QUERY = 5; // name of router we are asking about
        
        final static int PACKET_HEADER = 6; // wraps packets header info
            final static int DESTINATION_ID = 7; // ID of final destination
            final static int SOURCE_ID = 9; // ID of initial source
            final static int PACKET_TYPE = 10; // Type of packet being transmitted (irrelevant for assignment but need irl)
            final static int PACKET = 11; // the actual packet

    final static int CONNECTION_REQUEST = 12; // request from router to connect to another router / make presence known
        final static int REQUESTOR_NAME = 13; // ID of FS
        final static int CONNECT_TO = 14; // router to connect to
    
    final static int APP_ALERT = 15; // an alert from an App to a FS that it wants to receive stuff
        // REQUESTOR_NAME must be included;
        final static int STRING = 16; // string to associate with app
        
    
    private static HashMap<String, ArrayList<String>> connections;   
    private static HashMap<String, InetAddress> addresses; 
    
    static DatagramSocket socket;
    
    final static int MTU = 1500;
    public static void main(String[] args) throws IOException {
        // init stuff
        connections = new HashMap<>();
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

    private static void executeFsRequest(byte[] data, InetAddress address){
        int index = 0;
        if(data[index++]!=FS_REQUEST)
            return;
        index++; // can skip L bit
        if(data[index++]!= REQUESTOR_NAME)
            return;
        int reqLen = data[index++];
        byte[] reqId = new byte[reqLen];

        
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
