/**
 * Created by vegito2002 on 3/14/17.
 */

import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class RCPSWriter {
    private RCPSReader dataReader;

    private static String ECL_HEADER = ":- lib(ic).\t\t\t% include the standard interval constraint library\n" +
            ":- lib(branch_and_bound).\t% include the branch and bound library for minimization\n" +
            ":- lib(ic_edge_finder).\t\t% include the cumulative constraint library needed for resource constraints\n" +
            "\n";
    private static String SCHEDULE_HEADER = "schedule(EndTime) :-\n";

    public RCPSWriter() {
        dataReader = new RCPSReader();
    }

    private String writeDomainConstraints(int problemNumber) throws IOException{
        String result = "\nTaskFinishTimes = [ \n";
        int length = dataReader.getTasks().size();
        Task[] tasksArray = new Task[length];
        tasksArray = dataReader.getTasks().toArray(tasksArray);
        for (int i = 0; i<length-1; i++) {
            result += "F_" + tasksArray[i].getName() + ", \n";
        }
        result += "F_" + tasksArray[length-1].getName() + "], \n" +
                "\nTaskStartTimes = [ \n";

        for(int i = 0; i<length-1; i++) {
            result += "S_" + tasksArray[i].getName() + ", \n";
        }
        result += "S_" + tasksArray[length-1].getName() + "], \n";

        int timeBound = 0;
        for(Task task : tasksArray) timeBound += task.getDuration();
        int fakeBound = 39991;
        if (problemNumber != 1) {
            timeBound = fakeBound;
        }

        result += "TaskFinishTimes :: 0.." + timeBound + ",\n" +
                "TaskStartTimes :: 0.." + timeBound +
                ", \n";

        return result;
    }

    private String writeDurationConstraints() throws IOException {
        String result = "\n ";
        for (Task task : dataReader.getTasks()) {
            result += "F_" + task.getName() + " - " + "S_" + task.getName() + "#= " + task.getDuration() + ", \n";
        }
        return result;
    }

    private String writePrecedenceConstraints() throws IOException {
        String result = "\n";
        for (PrecedenceConstraint pre : dataReader.getPrecedenceConstraints()) {
            result += "S_" + pre.getSuccessor() + "#>= F_" + pre.getPredecessor() + ", \n";
        }
        return result;
    }

    private String writeZoneConstraints() throws IOException {
        String result = "\n";
        Map<String, Integer> zoneMap = new HashMap<>();
        for (ZoneConstraint each : dataReader.getZoneConstraints()) {
            zoneMap.put(each.getName(), each.getLimit());
        }
        for (int i = 0; i<13; i++) {
            List<Task> activeTasks = new ArrayList<>();
            for (Task task : dataReader.getTasks()) {
                if (task.getZone()[i] > 0) activeTasks.add(task);
            }
            Task[] activeTasksArray = new Task[activeTasks.size()];
            activeTasksArray = activeTasks.toArray(activeTasksArray);
            int length = activeTasks.size();
            result += "cumulative([ \n";
            int j = 0;
            for (; j<length-1; j++) {
                result += "S_" + activeTasksArray[j].getName() + ", \n";
            }
            result += "S_" + activeTasksArray[j].getName() + "\n], \n[\n";
            j = 0;
            for (; j<length-1; j++) {
                result += activeTasksArray[j].getDuration() + ", \n";
            }
            result += activeTasksArray[j].getDuration() + "\n], \n[\n";
            j = 0;
            for (; j<length-1; j++) {
                result += activeTasksArray[j].getZone()[i] + ", \n";
            }
            result += activeTasksArray[j].getZone()[i] + "\n], ";
            Integer retrievedZone =  zoneMap.get("Zone_Z" + (char)('a'+i));
            if (retrievedZone == null) {
                throw new IOException("None Zone Constraints found");
            } else {
                result += retrievedZone + "), \n";
            }
        }
        return result;
    }

    private String writePrintf() throws IOException {
        String result = "\n";
        for (Task t : dataReader.getTasks()) {
			String[] taskNames = t.getName().split("_");
			String newName = taskNames[0] + "_" + taskNames[1] + "." + taskNames[2] + "_" + taskNames[3];
        	result += "printf(\"" + newName + "  %d %n\", [S_" + t.getName() + "]), \n";
        }
        result += "nl.";
        return result;
    }

    private String writeCriterion() throws IOException {
        String result = "\n";
        for (Task t : dataReader.getTasks()) {
            result += String.format("crit(S_%s, %d). %n", t.getName(), t.getDuration());
            result += String.format("crit(F_%s, %d). %n", t.getName(), t.getDuration());
        }
        return result;
    }

    private String writeAssertion() throws IOException {
        String result = "\n";
        for (Task t : dataReader.getTasks()) {
            result += String.format("assert(crit(S_%s, %d)), %n", t.getName(), t.getDuration());
            result += String.format("assert(crit(F_%s, %d)), %n", t.getName(), t.getDuration());
        }
        return result;
    }

    private String writeLasting() throws IOException {
        String result = "\n ";
        for (Task task : dataReader.getTasks()) {
            result += String.format("lasting(F_%s, S_%s, %d), %n", task.getName(), task.getName(), task.getDuration());
        }
        return result;
    }

    private String writeTerm() throws IOException {
        String result = "\n";
        result += "Terms = [\n";
        Task[] tasksArray = new Task[dataReader.getTasks().size()];
        int length = tasksArray.length;
        tasksArray = dataReader.getTasks().toArray(tasksArray);
        int i = 0;
        for (; i<length-1; i++) {
            result += String.format("together(F_%s, S_%s), %n", tasksArray[i].getName(), tasksArray[i].getName());
        }
        result += String.format("together(F_%s, S_%s) %n", tasksArray[i].getName(), tasksArray[i].getName());
        result += "],\n";
        return result;
    }

    public String writeProblem1() throws IOException {
        return ECL_HEADER
                + SCHEDULE_HEADER
                + writeDomainConstraints(1)
                + writeDurationConstraints()
                + writePrecedenceConstraints()
                + "EndTime #= max(TaskFinishTimes),\n" +
                "flatten([TaskStartTimes,TaskFinishTimes,EndTime], AllVars),\n" +
                "minimize(labeling(AllVars), EndTime),\n"
                + writePrintf();
    }

    public String writeProblem2() throws IOException {
        return ECL_HEADER
                +SCHEDULE_HEADER
                + writeDomainConstraints(2)
                + writeDurationConstraints()
                + writePrecedenceConstraints()
                + writeZoneConstraints()
                + "EndTime #= max(TaskFinishTimes),\n" +
                "flatten([TaskStartTimes,TaskFinishTimes,EndTime], AllVars),\n" +
                "%% In actuality, I would not use bbs(0) here and would just leave the program running. \n" +
                "%% I added bbs(0) here so that the program can quickly produce result for display.\n" +
                "minimize(search(AllVars, 0, largest, indomain_max, bbs(0), []), EndTime),\n"
                + writePrintf();
    }

    public String writeProblem3() throws IOException {
        return ECL_HEADER
                + "longest(X,Crit):-\n" +
                "\tcrit(X,Dur),\n" +
                "\tCrit is (-1 * Dur).\n"
                + "shortest(X,Crit):-\n" +
                "\tcrit(X,Dur),\n" +
                "\tCrit is (1 * Dur).\n"
                + writeCriterion()
                + SCHEDULE_HEADER
                + writeDomainConstraints(3)
                + writeDurationConstraints()
                + writePrecedenceConstraints()
                + writeZoneConstraints()
                + "EndTime #= max(TaskFinishTimes),\n" +
                "flatten([TaskStartTimes,TaskFinishTimes,EndTime], AllVars),\n" +
                "minimize(search(AllVars, 0, longest, indomain_max, complete, []), EndTime),\n"
                + writePrintf();
    }

    public String writeProblem4() throws IOException{
        return ECL_HEADER
                + "longest(X,Crit):-\n" +
                "\tcrit(X,Dur),\n" +
                "\tCrit is (-1 * Dur).\n"
                + "shortest(X,Crit):-\n" +
                "\tcrit(X,Dur),\n" +
                "\tCrit is (1 * Dur).\n"
                + SCHEDULE_HEADER
                + writeDomainConstraints(3)
                + writeDurationConstraints()
                + writePrecedenceConstraints()
                + writeZoneConstraints()
                + writeAssertion()
                + "EndTime #= max(TaskFinishTimes),\n" +
                "flatten([TaskStartTimes,TaskFinishTimes,EndTime], AllVars),\n" +
                "minimize(search(AllVars, 0, longest, indomain_max, complete, []), EndTime),\n"
                + writePrintf();
    }


    public String writeProblem5() throws IOException {
        return ECL_HEADER
                + "longest(X,Crit):-\n" +
                "\tX- _ #= Dur; _ - X #= Dur,\n" +
                "\tCrit is (-1 * Dur).\n" +
                "\n" +
                "shortest(X,Crit):-\n" +
                "\tX- _ #= Dur; _ - X #= Dur,\n" +
                "\tCrit is (1 * Dur).\n"
                + SCHEDULE_HEADER
                + writeDomainConstraints(3)
                + writeDurationConstraints()
                + writePrecedenceConstraints()
                + writeZoneConstraints()
                + "EndTime #= max(TaskFinishTimes),\n" +
                "flatten([TaskStartTimes,TaskFinishTimes,EndTime], AllVars),\n" +
                "minimize(search(AllVars, 0, longest, indomain_max, complete, []), EndTime),\n"
                + writePrintf();
    }

    public String writeProblem6() throws IOException {
        return ECL_HEADER
                + "longest(X,Crit):-\n" +
                "\tX- _ #= Dur; _ - X #= Dur,\n" +
                "\tget_bounds(X, Lo, Hi),\n" +
                "\tCrit is (-1 * (100 * Dur + Hi)).\n" +
                "\n" +
                "shortest(X,Crit):-\n" +
                "\tX- _ #= Dur; _ - X #= Dur,\n" +
                "\tget_bounds(X, Lo, Hi),\n" +
                "\tCrit is (1 * (100 * Dur + Hi)).\n"
                + SCHEDULE_HEADER
                + writeDomainConstraints(3)
                + writeDurationConstraints()
                + writePrecedenceConstraints()
                + writeZoneConstraints()
                + "EndTime #= max(TaskFinishTimes),\n" +
                "flatten([TaskStartTimes,TaskFinishTimes,EndTime], AllVars),\n" +
                "minimize(search(AllVars, 0, longest, indomain_max, complete, []), EndTime),\n"
                + writePrintf();
    }

    public String writeProblem7() throws IOException {
        return ECL_HEADER
                + "longest(X,Crit):-\n" +
                "\ttogether(F,S),\n" +
                "\tget_bounds(X, _, Hi),\n" +
                "\tCrit is (-1 * (F - S + Hi)).\n" +
                "\n" +
                "lasting(X,Y,Dur):- X - Y #= Dur.\n" +
                "together(X,Y):- lasting(X,Y,_).\n" +
                "\n"
                + SCHEDULE_HEADER
                + writeDomainConstraints(3)
                + writeLasting()
                + writePrecedenceConstraints()
                + writeZoneConstraints()
                + writeTerm()
                + "EndTime #= max(TaskFinishTimes),\n" +
                "flatten([Terms,EndTime], AllVars),\n" +
                "minimize(search(AllVars, 0, longest, indomain_max, complete, []), EndTime),\n"
                + writePrintf();
    }

    public String writeProblem8() throws IOException {
//        test problem1 on lasting rule
        return ECL_HEADER
                + "lasting(X,Y,Dur):- X - Y #= Dur.\n"
                + SCHEDULE_HEADER
                + writeDomainConstraints(3)
                + writeLasting()
                + writePrecedenceConstraints()
                + "EndTime #= max(TaskFinishTimes),\n" +
                "flatten([TaskStartTimes,TaskFinishTimes,EndTime], AllVars),\n" +
                "minimize(labeling(AllVars), EndTime),\n"
                + writePrintf();
    }
}
