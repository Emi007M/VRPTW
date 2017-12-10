package vrptw;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import vrptw.node.Node;
import vrptw.node.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Graph {

    private List<Node> customers;
    private Node depot;


    public Graph(List<Node> nodes) {
        this.depot = nodes.get(0);
        this.customers = new ArrayList<>(nodes);
        this.customers.remove(0);

        this.depot.setVisited(true);
    }

    public Graph(Graph graph) {
        this(new ArrayList<Node>(graph.customers), new Node(graph.depot));
    }


    public List<Node> getUnvisitedCustomers() {
        return customers.stream().filter(c -> !c.isVisited()).collect(Collectors.toList());
    }

    public void shuffleCustomers() {
        Collections.shuffle(customers);
    }


    /**
     * finds closest customers to the given within all nodes from graph, which are not visited yet
     */
    public List<Node> findClosestCustomersInGraph(Node target, int amount) {
        return findClosestNodes(target, amount, this.customers);
    }

    /**
     * within nodes from given
     */
    public List<Node> findClosestNodes(Node target, int amount, List<Node> nodes) {
        Position t_pos = target.getPos();
        Comparator<Node> nodeComparator = (n1, n2) -> Double.compare(Position.getDistance(t_pos, n2.getPos()), Position.getDistance(t_pos, n1.getPos()));
        return findNodes(target, amount, nodeComparator, nodes);
    }

    /**
     * finds farthest customers to the given within all nodes from graph, which are not visited yet
     */
    public List<Node> findFarthestCustomersInGraph(Node target, int amount) {
        return findFarthestNodes(target, amount, this.customers);
    }

    /**
     * within nodes from given
     */
    public List<Node> findFarthestNodes(Node target, int amount, List<Node> nodes) {
        Position t_pos = target.getPos();
        Comparator<Node> nodeComparator = Comparator.comparingDouble(n2 -> Position.getDistance(t_pos, n2.getPos()));
        return findNodes(target, amount, nodeComparator, nodes);
    }

    private List<Node> findNodes(Node target, int amount, Comparator<Node> nodeComparator, List<Node> nodes) {
        return nodes.stream().filter(n -> !n.isVisited() && !n.equals(target))
                .sorted(nodeComparator)
                .limit(amount)
                .collect(Collectors.toList());
    }


}
