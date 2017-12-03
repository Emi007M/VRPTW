package vrptw;

import vrptw.node.Node;
import vrptw.node.Position;
import vrptw.node.TimeWindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;


public class Main {

    private static String nodesDir = "R101.100.txt";
    private static String capacityDir = "capacity.txt";

    private static List<Vehicle> fleet;
    private static Graph graph;

    public static void main(String[] args) throws FileNotFoundException {

        fleet = new ArrayList<>();
        graph = new Graph(readNodes(nodesDir));
        Vehicle.totalCapacity = readCapacity(capacityDir);

//        Node depot = graph.getDepot();
//        List<Node> closest = graph.findClosestNodesInGraph(depot, 5);


        double total_distance = 0;
        // Bruteforcing VRPTW
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

        System.out.println("\nSummary: vehicles=" + fleet.size() + ", total distance=" + total_distance);

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
