package vrptw;

import vrptw.node.Node;
import vrptw.node.Position;
import vrptw.node.TimeWindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;


public class OriginalMain {

    public static ArrayList<ArrayList<double[]>> pointsToDraw = new ArrayList<>();

    private static String nodesDir = "R101.100.txt";
    private static String capacityDir = "capacity.txt";

    private static List<Vehicle> fleet;
    private static Graph graph;

    private static int NUMBER_OF_ITERATIONS = 10000;
    private static double REMOVAL_FACTOR = 0.30;

    public static void setNodesDir(String nodes) {
        nodesDir = nodes;
    }

    public void originalMain() {
        try {
            fleet = new ArrayList<>();
            graph = new Graph(readNodes(nodesDir));
            Vehicle.totalCapacity = readCapacity(capacityDir);
            Random random = new Random();
//        Node depot = graph.getDepot();
//        List<Node> closest = graph.findClosestNodesInGraph(depot, 5);


            double total_distance = 0;
     /*   // Bruteforcing VRPTW - init solution
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

            v.setPath(path);

            total_distance += PathRepository.calcTotalDistance(path);
            System.out.println("\n-vrptw.Vehicle " + fleet.size());
            System.out.println(v.readRoute());

        }
        System.out.println("\nSummary brute force: vehicles=" + fleet.size() + ", total distance=" + total_distance);
    */
            // make initial solution - one vehicle per one customer
     /*   for (Node customer : graph.getCustomers()) {
            Vehicle v = new Vehicle();
            fleet.add(v);
            List<Node> path = new ArrayList<>();
            path.add(graph.getDepot());
            path.add(customer);
            path.add(graph.getDepot());
            v.setPath(path);
            customer.setVisited(true);
            total_distance += PathRepository.calcTotalDistance(path);
        }*/

            while (!graph.getUnvisitedCustomers().isEmpty()) {

                Vehicle v = new Vehicle();
                fleet.add(v);

                v.visitAsMuchAsYouCanWhateverHow(graph.getDepot(), graph.getUnvisitedCustomers());
                total_distance += PathRepository.calcTotalDistance(v.getPath());
            }

            System.out.println("\nSummary after initialisation: vehicles=" + fleet.size() +
                    ", total distance=" + total_distance);

            boolean isEnd = false;
            int iterator = 0;
            // init best solution:
            LNSSolution bestSolution = new LNSSolution(fleet, graph, total_distance);

            int numberOfCustomersToRemove = (int) Math.round(REMOVAL_FACTOR * graph.getCustomers().size());

            while (!isEnd && iterator < NUMBER_OF_ITERATIONS) {

                LNSSolution solutionToWorkWith = new LNSSolution(bestSolution);
                List<Node> freeCustomers = new ArrayList<>();
                // select customers to remove
                List<Node> allCustomers = solutionToWorkWith.getGraph().getCustomers();
                //System.out.println("aaaaaaa " + allCustomers.size());
                for (int i = 0; i < numberOfCustomersToRemove; i++) {
                    int indexToRemove = random.nextInt(allCustomers.size());
                    freeCustomers.add(allCustomers.get(indexToRemove));
                    allCustomers.remove(indexToRemove);
                }
                // remove customers from vehicle paths
                for (Vehicle vehicle : solutionToWorkWith.getFleet()) {
                    for (Node customerToBeRemoved : freeCustomers) {
                        if (vehicle.getPath().contains(customerToBeRemoved)) {
                            vehicle.getPath().remove(customerToBeRemoved);
                            vehicle.setCapacity(vehicle.getCapacity() + customerToBeRemoved.getDemand());
                        }
                    }
                }
                // remove unused vehicles
                List<Vehicle> vehiclesToRemove = new ArrayList<>();
                for (Vehicle vehicle : solutionToWorkWith.getFleet()) {
                    // only depots are left in a path
                    if (vehicle.getPath().size() < 3) {
                        // System.out.println("vehicle removed");
                        vehiclesToRemove.add(vehicle);
                    }
                }
                solutionToWorkWith.getFleet().removeAll(vehiclesToRemove);
                //System.out.println(solutionToWorkWith.getFleet().size());

                // try to insert each free customer
                Collections.shuffle(freeCustomers);
                List<Node> notInsertedCustomers = new ArrayList<>();
                for (Node customer : freeCustomers) {
                    customer.setVisited(false);
                    List<Vehicle> possibleVehicleSolutions = new ArrayList<>();
                    Map<Vehicle, Vehicle> vehicleReplacementMap = new HashMap<>();
                    // for each vehicle find solution
                    boolean isSolutionFound = false;
                    for (Vehicle vehicle : solutionToWorkWith.getFleet()) {
                        Vehicle newVehicle = new Vehicle();
                        List<Node> currentPath = new ArrayList<>();
                        currentPath.add(solutionToWorkWith.getGraph().getDepot());
                        List<Node> path = new ArrayList<>();
                        path.add(solutionToWorkWith.getGraph().getDepot());

                        //add customers which new vehicle has to serve
                        List<Node> customersToVisit = new ArrayList<>();
                        customersToVisit.addAll(vehicle.getPath());
                        //remove depots from customer list
                        customersToVisit.removeAll(Collections.singleton(solutionToWorkWith.getGraph().getDepot()));
                        customersToVisit.add(customer);


                        //find optimal path joining customers
                        path.addAll(PathRepository.constructPath(currentPath, solutionToWorkWith.getGraph().getDepot(), customersToVisit,
                                newVehicle.getCapacity(), 0.0, 0.0));

                        newVehicle.setPath(path);

                        // check if all customers can be connected - size equals all customers + 2 times depot
                        if (path.size() == customersToVisit.size() + 2) {
                            possibleVehicleSolutions.add(newVehicle);
                            vehicleReplacementMap.put(newVehicle, vehicle);
                        }
                    }

                    // chose best solution for this customer
                    if (possibleVehicleSolutions.size() == 0) {
                        // no solutions are possible
                        notInsertedCustomers.add(customer);
                        //System.out.println("baad: customer not inserted " + customer.isVisited());
                    } else {
                        // choose vehicle path with smallest distance
                        Collections.sort(possibleVehicleSolutions);
                        // choose best vehicle
                        Vehicle bestVehicle = possibleVehicleSolutions.get(0);
                        Vehicle vehicleToBeReplaced = vehicleReplacementMap.get(bestVehicle);
                        // update a fleet with new Vehicle
                        vehicleToBeReplaced.setPath(bestVehicle.getPath());
                        customer.setVisited(true);
                        //  System.out.println("restored customer LNS");
                    }
                    solutionToWorkWith.getGraph().getCustomers().add(customer);
                }
                //update total distance of all vehicles
                double totalDistance = 0.0;
                for (Vehicle vehicle : solutionToWorkWith.getFleet()) {
                    totalDistance += PathRepository.calcTotalDistance(vehicle.getPath());
                }
                solutionToWorkWith.setTotalDistance(totalDistance);
                //  System.out.println(solutionToWorkWith.getFleet().size() + " " + totalDistance);

                //System.out.println(solutionToWorkWith.getTotalDistance());
                // set not visited customers in a graph
                // System.out.println("not inserted customers" + notInsertedCustomers.size());
                //  System.out.println(solutionToWorkWith.getGraph().getUnvisitedCustomers().size());

                // for each not inserted do brute force again with new vehicle(s)
                while (!solutionToWorkWith.getGraph().getUnvisitedCustomers().isEmpty()) {

                    Vehicle v = new Vehicle();
                    solutionToWorkWith.getFleet().add(v);
                    //  System.out.println("vehicle added");
                    List<Node> currentPath = new ArrayList<>();
                    currentPath.add(solutionToWorkWith.getGraph().getDepot());
                    List<Node> path = new ArrayList<>();
                    //main part of searching is PathRepository.constructPath
                    path.add(solutionToWorkWith.getGraph().getDepot());
                    path.addAll(PathRepository.constructPath(currentPath, solutionToWorkWith.getGraph().getDepot(),
                            solutionToWorkWith.getGraph().getUnvisitedCustomers(), v.getCapacity(), 0.0, 0.0));

                    v.setPath(path);
                    for (Node customer : path) {
                        if (!customer.equals(solutionToWorkWith.getGraph().getDepot())) {
                            customer.setVisited(true);
                            //System.out.println("restored customers BRUTE FORCE");
                        }
                    }

                    double newDistance = solutionToWorkWith.getTotalDistance() + PathRepository.calcTotalDistance(path);
                    solutionToWorkWith.setTotalDistance(newDistance);

                }

                // if solution is better than best solution update
                if (solutionToWorkWith.compareTo(bestSolution) < 0) {
                    // System.out.println("gotta better solution " + bestSolution.getTotalDistance() + "->" +
                    //         solutionToWorkWith.getTotalDistance());
                    bestSolution = solutionToWorkWith;
                } else {
                    // System.out.println("gotta worse solution " + bestSolution.getTotalDistance() + "->" +
                    //      solutionToWorkWith.getTotalDistance());
                }
                iterator++;
            }
            System.out.println("\nSummary after LNS: vehicles=" + bestSolution.getFleet().size() +
                    ", total distance=" + bestSolution.getTotalDistance());

            //BEST SOLUTION PRINT
            System.out.println("\nData of the best solution" + bestSolution.getFleet());
            int counter = 1;
            pointsToDraw.clear();
            for (Vehicle v : bestSolution.getFleet())
            {
                ArrayList<double[]> singleVehiclePoints = new ArrayList<>();
                double[] xes = new double[v.getPath().size()];
                double[] ys = new double[v.getPath().size()];
                System.out.println("\nLocations visited by vehicle_" + counter + ":");
                for (int index = 0; index < v.getPath().size(); index ++) {
                    System.out.println(v.getPath().get(index).getPos());
                    xes[index] = v.getPath().get(index).getPos().getX()*5+500-v.getPath().get(0).getPos().getX()*5;
                    ys[index] = v.getPath().get(index).getPos().getY()*5+300-v.getPath().get(0).getPos().getY()*5;
                }
                singleVehiclePoints.add(xes);
                singleVehiclePoints.add(ys);
                pointsToDraw.add(singleVehiclePoints);
//                System.out.println(pointsToDraw.get(0).get(1)[0]);
//                System.out.println(pointsToDraw.get(0).get(1)[1]);
//                System.out.println(pointsToDraw.get(0).get(1)[2]);
//                System.out.println(pointsToDraw.get(0).get(1)[3]);
//                System.out.println(pointsToDraw.get(0).get(1)[4]);
//                System.out.println("---");
                counter++;
                //System.out.println(v.readRoute());
            }
            System.out.println("----------------------------------------------------------------------------------");
            //BEST SOLUTION PRINT ^^^^

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<ArrayList<double[]>> getPointsToDraw(){
        return pointsToDraw;
    }

    private static int readCapacity(String capacityDir) throws FileNotFoundException {
        int capacity = 0;
        File file = new File(OriginalMain.class.getClassLoader().getResource(capacityDir).getPath());
        Scanner sc = new Scanner(file);
        if (sc.hasNextInt()) {
            capacity = sc.nextInt();
        }
        sc.close();
        return capacity;
    }

    private static List<Node> readNodes(String nodesDir) throws FileNotFoundException {
        List<Node> nodes = new ArrayList<>();
        File file = new File(OriginalMain.class.getClassLoader().getResource(nodesDir).getPath());
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
