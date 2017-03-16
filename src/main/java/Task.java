/**
 * Created by vegito2002 on 3/14/17.
 */
public class Task {
    private String name;
    private int duration;
    int[] labor = new int[4];
    int[] zone = new int[13];

    public Task(String name, int duration, int[] labor, int[] zone) {
        this.name = name;
        this.duration = duration;
        this.labor = labor;
        this.zone = zone;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public int[] getLabor() {
        return labor;
    }

    public int[] getZone() {
        return zone;
    }
}
