package vrptw;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import vrptw.node.Node;
import vrptw.node.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class Vehicle implements Comparable<Vehicle>{

    public static int totalCapacity;
    private int capacity;

    private List<Node> path;

    public Vehicle() {
        capacity = totalCapacity;
        path = new ArrayList<>();
    }
    public Vehicle(Vehicle otherVehicle) {
        this.capacity = otherVehicle.capacity;
        this.path = new ArrayList<>(otherVehicle.path);
    }
    public void setPath(List<Node> path) {
        this.path = path;
        int newCapacity = this.capacity;
        for (Node customer : path) {
            newCapacity -= customer.getDemand();
        }
        this.setCapacity(newCapacity);
    }
    public String readRoute() {
        String ret = "";
        int i = 1;
        double time = 0;
        int initial_capacity = totalCapacity;
        for (int index = 0; index < path.size(); index ++) {
            // ret += "\n-part " + 1 + ":\n";
           ret += "\ncapacity=" + initial_capacity + "\n";

            if (index != 0) {
                double dist = Position.getDistance(path.get(index - 1).getPos(), path.get(index).getPos());
                time += dist;
                ret += "moving by " + dist + "\n";
            }

            ret += "t:" + time + " - in location " + path.get(index).getId() + " " + path.get(index).getPos() +
                    " with window " + path.get(index).getTimeWindow() + " serving for " + path.get(index).getServiceTime() + "\n";

            if (index != 0) {
                if (time < path.get(index).getTimeWindow().getFrom()) {
                    time = path.get(index).getTimeWindow().getFrom() + path.get(index).getServiceTime();
                } else {
                    time += path.get(index).getServiceTime();
                }
                ret += "serving finished at " + time + "\n";

            }
            initial_capacity -= path.get(index).getDemand();

        }
        return ret;
    }

    /**
     * made as first, works, but stupid, different results if list of nodes is being shuffled, so don't use it
     *
     * @param depot
     * @param nodes only not visited
     */
    // @Deprecated - not any more
    public void visitAsMuchAsYouCanWhateverHow(Node depot, List<Node> nodes) {

        List<Node> single_path = new ArrayList<>();
        single_path.add(depot); //adding depot
        double total_time = 0;
        int depot_deadline = depot.getTimeWindow().getTo();
        for (Node n : nodes) {
            if (capacity == 0) {
                return;
            }
            if (capacity < n.getDemand()) {
                continue;
            } //if don't have enough capacity left
            double dist = Position.getDistance(single_path.get(single_path.size() - 1).getPos(), n.getPos());
            double depot_dist = Position.getDistance(depot.getPos(), n.getPos());
            double service_end_time = total_time + dist;
            if (service_end_time <= n.getTimeWindow().getTo()) { //if will get on time for time window
                if (service_end_time < n.getTimeWindow().getFrom()) {
                    service_end_time = n.getTimeWindow().getFrom() + n.getServiceTime();
                } else {
                    service_end_time += n.getServiceTime();
                }

                if ((service_end_time + depot_dist) > depot_deadline) { // if won't get to depot on time
                    continue;
                }

                //add
                n.setVisited(true);
                single_path.add(n);
                total_time = service_end_time;
                capacity -= n.getDemand();


            }


        }

        single_path.add(depot);

        path = single_path;
    }

    @Override
    public int compareTo(Vehicle otherVehicle) {
        double distance = PathRepository.calcTotalDistance(path);
        double otherDistance = PathRepository.calcTotalDistance(otherVehicle.getPath());
        return Double.compare(distance, otherDistance);
    }

}
