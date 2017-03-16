/**
 * Created by vegito2002 on 3/14/17.
 */
public class LaborConstraint {
    private String name;
    private int limit1;
    private int limit2;

    public String getName() {
        return name;
    }

    public int getLimit1() {
        return limit1;
    }

    public int getLimit2() {
        return limit2;
    }

    public LaborConstraint(String name, int limit1, int limit2) {

        this.name = name;
        this.limit1 = limit1;
        this.limit2 = limit2;
    }
}
