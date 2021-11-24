package Node;


public class Node {

    private static class App extends Thread{
    @Override
        public void run(){
            System.out.println("Hello from App");
        }
}

    public static void main(String[] args) {
        App demo = new App();
        demo.run();
        System.out.println("Hello from Node");
    }

    private static void send(){
        // TODO send UDP packets
    }

    private static void receive(){
        // TODO receive UDP packets
    }
}
