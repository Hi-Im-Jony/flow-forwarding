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
        final static int UPDATE = 1; // always length 2, first value is id of router to change, second value is updated data
    
    final static int FS_REQUEST = 2;
        final static int REQUESTOR_ID = 3;

        final static int PACKET_HEADER = 4; // header that wraps around multiple header items
            final static int DESTINATION_ID = 5; // end node packet is being sent to
            final static int SOURCE_ID = 6; // source of packet
            final static int PACKET_TYPE = 7; // type of packet (ie, SMS, Image, blah blah)
            
    private static HashMap<String, Integer> forwardingTable;
    private static DatagramSocket socket;
    final static int MTU = 1500;
    public static void main(String[] args) throws SocketException {
        // init
        forwardingTable = new HashMap<>();
        socket = new DatagramSocket(51510);

        System.out.println("Hello from FS");
    }

    private static void forward(byte[] data, String dest) throws IOException{
        if(forwardingTable.containsKey(dest))
            send(data, forwardingTable.get(dest));
        else
            
            contactController(data);
    }

    private static void contactController(byte[] data) throws IOException{
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
        byte headerType = data[0];

        if(headerType == CONTROLLER_REPLY)
            interpretReply(data);
        else if(headerType==PACKET_HEADER)
            interpetHeader(data);

            

        String dest = "";
        forward(data, dest);
        return data;
    }

    public static void interpretReply(byte[] data){
        // TODO
    }

    public static void interpetHeader(byte[] data){
        // TODO
    }

    public static void send(byte[] data, int dest) throws IOException{
        
        InetAddress address= InetAddress.getLocalHost();   
        int port= dest;                       
    
        // create packet addressed to destination
        DatagramPacket packet= new DatagramPacket(data, data.length, address, port);
        socket.send(packet);
    }
}
