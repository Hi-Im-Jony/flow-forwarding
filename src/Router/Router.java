package Router;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

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

    final static int CONNECTION_REQUEST = 11; // request from router to connect to another router / make presence known
        // REQUESTOR_ID must be included;
        final static int CONNECT_TO = 12; // router to connect to
    
    final static int APP_ALERT = 13; // an alert from an App to a FS that it wants to receive stuff
        // REQUESTOR_ID must be included;
        final static int STRING = 14; // string to associate with app
    
    
    
            
    private static String name;
    final static int MTU = 1500;
    final static int ROUTER_PORT = 80;
    final static int FS_PORT = 51510;

    static DatagramSocket socket;

    public static void main(String[] args) throws IOException, InterruptedException {

        
        
        socket = new DatagramSocket(ROUTER_PORT);

        name = args[0]; // set name

        ArrayList<byte[]> connectionHeaders = new ArrayList<>(); // prepare to make connections

        // prepare connection headers
        for(int i = 1; i<args.length;i++){
            byte[] header = new byte[2+(args[i].length())];
            int index = 0;
            header[index++] = CONNECT_TO;
            header[index++] = (byte) args[i].length();
            byte[] bytes = args[i].getBytes();
            for(int j = 0; j<bytes.length;j++)
                header[index++]=bytes[j];
            connectionHeaders.add(header);
        }

        // create request header
        int len = 0;
        for(int i = 0; i<connectionHeaders.size();i++)
            len = len + connectionHeaders.get(i).length;

        byte[] nameInBytes = name.getBytes();

        len+=2+(2+nameInBytes.length);    
        byte[] connectionRequest = new byte[len];

        int index = 0;
        connectionRequest[index++] = CONNECTION_REQUEST;
        System.out.println("Connections to request: "+ connectionHeaders.size());
        connectionRequest[index++] =  (byte) (connectionHeaders.size());

        connectionRequest[index++] = REQUESTOR_ID;
        connectionRequest[index++] = (byte) nameInBytes.length;
        for(int i = 0; i<nameInBytes.length;i++)
            connectionRequest[index++] = nameInBytes[i];

        for(int i = 0; i<connectionHeaders.size();i++){
            byte[] currentHeader = connectionHeaders.get(i);
            for(int j = 0; j<currentHeader.length;j++)
                connectionRequest[index++] = currentHeader[j];
        }

        send(connectionRequest, InetAddress.getByName("controller"), 69); // send connection requesto to controller


        System.out.println("Hello from Router "+name);
        
        // hard coding a test for FS
        // byte[] packet = new byte[MTU];

        // index = 0;
        // packet[index++] = PACKET_HEADER;
        // packet[index++] = 0;

        // packet[index++] = DESTINATION_ID;
        // String s = "trinity";
        // byte[] sB = s.getBytes();
        // packet[index++] = (byte) sB.length;
        // for(int i = 0; i<sB.length; i++)
        //     packet[index++] = sB[i];

        // packet[index++] = SOURCE_ID;
        // packet[index++] = 1;
        // packet[index++] = 69;

        // packet[index++] = PACKET_TYPE;
        // packet[index++] = 1;
        // packet[index++] = 1;

        // packet[index++] = PACKET;
        // String p = "TESTING FS";
        // byte[] pB = p.getBytes();
        // packet[index++] = (byte) pB.length;
        // for(int i = 0; i<pB.length; i++)
        //     packet[index++] = pB[i];

        
        
        // while(true){
        //     send(packet, InetAddress.getByName("fs"), FS_PORT);
        //     Thread.sleep(2000);
        // }
    }

    public static byte[] receive() throws IOException{
          
        byte[] data= new byte[MTU];
        DatagramPacket packet= new DatagramPacket(data, data.length);
    
        socket.receive(packet);

        // extract data from packet
        data= packet.getData();

        System.out.println("Router received: \""+data+",\" from: "+packet.getAddress());
        
        InetAddress address= InetAddress.getByName("fs");
        send(data, address, FS_PORT); // send to forwarding service
        return data;
    }

    public static void send(byte[] data, InetAddress address, int port) throws IOException{
        
        // create packet addressed to destination
        DatagramPacket packet= new DatagramPacket(data, data.length, address, port);
        socket.send(packet);
    }
}
