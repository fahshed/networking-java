import java.util.ArrayList;
import java.util.Random;

public class ServerThread implements Runnable {
    private NetworkUtility networkUtility;
    private EndDevice endDevice;

    ServerThread(NetworkUtility networkUtility, EndDevice endDevice) {
        this.networkUtility = networkUtility;
        this.endDevice = endDevice;
        System.out.println("Server Ready for client " + NetworkLayerServer.clientCount + "\n");
        new Thread(this).start();
    }

    @Override
    public void run() {
        this.networkUtility.write(this.endDevice);

        this.networkUtility.write(NetworkLayerServer.endDevices.size());
        for(EndDevice device: NetworkLayerServer.endDevices) {
            this.networkUtility.write(device);
        }

        for(int i=0; i<100; i++) {
            Packet packet = (Packet) this.networkUtility.read();

            boolean success =this.deliverPacket(packet);

            if(success) {
                this.networkUtility.write("Success");
                this.networkUtility.write(packet);
            } else {
                this.networkUtility.write("Failure");
            }
        }

        this.networkUtility.closeConnection();

        /**
         * Synchronize actions with client.
         */
        /*
        Tasks:
        1. Upon receiving a packet and recipient, call deliverPacket(packet)
        2. If the packet contains "SHOW_ROUTE" request, then fetch the required information
                and send back to client
        3. Either send acknowledgement with number of hops or send failure message back to client
        */
    }


    public boolean deliverPacket(Packet p) {
        EndDevice sourceDevice = NetworkLayerServer.endDeviceMap.get(p.getSourceIP());
        int sourceRouterId = NetworkLayerServer.interfacetoRouterID.get(sourceDevice.getGateway());
        Router s = NetworkLayerServer.routerMap.get(sourceRouterId);

        EndDevice destinationDevice = NetworkLayerServer.endDeviceMap.get(p.getDestinationIP());
        int destinationRouterId = NetworkLayerServer.interfacetoRouterID.get(destinationDevice.getGateway());
        Router d = NetworkLayerServer.routerMap.get(destinationRouterId);

        int currentRouterId = s.getRouterId();
        Router currentRouter = NetworkLayerServer.routerMap.get(currentRouterId);
        if(!currentRouter.getState()) return false;

        if(p.getSpecialMessage().equals("SHOW_ROUTE")) {
            System.out.println("From " + s.getRouterId() + " To " + d.getRouterId());
            p.routersInPath.add(s.getRouterId());
            p.routingTables.add(s.strRoutingTable());
        }

        while(currentRouterId!=d.getRouterId()) {
            RoutingTableEntry entry = null;

            for(RoutingTableEntry e: currentRouter.getRoutingTable()) {
                if(e.getRouterId()==d.getRouterId()) {
                    entry = e;
                    break;
                }
            }

            int gatewayRouterId = entry.getGatewayRouterId();
            Router gatewayRouter = NetworkLayerServer.routerMap.get(gatewayRouterId);

            if(entry.getDistance()==Constants.INFINITY) return false;

            if(!gatewayRouter.getState()) {
                entry.setDistance(Constants.INFINITY);

                RouterStateChanger.islocked = true;
                //NetworkLayerServer.simpleDVR(currentRouterId);
                NetworkLayerServer.DVR(currentRouterId);
                RouterStateChanger.islocked = false;
                synchronized (RouterStateChanger.msg) {
                    RouterStateChanger.msg.notify();
                }

                return false;
            }

            for(RoutingTableEntry e: gatewayRouter.getRoutingTable()) {
                if(e.getRouterId()==currentRouterId && e.getDistance()==Constants.INFINITY) {
                    e.setDistance(1);

                    RouterStateChanger.islocked = true;
                    //NetworkLayerServer.simpleDVR(gatewayRouterId);
                    NetworkLayerServer.DVR(gatewayRouterId);
                    RouterStateChanger.islocked = false;
                    synchronized (RouterStateChanger.msg) {
                        RouterStateChanger.msg.notify();
                    }
                }
            }

            currentRouterId = gatewayRouterId;
            currentRouter = gatewayRouter;
            p.hopcount++;   /** Delivering packet to gateway router **/
            if(p.getSpecialMessage().equals("SHOW_ROUTE")) {
                p.routersInPath.add(currentRouterId);
                p.routingTables.add(currentRouter.strRoutingTable());
            }

            if(p.hopcount==Constants.INFINITY) return false;
        }

        if(p.getSpecialMessage().equals("SHOW_ROUTE")) {
            if(s.getRouterId()==d.getRouterId()) {
                p.routersInPath.add(d.getRouterId());
            } else {
                p.routingTables.remove(p.routingTables.size() - 1);
            }
        }

        return true;

        /*
        1. Find the router s which has an interface
                such that the interface and source end device have same network address.
        2. Find the router d which has an interface
                such that the interface and destination end device have same network address.
        3. Implement forwarding, i.e., s forwards to its gateway router x considering d as the destination.
                similarly, x forwards to the next gateway router y considering d as the destination,
                and eventually the packet reaches to destination router d.

            3(a) If, while forwarding, any gateway x, found from routingTable of router r is in down state[x.state==FALSE]
                    (i) Drop packet
                    (ii) Update the entry with distance Constants.INFTY
                    (iii) Block NetworkLayerServer.stateChanger.t
                    (iv) Apply DVR starting from router r.
                    (v) Resume NetworkLayerServer.stateChanger.t

            3(b) If, while forwarding, a router x receives the packet from router y,
                    but routingTableEntry shows Constants.INFTY distance from x to y,
                    (i) Update the entry with distance 1
                    (ii) Block NetworkLayerServer.stateChanger.t
                    (iii) Apply DVR starting from router x.
                    (iv) Resume NetworkLayerServer.stateChanger.t

        4. If 3(a) occurs at any stage, packet will be dropped,
            otherwise successfully sent to the destination router
        */
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }
}
