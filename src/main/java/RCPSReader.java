/**
 * Created by vegito2002 on 3/14/17.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class simply to read data from a CSV file and return it as a List to be further processed
 */
public class RCPSReader {
    private static String FILE_NAME = "rcps.data";
    private List<Task> tasks = new ArrayList<>();
    private List<ZoneConstraint> zoneConstraints = new ArrayList<>();
    private List<LaborConstraint> laborConstraints = new ArrayList<>();
    private List<PrecedenceConstraint> precedenceConstraints = new ArrayList<>();

    public RCPSReader() {
        primaryReader();
    }

    public void primaryReader() {

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            /*
            这里有一个非常奇怪的现象,因为 data 里面,zone 是在 labor 前面的,所以如果下面 read 的时候,zoneReader在laborReader的后面,zone 直接就什么也没有.但是调换
            成为和 data 里面一样的顺序,就没有这个问题了. 说明这里用的同一个buffered reader,其实是只读了一遍,加上我每个 reader 里面还用了 break, 下一个 reader 直接
            就接着上一个 reader break 出来的位置继续 read 了;
             */
            taskReader(br);
            precedenceReader(br);
            zoneReader(br);
            laborReader(br);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void taskReader(BufferedReader br) throws IOException {
        String line = "";
        boolean start = false;
        while ((line = br.readLine()) != null) {
            if (line.contains("section1")) {
                start=true;
                continue;
            }
            if ((line.length() == 0) && (start==true)) break;
            if (start == true) {
                String[] entries = line.split(" ");
                String[] times = entries[1].split(":");
                int taskDuration = (Integer.parseInt(times[0]) * 60 + (Integer.parseInt(times[1])));
                String[] names = entries[0].split("\\.");
                String taskName = names[0] + "_" + names[1];
                int[] labors = new int[4];
                int[] zones = new int[13];
                for (int i = 2; i < 6; i++) {
                    labors[i - 2] = Integer.parseInt(entries[i]);
                }
                for (int i = 6; i < 19; i++) {
                    zones[i - 6] = Integer.parseInt(entries[i]);
                }
                tasks.add(new Task(taskName, taskDuration, labors, zones));
            }
        }
    }

    private void precedenceReader(BufferedReader br) throws IOException {
        String line = "";
        boolean start = false;
        while ((line = br.readLine()) != null) {
            if (line.contains("section2")) {
                start=true;
                continue;
            }
            if ((line.length() == 0) && (start==true)) break;
            if (start == true) {
                String[] entries = line.split(" ");
                String[] firstEntries = entries[0].split("\\.");
                String[] secondEntries = entries[1].split("\\.");
                String predecessorName = firstEntries[0] + "_" + firstEntries[1];
                String successorName = secondEntries[0] + "_" + secondEntries[1];
                precedenceConstraints.add(new PrecedenceConstraint(predecessorName, successorName));
            }
        }
    }

    private void laborReader(BufferedReader br) throws IOException {
        String line = "";
        boolean start = false;
        while ((line = br.readLine()) != null) {
            if (line.contains("Initial Labor Availability by Shift")) {
                start=true;
                continue;
            }
            if ((line.length() == 0) && (start==true)) break;
            if (start == true) {
                String[] entries = line.split(" ");
                String[] names = entries[0].split("\\.");
                laborConstraints.add(new LaborConstraint((names[0] + "_" + names[1]), Integer.parseInt(entries[1]), Integer.parseInt(entries[2])));
            }
        }
    }

    private void zoneReader(BufferedReader br) throws IOException {
        String line = "";
        boolean start = false;
        while ((line = br.readLine()) != null) {
            if (line.contains("Assembly Zone Maximum Occupancy")) {
                start=true;
                continue;
            }
            if ((line.length() == 0) && (start==true)) break;
            if (start == true) {
                String[] entries = line.split(" ");
                String[] names = entries[0].split("\\.");
                zoneConstraints.add(new ZoneConstraint((names[0] + "_" + names[1]), Integer.parseInt(entries[1])));
            }
        }
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public List<ZoneConstraint> getZoneConstraints() {
        return zoneConstraints;
    }

    public List<LaborConstraint> getLaborConstraints() {
        return laborConstraints;
    }

    public List<PrecedenceConstraint> getPrecedenceConstraints() {
        return precedenceConstraints;
    }
}
