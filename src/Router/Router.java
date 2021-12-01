package Router;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Router {
    // header types
    final static int CONTROLLER_REPLY = 0; // packet is a reply from the controller
        final static int UPDATE = 1; // always length 2, first value is id of router to change, second value is updated data
    
    final static int FS_REQUEST = 2;
        final static int REQUESTOR_ID = 3;

        final static int MULTI_H = 3; // header that wraps around multiple header items
            final static int DESTINATION_ID = 4; // end node packet is being sent to
            final static int SOURCE_ID = 5; // source of packet
            final static int PACKET_TYPE = 6; // type of packet (ie, SMS, Image, blah blah)
    
    private static String id;
    final static int MTU = 1500;
    final static int FS_PORT = 51510;

    static DatagramSocket socket;

    public static void main(String[] args) throws IOException {
        
        socket = new DatagramSocket();
        id = args[0];
        System.out.println("Hello from Router "+id);

        while(true){
            receive();
        }
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
