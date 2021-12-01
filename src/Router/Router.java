package Router;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Router {
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
    
    
            
    private static String id;
    final static int MTU = 1500;
    final static int ROUTER_PORT = 80;
    final static int FS_PORT = 51510;

    static DatagramSocket socket;

    public static void main(String[] args) throws IOException, InterruptedException {

        
        
        socket = new DatagramSocket(ROUTER_PORT);
        id = args[0];
        System.out.println("Hello from Router "+id);
        Thread.sleep(2000);
        // hard coding a test for FS
        byte[] packet = new byte[MTU];

        int index = 0;
        packet[index++] = PACKET_HEADER;
        packet[index++] = 0;

        packet[index++] = DESTINATION_ID;
        String s = "trinity";
        byte[] sB = s.getBytes();
        packet[index++] = (byte) sB.length;
        for(int i = 0; i<sB.length; i++)
            packet[index++] = sB[i];

        packet[index++] = SOURCE_ID;
        packet[index++] = 1;
        packet[index++] = 69;

        packet[index++] = PACKET_TYPE;
        packet[index++] = 1;
        packet[index++] = 1;

        packet[index++] = PACKET;
        String p = "TESTING FS";
        byte[] pB = p.getBytes();
        packet[index++] = (byte) pB.length;
        for(int i = 0; i<pB.length; i++)
            packet[index++] = pB[i];

        System.out.println("Sending to FS");
        send(packet, InetAddress.getLocalHost(), FS_PORT);
    }

    public static byte[] receive() throws IOException{
          
        byte[] data= new byte[MTU];
        DatagramPacket packet= new DatagramPacket(data, data.length);
    
        socket.receive(packet);

        // extract data from packet
        data= packet.getData();

        System.out.println("Router received: \""+data+",\" from: "+packet.getAddress());
        
        InetAddress address= InetAddress.getLocalHost();
        send(data, address, FS_PORT); // send to forwarding service
        return data;
    }

    public static void send(byte[] data, InetAddress address, int dest) throws IOException{
        
        // InetAddress address= InetAddress.getLocalHost();   
        int port= dest;                       
    
        // create packet addressed to destination
        DatagramPacket packet= new DatagramPacket(data, data.length, address, port);
        socket.send(packet);
    }
}
