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
        final CommandLine commandLine = console.getCommandLine();
        final boolean debug = (commandLine.get("debug") != null);
        final boolean coverage = (commandLine.get("coverage") != null);

        final JSONObject projectJsonRoot = QubCLI.readProjectJson(console);
        if (projectJsonRoot != null)
        {
            final JSONObject javaSegment = QubCLI.getJavaSegment(console, projectJsonRoot);
            if (javaSegment != null)
            {
                final Folder outputsFolder = QubCLI.getOutputsFolder(console, javaSegment);
                if (outputsFolder != null)
                {
                    final Folder testsFolder = QubCLI.getTestsFolder(console, javaSegment);
                    if (testsFolder != null)
                    {
                        final Folder testOutputsFolder = outputsFolder.getFolder(testsFolder.getName());

                        final Folder sourcesFolder = QubCLI.getSourcesFolder(console, javaSegment);
                        final Folder sourceOutputsFolder = sourcesFolder == null ? null : outputsFolder.getFolder(sourcesFolder.getName());

                        final List<String> classpaths = QubCLI.getClasspaths(console, javaSegment);
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
                            .where((File file) -> file.getFileExtension().equals(".class") && !file.getName().contains("$"));
                        if (!testClassFiles.any())
                        {
                            console.writeLine("No compiled test classes found.");
                        }
                        else
                        {
                            final Iterable<Path> relativeTestSourcePaths = testClassFiles
                                .map(file -> file.getPath().relativeTo(testOutputsFolder.getPath()));

                            final Iterable<String> fullTestClassNames = relativeTestSourcePaths
                                .map((Path relativeTestSourcePath) ->
                                    relativeTestSourcePath.withoutFileExtension().toString().replace('/', '.').replace('\\', '.'));

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
                                coverageExecFile = outputsFolder.getFile("coverage.exec");
                                java.addArgument("-javaagent:" + jacocoAgentJarFile.getPath().toString() + "=destfile=" + coverageExecFile.getPath().toString());
                            }

                            addNamedArgument(java, "-classpath", classpath);

                            java.addArgument("qub.ConsoleTestRunner");

                            java.addArguments(fullTestClassNames);

                            if (debug)
                            {
                                java.addArgument("-debug");

                                console.writeLine("Command: \"" + java.getCommand() + "\"");
                            }

                            java.run();

                            if (coverage && sourceOutputsFolder != null)
                            {
                                final File jacocoCLIJarFile = jacocoFolder.getFile("jacococli.jar");
                                final Folder coverageFolder = outputsFolder.getFolder("coverage");

                                final ProcessBuilder jacococli = console.getProcessBuilder("java");
                                jacococli.redirectOutput(console.getOutputAsByteWriteStream());
                                jacococli.redirectError(console.getErrorAsByteWriteStream());
                                jacococli.addArguments("-jar", jacocoCLIJarFile.getPath().toString());
                                jacococli.addArgument("report");
                                jacococli.addArgument(coverageExecFile.getPath().toString());
                                jacococli.addArguments("--classfiles", sourceOutputsFolder.getPath().toString());
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
    }

    private static void addNamedArgument(ProcessBuilder builder, String argumentName, String argumentValue)
    {
        if (argumentValue != null && !argumentValue.isEmpty())
        {
            builder.addArguments(argumentName, argumentValue);
        }
    }
}
