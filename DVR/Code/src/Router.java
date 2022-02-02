//Work needed
import java.util.*;

public class Router {
    private int routerId;
    private int numberOfInterfaces;
    private ArrayList<IPAddress> interfaceAddresses; //list of IP address of all interfaces of the router
    private ArrayList<RoutingTableEntry> routingTable; //used to implement DVR
    private ArrayList<Integer> neighborRouterIDs; //Contains both "UP" and "DOWN" state routers
    private Boolean state; //true represents "UP" state and false is for "DOWN" state
    private Map<Integer, IPAddress> gatewayIDtoIP;

    public Router() {
        interfaceAddresses = new ArrayList<>();
        routingTable = new ArrayList<>();
        neighborRouterIDs = new ArrayList<>();

        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p < 0.80) state = true;
        else state = false;

        numberOfInterfaces = 0;
    }

    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddresses, Map<Integer, IPAddress> gatewayIDtoIP) {
        this.routerId = routerId;
        this.interfaceAddresses = interfaceAddresses;
        this.neighborRouterIDs = neighborRouters;
        this.gatewayIDtoIP = gatewayIDtoIP;
        routingTable = new ArrayList<>();

        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        state = p < 0.80;

        numberOfInterfaces = interfaceAddresses.size();
    }

    @Override
    public String toString() {
        String string = "";
        string += "Router ID: " + routerId + "\n" + "Interfaces: \n";
        for (int i = 0; i < numberOfInterfaces; i++) {
            string += interfaceAddresses.get(i).getString() + "\t";
        }
        string += "\n" + "Neighbors: \n";
        for(int i = 0; i < neighborRouterIDs.size(); i++) {
            string += neighborRouterIDs.get(i) + "\t";
        }
        return string;
    }


    /**
     * Initialize the distance(hop count) for each router.
     * for itself, distance=0; for any connected router with state=true, distance=1; otherwise distance=Constants.INFTY;
     */
    public void initiateRoutingTable() {
        for(Router router: NetworkLayerServer.routers) {
            RoutingTableEntry entry;

            if(router.getState()) {
                if(router.getRouterId() == this.routerId) {
                    entry = new RoutingTableEntry(this.routerId,0, this.routerId);
                    this.routingTable.add(entry);
                } else if(this.neighborRouterIDs.contains(router.getRouterId())) {
                    entry = new RoutingTableEntry(router.getRouterId(),1, router.getRouterId());
                    this.routingTable.add(entry);
                } else {
                    entry = new RoutingTableEntry(router.getRouterId(), Constants.INFINITY, -1);
                    this.routingTable.add(entry);
                }

            } else {
                entry = new RoutingTableEntry(router.getRouterId(), Constants.INFINITY, -1);
                this.routingTable.add(entry);
            }
        }
    }

    /**
     * Delete all the routingTableEntry
     */
    public void clearRoutingTable() {
        this.routingTable.clear();
    }


    /**
     * Update the routing table for this router using the entries of Router neighbor
     * @param neighbor
     */
    public boolean updateRoutingTable(Router neighbor) {
        boolean change = false;

        double neighborDistance = 0;
        for(RoutingTableEntry entry: this.routingTable) {
            if(entry.getRouterId()==neighbor.getRouterId()) {
                neighborDistance = entry.getDistance();
            }
        }

        for(RoutingTableEntry entry: this.routingTable) {
            double neighborEntryDistance = 0;
            for(RoutingTableEntry e: neighbor.getRoutingTable()) {
                if(e.getRouterId()==entry.getRouterId()) {
                    neighborEntryDistance = e.getDistance();
                }
            }

            double newDistance =  neighborDistance + neighborEntryDistance;

            if(newDistance<entry.getDistance()) {
                entry.setDistance(newDistance);
                entry.setGatewayRouterId(neighbor.getRouterId());
                change = true;
            }
        }

        return change;
    }

    public boolean sfupdateRoutingTable(Router neighbor) {
        boolean change = false;

        double neighborDistance = 0;
        for(RoutingTableEntry entry: this.routingTable) {
            if(entry.getRouterId()==neighbor.getRouterId()) {
                neighborDistance = entry.getDistance();
            }
        }

        for(RoutingTableEntry entry: this.routingTable) {
            double neighborEntryDistance = 0;
            int neighborEntryGateway = 0;
            for(RoutingTableEntry e: neighbor.getRoutingTable()) {
                if(e.getRouterId()==entry.getRouterId()) {
                    neighborEntryDistance = e.getDistance();
                    neighborEntryGateway = e.getGatewayRouterId();
                }
            }

            double newDistance =  neighborDistance + neighborEntryDistance;

            if(entry.getGatewayRouterId()==neighbor.getRouterId()
                    || (newDistance<entry.getDistance() && this.routerId!=neighborEntryGateway)) {
                entry.setDistance(newDistance);
                entry.setGatewayRouterId(neighbor.getRouterId());

                if(newDistance<entry.getDistance()) {
                    change = true;
                }
            }
        }

        return change;
    }

    /**
     * If the state was up, down it; if state was down, up it
     */
    public void revertState() {
        state = !state;
        if(state) { initiateRoutingTable(); }
        else { clearRoutingTable(); }
    }

    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public int getNumberOfInterfaces() {
        return numberOfInterfaces;
    }

    public void setNumberOfInterfaces(int numberOfInterfaces) {
        this.numberOfInterfaces = numberOfInterfaces;
    }

    public ArrayList<IPAddress> getInterfaceAddresses() {
        return interfaceAddresses;
    }

    public void setInterfaceAddresses(ArrayList<IPAddress> interfaceAddresses) {
        this.interfaceAddresses = interfaceAddresses;
        numberOfInterfaces = interfaceAddresses.size();
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return this.routingTable;
    }

    public void addRoutingTableEntry(RoutingTableEntry entry) {
        this.routingTable.add(entry);
    }

    public ArrayList<Integer> getNeighborRouterIDs() {
        return neighborRouterIDs;
    }

    public void setNeighborRouterIDs(ArrayList<Integer> neighborRouterIDs) { this.neighborRouterIDs = neighborRouterIDs; }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Map<Integer, IPAddress> getGatewayIDtoIP() { return gatewayIDtoIP; }

    public void printRoutingTable() {
        System.out.println("Router " + routerId);
        System.out.println("DestID Distance Nexthop");
        for (RoutingTableEntry routingTableEntry : routingTable) {
            System.out.println(routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId());
        }
        System.out.println("-----------------------");
    }

    public String strRoutingTable() {
        String string = "Router" + routerId + "\n";
        string += "DestID Distance Nexthop\n";
        for (RoutingTableEntry routingTableEntry : routingTable) {
            string += routingTableEntry.getRouterId() + " " + routingTableEntry.getDistance() + " " + routingTableEntry.getGatewayRouterId() + "\n";
        }

        string += "-----------------------\n";
        return string;
    }
}
