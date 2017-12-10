package vrptw;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anean on 10.12.2017.
 */
@Getter
@Setter
@AllArgsConstructor
public class LNSSolution implements Comparable<LNSSolution>{
    private List<Vehicle> fleet;
    private Graph graph;
    private double totalDistance;

    public LNSSolution(LNSSolution otherSolution) {
        this(new ArrayList<Vehicle>(otherSolution.fleet), new Graph(otherSolution.graph), otherSolution.totalDistance);
    }

    // TODO: modify compare method to include number of vehicles used and not only distance
    @Override
    public int compareTo(LNSSolution o) {
        return Double.compare(totalDistance, o.totalDistance);
    }



}
