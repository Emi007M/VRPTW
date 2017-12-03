package vrptw.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
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
}
