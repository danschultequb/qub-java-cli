package qub;

import java.awt.Desktop;
import java.io.IOException;

public class TestAction implements Action
{
    @Override
    public String getName()
    {
        return "Test";
    }

    @Override
    public String getDescription()
    {
        return "Run the tests for the coding project in the current directory.";
    }

    @Override
    public String getArgumentUsage()
    {
        return null;
    }

    @Override
    public void run(Console console)
    {
        final boolean debug = QubCLI.parseDebug(console);
        final String pattern = TestAction.parsePattern(console);
        final boolean coverage = TestAction.parseCoverage(console);

        TestAction.run(console, debug, pattern, coverage);
    }

    public static boolean run(Console console, boolean debug, String pattern, boolean coverage)
    {
        boolean testsPassed = false;

        if (BuildAction.run(console, debug))
        {
            console.writeLine();

            final ProjectJson projectJson = ProjectJson.parse(console);
            if (projectJson != null)
            {
                final Folder javaOutputsFolder = projectJson.getJavaOutputsFolder();
                if (javaOutputsFolder != null)
                {
                    final Folder javaTestsFolder = projectJson.getJavaTestsFolder();
                    if (javaTestsFolder != null)
                    {
                        final Folder testOutputsFolder = javaOutputsFolder.getFolder(javaTestsFolder.getName());

                        final Folder sourcesFolder = projectJson.getJavaSourcesFolder();
                        final Folder sourceOutputsFolder = sourcesFolder == null ? null : javaOutputsFolder.getFolder(sourcesFolder.getName());

                        final List<String> classpaths = ArrayList.fromValues(projectJson.getAllClasspaths(QubCLI.getQubFolder(console)));
                        if (sourceOutputsFolder != null)
                        {
                            classpaths.add(sourceOutputsFolder.getPath().toString());
                        }
                        classpaths.add(testOutputsFolder.getPath().toString());
                        final String classpath = String.join(";", classpaths);

                        if (debug)
                        {
                            console.writeLine("Classpath: \"" + classpath + "\"");
                        }

                        final Iterable<File> testClassFiles = testOutputsFolder.getFilesRecursively()
                            .where(file -> file.getFileExtension().equals(".class") && !file.getName().contains("$"));
                        if (!testClassFiles.any())
                        {
                            console.writeLine("No compiled test classes found.");
                        }
                        else
                        {
                            final Iterable<Path> relativeTestSourcePaths = testClassFiles
                                .map(file -> file.relativeTo(testOutputsFolder));

                            final Iterable<String> fullTestClassNames = relativeTestSourcePaths
                                .map(relativeTestSourcePath -> relativeTestSourcePath.withoutFileExtension().toString().replace('/', '.').replace('\\', '.'));

                            final ProcessBuilder java = console.getProcessBuilder("java");
                            java.redirectOutput(console.getOutputAsByteWriteStream());
                            java.redirectError(console.getErrorAsByteWriteStream());

                            final Folder qubFolder = QubCLI.getQubFolder(console);
                            final Folder jacocoFolder = qubFolder
                                .getFolder("jacoco")
                                .getFolder("jacococli")
                                .getFolder("0.8.0");

                            File coverageExecFile = null;
                            if (coverage)
                            {
                                final File jacocoAgentJarFile = jacocoFolder.getFile("jacocoagent.jar");
                                coverageExecFile = javaOutputsFolder.getFile("coverage.exec");
                                java.addArgument("-javaagent:" + jacocoAgentJarFile.getPath().toString() + "=destfile=" + coverageExecFile.getPath().toString());
                            }

                            addNamedArgument(java, "-classpath", classpath);

                            java.addArgument("qub.ConsoleTestRunner");

                            java.addArguments(fullTestClassNames);

                            if (pattern != null && !pattern.isEmpty())
                            {
                                java.addArgument("-pattern=" + pattern);
                            }

                            if (debug)
                            {
                                java.addArgument("-debug");

                                console.writeLine("Command: \"" + java.getCommand() + "\"");
                            }

                            testsPassed = (java.run() == 0);

                            if (coverage && sourceOutputsFolder != null)
                            {
                                final Iterable<File> classFiles = sourceOutputsFolder.getFilesRecursively()
                                    .where((File file) -> file.getFileExtension().equals(".class") && !file.getName().contains("$"));
                                final File jacocoCLIJarFile = jacocoFolder.getFile("jacococli.jar");
                                final Folder coverageFolder = javaOutputsFolder.getFolder("coverage");

                                final ProcessBuilder jacococli = console.getProcessBuilder("java");
                                jacococli.redirectOutput(console.getOutputAsByteWriteStream());
                                jacococli.redirectError(console.getErrorAsByteWriteStream());
                                jacococli.addArguments("-jar", jacocoCLIJarFile.getPath().toString());
                                jacococli.addArgument("report");
                                jacococli.addArgument(coverageExecFile.getPath().toString());
                                for (final File classFile : classFiles)
                                {
                                    jacococli.addArguments("--classfiles", classFile.toString());
                                }
                                jacococli.addArguments("--sourcefiles", sourcesFolder.getPath().toString());
                                jacococli.addArguments("--html", coverageFolder.getPath().toString());
                                if (debug)
                                {
                                    console.writeLine("Command: \"" + jacococli.getCommand() + "\"");
                                }
                                jacococli.run();

                                try
                                {
                                    Desktop.getDesktop().open(new java.io.File(coverageFolder.getFile("index.html").getPath().toString()));
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }

        return testsPassed;
    }

    static String parsePattern(Console console)
    {
        final CommandLineArgument testPatternArgument = console.getCommandLine().remove("pattern");
        return testPatternArgument == null ? null : testPatternArgument.getValue();
    }

    static boolean parseCoverage(Console console)
    {
        final CommandLineArgument coverageArgument = console.getCommandLine().remove("coverage");
        return coverageArgument != null && (coverageArgument.getValue() == null || coverageArgument.getValue().equalsIgnoreCase("true"));
    }

    private static void addNamedArgument(ProcessBuilder builder, String argumentName, String argumentValue)
    {
        if (argumentValue != null && !argumentValue.isEmpty())
        {
            builder.addArguments(argumentName, argumentValue);
        }
    }
}
