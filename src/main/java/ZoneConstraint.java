/**
 * Created by vegito2002 on 3/14/17.
 */
public class ZoneConstraint {
    private String name;
    private int limit;

    public ZoneConstraint(String name, int limit) {
        this.name = name;
        this.limit = limit;
    }

    public String getName() {
        return name;
    }

    public int getLimit() {
        return limit;
    }
}
