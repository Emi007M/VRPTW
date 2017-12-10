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

    private List<List<Node>> path;

    public Vehicle() {
        capacity = totalCapacity;
        path = new ArrayList<>();
    }

    public String readRoute() {
        String ret = "";
        int i = 1;
        double time = 0;
        int initial_capacity = totalCapacity;
        for (List<Node> p : path) {
            // ret += "\n-part " + 1 + ":\n";


            for (int n = 0; n < p.size(); n++) {
                ret += "\ncapacity=" + initial_capacity + "\n";

                if (n != 0) {
                    double dist = Position.getDistance(p.get(n - 1).getPos(), p.get(n).getPos());
                    time += dist;
                    ret += "moving by " + dist + "\n";
                }

                ret += "t:" + time + " - in location " + p.get(n).getId() + " " + p.get(n).getPos() + " with window " + p.get(n).getTimeWindow() + " serving for " + p.get(n).getServiceTime() + "\n";

                if (n != 0) {
                    if (time < p.get(n).getTimeWindow().getFrom()) {
                        time = p.get(n).getTimeWindow().getFrom() + p.get(n).getServiceTime();
                    } else {
                        time += p.get(n).getServiceTime();
                    }
                    ret += "serving finished at " + time + "\n";

                }
                initial_capacity -= p.get(n).getDemand();

            }
        }

        return ret;
    }

    /**
     * made as first, works, but stupid, different results if list of nodes is being shuffled, so don't use it
     *
     * @param depot
     * @param nodes only not visited
     */
    @Deprecated
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

        path.add(single_path);
    }

    @Override
    public int compareTo(Vehicle otherVehicle) {
        double distance = PathRepository.calcTotalDistance(path.get(0));
        double otherDistance = PathRepository.calcTotalDistance(otherVehicle.getPath().get(0));
        return Double.compare(distance, otherDistance);
    }
}
