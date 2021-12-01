package Router;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;

public class ForwardingService  {
    // header types
    final static int CONTROLLER_REPLY = 0; // packet is a reply from the controller
    final static int COMBINATION = 1; // header type is a combo of other various types
    final static int DESTINATION_ID = 2; // end node packet is being sent to
    final static int SOURCE_ID = 3; // source of packet
    final static int UPDATE = 4; // always length 2, first value is id of router to change, second value is updated data


    
    
    private static HashMap<String, Integer> forwardingTable;
    private static DatagramSocket socket;
    final static int MTU = 1500;
    public static void main(String[] args) throws SocketException {
        // init
        forwardingTable = new HashMap<>();
        socket = new DatagramSocket(51510);

        System.out.println("Hello from FS");
    }

    private static void forward(byte[] data, int dest) throws IOException{
        if(forwardingTable.containsKey(dest))
            send(data, forwardingTable.get(dest));
        else{
            // TODO: dropping packet for now
            // contactController(dest);
        }
            
    }

    private void contactController(String dest) throws IOException{
        // TODO
        // contact controller
    }

    public static byte[] receive() throws IOException{
            
        byte[] data= new byte[MTU];
        DatagramPacket packet= new DatagramPacket(data, data.length);
    
        socket.receive(packet);

        // extract data from packet
        data= packet.getData();

        System.out.println("Node received: \""+data+",\" from: "+packet.getAddress());

        // TODO extrapolate data

        int dest = 0;
        forward(data, dest);
        return data;
    }

    public static void send(byte[] data, int dest) throws IOException{
        
        InetAddress address= InetAddress.getLocalHost();   
        int port= dest;                       
    
        // create packet addressed to destination
        DatagramPacket packet= new DatagramPacket(data, data.length, address, port);
        socket.send(packet);
    }
}
