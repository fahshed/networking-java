import java.util.ArrayList;
import java.util.Random;

//Work needed
public class Client {
    public static void main(String[] args) throws InterruptedException {
        NetworkUtility networkUtility = new NetworkUtility("127.0.0.1", 4444);
        System.out.println("Connected to server");

        EndDevice config = (EndDevice) networkUtility.read();
        System.out.println("ID " + config.getDeviceID() + " IP " + config.getIpAddress());

        ArrayList<EndDevice> endDevices = new ArrayList<>();
        int size = (int) networkUtility.read();
        for(int i=0; i<size; i++) {
            endDevices.add((EndDevice) networkUtility.read());
        }

        int drops = 0;
        int totalHops = 0;
        for(int i=0; i<100; i++) {
            Random random = new Random();
            EndDevice recipient = endDevices.get(random.nextInt(size));

            if(i==20) {
                Packet packet = new Packet("Hello_World", "SHOW_ROUTE", config.getIpAddress(), recipient.getIpAddress());
                networkUtility.write(packet);

                String status = (String) networkUtility.read();
                if(status.equals("Success")) {
                    Packet returnPacket = (Packet) networkUtility.read();
                    System.out.println("\nPacket 20\nSuccess\nHop Count " + returnPacket.hopcount);
                    totalHops = totalHops + returnPacket.hopcount;

                    for(int r: returnPacket.routersInPath) {
                        System.out.print(r + " -> ");
                    }
                    System.out.println("\n");
                    for(String table: returnPacket.routingTables) {
                        System.out.println(table);
                    }
                } else {
                    drops++;
                    System.out.println("\nPacket 20");
                    System.out.println(status);
                    System.out.println();
                }
            } else {
                Packet packet = new Packet("Hello_World", "", config.getIpAddress(), recipient.getIpAddress());
                networkUtility.write(packet);

                String status = (String) networkUtility.read();
                if(status.equals("Success")) {
                    Packet returnPacket = (Packet) networkUtility.read();
                    totalHops = totalHops + returnPacket.hopcount;
                } else {
                    drops++;
                }
            }
        }

        if(drops!=100) {
            double avgHops = Math.round((double)totalHops/(double)(100-drops)*100.0)/100.0;
            System.out.println("Average Hops " + avgHops);
        }
        System.out.println("Drop Rate " + drops + "%");

        networkUtility.closeConnection();


        /**
         * Tasks
         */
        
        /*
        1. Receive EndDevice configuration from server
        2. Receive active client list from server
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the message and recipient IP address to server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
                            all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
                    Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
        */
    }
}
