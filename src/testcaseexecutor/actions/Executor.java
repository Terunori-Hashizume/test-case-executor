package testcaseexecutor.actions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class Executor {
    static final long LIMIT_MILLIS = 5000;

    private String projectDir;

    public Executor(String projectDir) {
        this.projectDir = projectDir;
    }

    public void execute() throws IOException {
//		Set console
        IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
        MessageConsole console = new MessageConsole("", null);
        consoleManager.addConsoles(new MessageConsole[] { console });
        consoleManager.showConsoleView(console);
        MessageConsoleStream out = console.newMessageStream();

        File testCaseFile = new File(projectDir + "/test_cases.txt");
        List<String> testCases = getTestCases(testCaseFile);

        String[] cmd = {"java", "-cp", projectDir + "/bin/", "mypackage.Main"};

        for (int i = 0; i < testCases.size(); i++) {
            String input = testCases.get(i);

            out.println("Test case " + (i + 1) + ":");

            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();

            boolean finished = false;

            try (BufferedWriter bw = new BufferedWriter(new PrintWriter(p.getOutputStream()))) {
                bw.write(input);
                bw.close();
                finished = p.waitFor(LIMIT_MILLIS, TimeUnit.MILLISECONDS);
                if (!finished) {
                    out.println("Time Limit Exceeded.");
                    p.destroyForcibly();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (finished) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        out.println(line);
                    }
                }
                try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        out.println(line);
                    }
                }
            }
            out.println("----------");
        }
    }

    private List<String> getTestCases(File f) {
        List<String> testCases = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            StringBuilder testCase = new StringBuilder();
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) {
                    testCases.add(testCase.toString());
                    testCase = new StringBuilder();
                } else {
                    testCase.append(line).append('\n');
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return testCases;
    }

}
