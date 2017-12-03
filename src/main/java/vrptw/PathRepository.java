package vrptw;

import vrptw.node.Node;
import vrptw.node.Position;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static vrptw.node.Position.*;

public class PathRepository {


    /**
     * @return recursively next best part of the path to be added;
     * used for looking for the best path for a single track (most customers, shortest distance)
     */
    public static List<Node> constructPath(List<Node> currentPath, Node depot, List<Node> customers, int currentCapacity, double currentDistance, double currentTime) {
        List<Node> bestNextPath = new ArrayList<>();
        if (currentCapacity == 0 || customers.isEmpty()) {
            bestNextPath.add(depot);
            return bestNextPath;
        }

        List<List<Node>> possiblePaths = new ArrayList<>();
        for (Node c : customers) {


            if (canAdd(currentPath, depot, c, currentCapacity, currentTime)) {
                List<Node> newPath = new ArrayList<>(currentPath);
                newPath.add(c);
                List<Node> newCustomers = new ArrayList<>(customers);
                newCustomers.remove(c);
                int newCapacity = currentCapacity - c.getDemand();
                double dist = getDistance(currentPath.get(currentPath.size() - 1).getPos(), c.getPos());
                double newDistance = currentDistance + dist;
                double newEndTime = getServiceEndTime(currentTime, dist, c);

                List<Node> newPossiblePath = new ArrayList<>();
                newPossiblePath.add(c);
                newPossiblePath.addAll(constructPath(newPath, depot, newCustomers, newCapacity, newDistance, newEndTime));
                possiblePaths.add(newPossiblePath);
            }
        }

        if (!possiblePaths.isEmpty()) { //most nodes in smallest distance

            Position curPos = currentPath.get(currentPath.size() - 1).getPos();
            Comparator<List<Node>> comparator = Comparator.comparing((List<Node> p) -> p.size()).reversed()
                    .thenComparing((List<Node> p) -> calcTotalDistance(p) + getDistance(curPos, p.get(0).getPos()));

            List<List<Node>> ordered = possiblePaths.stream().sorted(comparator).collect(Collectors.toList());


            if (currentPath.size() <= 2) {
                bestNextPath = ordered.get(0);
            } else
                bestNextPath = ordered.get(0);
        } else {
            bestNextPath.add(depot);
        }

        return bestNextPath;

    }


    private static boolean canAdd(List<Node> path, Node depot, Node n, int capacity, double total_time) {
        if (capacity < n.getDemand()) { //if don't have enough capacity left
            return false;
        }
        double dist = getDistance(path.get(path.size() - 1).getPos(), n.getPos());
        double depot_dist = getDistance(depot.getPos(), n.getPos());

        if (total_time + dist <= n.getTimeWindow().getTo()) { //if will get on time for time window
            double service_end_time = getServiceEndTime(total_time, dist, n);

            if ((service_end_time + depot_dist) > depot.getTimeWindow().getTo()) { // if won't get to depot on time
                return false;
            }

            return true;
        }

        return false;

    }

    private static double getServiceEndTime(double currentTotalTime, double distance, Node n) {
        double service_end_time = currentTotalTime + distance;
        if (service_end_time < n.getTimeWindow().getFrom()) {
            service_end_time = n.getTimeWindow().getFrom();
        }
        service_end_time += n.getServiceTime();

        return service_end_time;
    }

    static double calcTotalDistance(List<Node> path) {
        double dist = 0;
        for (int i = 1; i < path.size(); i++) {
            dist += getDistance(path.get(i - 1).getPos(), path.get(i).getPos());
        }
        return dist;
    }

}
