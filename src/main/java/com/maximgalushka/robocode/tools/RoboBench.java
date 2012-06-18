package com.maximgalushka.robocode.tools;

import java.io.*;

/**
 * <p></p>
 *
 * @author Maxim Galushka
 * @since 18.06.12
 */
public class RoboBench {

    private static final String ROBOCODE_ROOT = "C:/robocode";

    private static final String TEMPLATE =  "robocode.battleField.width=800\n" +
            "robocode.battleField.height=600\n" +
            "robocode.battle.numRounds=10\n" +
            "robocode.battle.gunCoolingRate=0.1\n" +
            "robocode.battle.rules.inactivityTime=450\n" +
            "robocode.battle.hideEnemyNames=true\n" +
            "robocode.battle.selectedRobots=com.maximgalushka.robocode.MaximBot*,sample.%s\n";


    public static void main(String[] args) {

        File robots = new File(String.format("%s/robots/sample", ROBOCODE_ROOT));
        File[] classes = robots.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".class");
            }
        });

        for (File f : classes){
            String robot = f.getName().substring(0, f.getName().indexOf("."));
            System.out.printf(String.format("Exec robot: [%s]\n", robot));
            try {
                PrintWriter pw = new PrintWriter(
                        new FileWriter(String.format("%s/battles/%s.battle",
                                ROBOCODE_ROOT, robot)));

                pw.printf(TEMPLATE, robot);
                pw.flush();

                execBattle(robot);

                System.out.printf(String.format("Robot: [%s] completed!\n", robot));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static void execBattle(String robot){
        try {

            String execLine = String.format(
                    "C:/Program Files/Java/jdk1.6.0_32/bin/java -Xmx512M -DROBOTPATH=%s\\robots" +
                            " -Dsun.io.useCanonCaches=false -cp " +
                            "%s/libs/robocode.jar;%s/robots/; robocode.Robocode -battle " +
                            "%s/battles/%s.battle -nodisplay -results %s/bench/%s.txt",
                    ROBOCODE_ROOT, ROBOCODE_ROOT, ROBOCODE_ROOT, ROBOCODE_ROOT, robot,
                    ROBOCODE_ROOT, robot);

            System.out.printf("[%s]\n", execLine);

            Process process = Runtime.getRuntime().exec(execLine);

            InputStream stdout = process.getInputStream ();
            InputStream stderr = process.getErrorStream ();

            BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));
            BufferedReader readerErr = new BufferedReader (new InputStreamReader(stderr));

            String line;
            while ((line = reader.readLine ()) != null) {
                System.out.println ("Stdout: " + line);
            }
            while ((line = readerErr.readLine ()) != null) {
                System.out.println ("Stdout: " + line);
            }
            process.waitFor();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
