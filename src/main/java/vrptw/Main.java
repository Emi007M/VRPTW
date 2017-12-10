package vrptw;

import com.sun.org.apache.xpath.internal.operations.Bool;
import vrptw.node.Node;
import vrptw.node.Position;
import vrptw.node.TimeWindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;


public class Main {

    private static String nodesDir = "R101.100.txt";
    private static String capacityDir = "capacity.txt";

    private static List<Vehicle> fleet;
    private static Graph graph;

    private static int NUMBER_OF_ITERATIONS = 1000;
    private static double REMOVAL_FACTOR = 0.15;

    public static void main(String[] args) throws FileNotFoundException {

        fleet = new ArrayList<>();
        graph = new Graph(readNodes(nodesDir));
        Vehicle.totalCapacity = readCapacity(capacityDir);
        Random random = new Random();
//        Node depot = graph.getDepot();
//        List<Node> closest = graph.findClosestNodesInGraph(depot, 5);


        double total_distance = 0;
        // Bruteforcing VRPTW - init solution
        while (!graph.getUnvisitedCustomers().isEmpty()) {

            Vehicle v = new Vehicle();
            fleet.add(v);

            List<Node> currentPath = new ArrayList<>();
            currentPath.add(graph.getDepot());
            List<Node> path = new ArrayList<>();
            //main part of searching is PathRepository.constructPath
            path.add(graph.getDepot());
            path.addAll(PathRepository.constructPath(currentPath, graph.getDepot(), graph.getUnvisitedCustomers(), v.getCapacity(), 0.0, 0.0));
            path.forEach(n -> n.setVisited(true));

            List<List<Node>> p = new ArrayList<>();
            p.add(path);
            v.setPath(p);

            total_distance += PathRepository.calcTotalDistance(path);
            System.out.println("\n-vrptw.Vehicle " + fleet.size());
            System.out.println(v.readRoute());

        }
        System.out.println("\nSummary brute force: vehicles=" + fleet.size() + ", total distance=" + total_distance);

        boolean isEnd = false;
        int iterator = 0;


        // init best solution:
       LNSSolution bestSolution = new LNSSolution(fleet, graph, total_distance);

       int numberOfCustomersToRemove = (int) Math.round(REMOVAL_FACTOR * graph.getCustomers().size());

       while (isEnd && iterator < NUMBER_OF_ITERATIONS) {

            List<Node> freeCustomers = new ArrayList<>();
            // select customers to remove
            List<Node> allCustomers = new ArrayList<>(graph.getCustomers());
            for (int i = 0; i < numberOfCustomersToRemove; i++) {
                int indexToRemove = random.nextInt(allCustomers.size());
                freeCustomers.add(new Node(allCustomers.get(indexToRemove)));
                allCustomers.remove(indexToRemove);
            }
            // remove customers from vehicle paths
            for (Vehicle vehicle : bestSolution.getFleet()) {
                vehicle.getPath().removeAll(freeCustomers);
            }
            // remove unused vehicles
            List<Vehicle> vehiclesToRemove = new ArrayList<>();
            for (Vehicle vehicle : bestSolution.getFleet()) {
                // only depots are left in a path
                if (vehicle.getPath().size() < 3) {
                    vehiclesToRemove.add(vehicle);
                }
            }
            bestSolution.getFleet().removeAll(vehiclesToRemove);


            // try to insert each free customer
            LNSSolution solutionToWorkWith = new LNSSolution(bestSolution);
            List<Node> notInsertedCustomers = new ArrayList<>();
            for (Node customer : freeCustomers) {
                List<Vehicle> newVehiclesForSingleCustomer = new ArrayList<>();
                Map<Vehicle, Vehicle> vehicleReplacementMap = new HashMap<>();
                // for each vehicle find solution
                for (Vehicle vehicle : solutionToWorkWith.getFleet()) {
                    Vehicle newVehicle = new Vehicle();
                    vehicleReplacementMap.put(newVehicle, vehicle);
                    List<Node> currentPath = new ArrayList<>();
                    currentPath.add(solutionToWorkWith.getGraph().getDepot());
                    List<Node> path = new ArrayList<>();
                    path.add(solutionToWorkWith.getGraph().getDepot());

                    //add customers which new vehicle has to serve
                    List<Node> customersToVisit = new ArrayList<>();
                    customersToVisit.addAll(vehicle.getPath().get(0));
                    //remove depots from customer list
                    customersToVisit.removeAll(Collections.singleton(solutionToWorkWith.getGraph().getDepot()));
                    customersToVisit.add(customer);

                    //find optimal path joining customers
                    path.addAll(PathRepository.constructPath(currentPath, solutionToWorkWith.getGraph().getDepot(), customersToVisit,
                            newVehicle.getCapacity(), 0.0, 0.0));

                    path.forEach(n -> n.setVisited(true));

                    List<List<Node>> p = new ArrayList<>();
                    p.add(path);
                    newVehicle.setPath(p);

                    // check if all customers can be connected - size equals all customers + 2 times depot
                    if (path.size() == customersToVisit.size() + 2) {
                        newVehiclesForSingleCustomer.add(newVehicle);
                    }
                }

                // chose best solution for this customer
                if (newVehiclesForSingleCustomer.size() == 0) {
                    // no solutions are possible
                    notInsertedCustomers.add(customer);
                } else {
                    // choose vehicle path with smallest distance
                    Collections.sort(newVehiclesForSingleCustomer);
                    // choose best vehicle
                    Vehicle bestVehicle = newVehiclesForSingleCustomer.get(0);
                    Vehicle vehicleToBeReplaced = vehicleReplacementMap.get(bestVehicle);
                    // update a fleet with new Vehicle
                    vehicleToBeReplaced.setPath(bestVehicle.getPath());
                }
            }
            // set not visited customers in a graph
           for (Node customer : solutionToWorkWith.getGraph().getCustomers()) {
                if (notInsertedCustomers.contains(customer)) {
                    customer.setVisited(false);
                }
           }
            // for each not inserted do brute force again with new vehicle(s)
           while (!solutionToWorkWith.getGraph().getUnvisitedCustomers().isEmpty()) {

               Vehicle v = new Vehicle();
               solutionToWorkWith.getFleet().add(v);

               List<Node> currentPath = new ArrayList<>();
               currentPath.add(solutionToWorkWith.getGraph().getDepot());
               List<Node> path = new ArrayList<>();
               //main part of searching is PathRepository.constructPath
               path.add(solutionToWorkWith.getGraph().getDepot());
               path.addAll(PathRepository.constructPath(currentPath, solutionToWorkWith.getGraph().getDepot(),
                       solutionToWorkWith.getGraph().getUnvisitedCustomers(), v.getCapacity(), 0.0, 0.0));
               path.forEach(n -> n.setVisited(true));

               List<List<Node>> p = new ArrayList<>();
               p.add(path);
               v.setPath(p);

               double newDistance = solutionToWorkWith.getTotalDistance() + PathRepository.calcTotalDistance(path) ;
               solutionToWorkWith.setTotalDistance(newDistance);
           }

            // if solution is better than best solution update
            if (solutionToWorkWith.compareTo(bestSolution) > 0) {
                bestSolution = solutionToWorkWith;
            }
            iterator++;
        }

    }

    private static int readCapacity(String capacityDir) throws FileNotFoundException {
        int capacity = 0;
        File file = new File(Main.class.getClassLoader().getResource(capacityDir).getPath());
        Scanner sc = new Scanner(file);
        if (sc.hasNextInt()) {
            capacity = sc.nextInt();
        }
        sc.close();
        return capacity;
    }

    private static List<Node> readNodes(String nodesDir) throws FileNotFoundException {
        List<Node> nodes = new ArrayList<>();
        File file = new File(Main.class.getClassLoader().getResource(nodesDir).getPath());
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (!line.isEmpty()) {
                List<Integer> n = Arrays.stream(line.split(" ")).map(String::trim).filter(s -> !s.isEmpty()).map(s -> new Integer(new Double(s).intValue())).collect(Collectors.toList());
                nodes.add(new Node(n.get(0), new Position(n.get(1), n.get(2)), n.get(3), new TimeWindow(n.get(4), n.get(5)), n.get(6), false));
            }
        }
        sc.close();

        return nodes;
    }


}
