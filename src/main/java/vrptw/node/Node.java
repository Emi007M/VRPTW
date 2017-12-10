package vrptw.node;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class Node {
    private int id;
    private Position pos;
    private int demand;
    private TimeWindow timeWindow;
    private int serviceTime;

    @Setter
    private boolean visited = false;

    @Override
    public String toString() {
        String v = visited ? "" : "not";
        return "Customer:" + id + " at " + pos + ", with demand:" + demand + ", within:" + timeWindow + " through:" + serviceTime + " (" + v + " visited)";
    }

    public Node(Node otherNode) {
        this(otherNode.id, otherNode.pos, otherNode.demand, otherNode.timeWindow, otherNode.serviceTime,
                otherNode.visited);
    }
}
