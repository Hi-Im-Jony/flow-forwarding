package Controller;

import java.util.HashMap;

public class Controller {
    // header types
    final static int CONTROLLER_REPLY = 0; // packet is a reply from the controller
        final static int UPDATE = 1; // always length 2, first value is id of router to change, second value is updated data
    
    final static int FS_REQUEST = 2;
        final static int REQUESTOR_ID = 3;

        final static int PACKET_HEADER = 3; // header that wraps around multiple header items
            final static int DESTINATION_ID = 4; // end node packet is being sent to
            final static int SOURCE_ID = 5; // source of packet
            final static int PACKET_TYPE = 6; // type of packet (ie, SMS, Image, blah blah)

    HashMap<String, // Dest
            HashMap<Integer, // Src
                    HashMap<Integer, // Router
                        HashMap<String, // In
                                Integer /* Out */>>>> table;
    public static void main(String[] args) {
        // TODO    
    }

    private static void receive(){
        // TODO
    }
    private static void send(){
        // TODO
    }

}
