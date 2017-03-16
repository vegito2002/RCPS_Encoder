import java.io.IOException;
import java.io.*;

/**
 * Created by vegito2002 on 3/15/17.
 */
public class Bootstrap {
    private static final String OUTPUT_PATH = "problem.ecl";

    public static void main(String[] arg) {
//        if(arg.length < 1) {
//            System.out.println("Please specify number of Problem to solve.");
//            System.exit(1);
//        }
        int problemNumber = 7;

        try {
            System.setOut(new PrintStream((new FileOutputStream(OUTPUT_PATH))));
            RCPSWriter writer = new RCPSWriter();
            if (problemNumber == 1) {
                System.out.println(writer.writeProblem1());
            } else if ( problemNumber == 2){
                System.out.println(writer.writeProblem2());
            } else if ( problemNumber == 3) {
                System.out.println(writer.writeProblem3());
            } else if ( problemNumber == 4) {
                System.out.println(writer.writeProblem4());
            } else if ( problemNumber == 5) {
                System.out.println(writer.writeProblem5());
            } else if ( problemNumber == 6) {
                System.out.println(writer.writeProblem6());
            }
            else if ( problemNumber == 7) {
                System.out.println(writer.writeProblem7());
            }
            else if ( problemNumber == 8) {
                System.out.println(writer.writeProblem8());
            }

            //            else {
//                System.out.printf("%s is not a legal number of problem, terminating...%n", arg[0]);
//                System.exit(1);
//            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }
}
