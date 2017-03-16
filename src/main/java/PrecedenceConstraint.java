/**
 * Created by vegito2002 on 3/14/17.
 */
public class PrecedenceConstraint {
    private String predecessor;
    private String successor;

    public PrecedenceConstraint(String predecessor, String successor) {
        this.predecessor = predecessor;
        this.successor = successor;
    }

    public String getPredecessor() {
        return predecessor;
    }

    public String getSuccessor() {
        return successor;
    }
}
