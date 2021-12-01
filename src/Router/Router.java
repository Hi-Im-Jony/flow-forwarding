package Router;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Router {
    private static String id;
    final static int MTU = 1500;
    final static int FS_PORT = 51510;

    static DatagramSocket socket;

    public static void main(String[] args) throws SocketException {
        
        socket = new DatagramSocket();
        id = args[0];
        System.out.println("Hello from Router "+id);
        RouterThread thread = new RouterThread();
        thread.start();
    }

    private static class RouterThread extends Thread{
        @Override
        public void run() {
            RouterThread thread = new RouterThread();
            thread.start();

            while(true){
                try {
                    receive();

                } 
                catch (IOException e) {e.printStackTrace();}
            }
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
        send(data, address, FS_PORT);
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
