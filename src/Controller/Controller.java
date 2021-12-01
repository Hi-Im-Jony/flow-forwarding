package Controller;

import java.util.HashMap;

public class Controller {
    // header types
    final static int CONTROLLER_REPLY = 0; // packet is a reply from the controller
    final static int COMBINATION = 1; // header type is a combo of other various types
    final static int DESTINATION_ID = 2; // end node packet is being sent to
    final static int SOURCE_ID = 3; // source of packet
    final static int UPDATE = 4; // always length 2, first value is id of router to change, second value is updated data

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
