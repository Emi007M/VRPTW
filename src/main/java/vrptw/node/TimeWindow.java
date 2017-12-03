package vrptw.node;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TimeWindow {
    private int from;
    private int to;

    @Override
    public String toString() {
        return "(" + from + "-" + to + ")";
    }
}
