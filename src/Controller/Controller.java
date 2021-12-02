package Controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class Controller {
    // header types
    final static int CONTROLLER_REPLY = 0; // wraps header to signify packet is a reply from Controller
        final static int UPDATE = 1; // always length 2, first value is id of router to change, second value is updated data
            final static int ROUTER_ID = 2; // ID of router that will be updated
            final static int UPDATED_VAL = 3; // value to update to
    
    final static int FS_REQUEST = 4; // wraps header to signify that packet is a request from a Forwarding Service
        final static int REQUESTOR_ID = 5; // ID of FS

        final static int PACKET_HEADER = 6; // wraps packets header info
            final static int DESTINATION_ID = 7; // ID of final destination
            final static int SOURCE_ID = 8; // ID of initial source
            final static int PACKET_TYPE = 9; // Type of packet being transmitted (irrelevant for assignment but need irl)
            final static int PACKET = 10; // the actual packet

    final static int CONNECTION_REQUEST = 11; // request from router to connect to another router / make presence known
        // REQUESTOR_ID must be included;
        final static int CONNECT_TO = 12; // router to connect to
    
    final static int APP_ALERT = 13; // an alert from an App to a FS that it wants to receive stuff
        // REQUESTOR_ID must be included;
        final static int STRING = 14; // string to associate with app
        

    static DatagramSocket socket;
    
    final static int MTU = 1500;
    public static void main(String[] args) throws IOException {
        // init stuff
        socket = new DatagramSocket(69);

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

    private static void executeFsRequest(byte[] data){

    }

    private static void connect(byte[] data){

    }

    private static void receive() throws IOException{
        ControllerThread backup = new ControllerThread();

        byte[] data= new byte[MTU];
        DatagramPacket packet= new DatagramPacket(data, data.length);
    
        socket.receive(packet);
        backup.start();

        // extract data from packet
        data= packet.getData();
        System.out.println("Controller received: \""+data+",\" from: "+packet.getAddress());

        int dataType = data[0];
        
        if(dataType==FS_REQUEST)
            executeFsRequest(data);
        else if(dataType==CONNECTION_REQUEST)
            connect(data);
            
        
    }
    private static void send(byte[] data, InetAddress address, int port) throws IOException{
        
        // create packet addressed to destination
        DatagramPacket packet= new DatagramPacket(data, data.length, address, port);
        socket.send(packet);
    }

}
