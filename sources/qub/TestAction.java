package qub;

import java.awt.Desktop;

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
                        final Folder testOutputsFolder = javaOutputsFolder.getFolder(javaTestsFolder.getName()).getValue();

                        final Folder sourcesFolder = projectJson.getJavaSourcesFolder();
                        final Folder sourceOutputsFolder = sourcesFolder == null ? null : javaOutputsFolder.getFolder(sourcesFolder.getName()).getValue();

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

                        final Iterable<File> outputFiles = testOutputsFolder.getFilesRecursively().getValue();
                        final Iterable<File> testClassFiles = outputFiles == null ? null : outputFiles
                            .where(file -> file.getFileExtension().equals(".class") && !file.getName().contains("$"));
                        if (testClassFiles == null || !testClassFiles.any())
                        {
                            console.writeLine("No compiled test classes found.");
                            testsPassed = true;
                        }
                        else
                        {
                            final Iterable<Path> relativeTestSourcePaths = testClassFiles
                                .map(file -> file.relativeTo(testOutputsFolder));

                            if (debug)
                            {
                                console.writeLine("Found (" + relativeTestSourcePaths.getCount() + ") test classes found: " + relativeTestSourcePaths);
                            }

                            final Iterable<String> fullTestClassNames = relativeTestSourcePaths
                                .map(relativeTestSourcePath -> relativeTestSourcePath.withoutFileExtension().toString().replace('/', '.').replace('\\', '.'));

                            final ProcessBuilder java = console.getProcessBuilder("java.exe").getValue();
                            java.redirectOutput(console.getOutputAsByteWriteStream());
                            java.redirectError(console.getErrorAsByteWriteStream());

                            final Folder qubFolder = QubCLI.getQubFolder(console);
                            final Folder jacocoFolder = qubFolder.getFolder("jacoco/jacococli/0.8.1").getValue();

                            final Double javaTestsLineCoverageRequirement = projectJson.getJavaTestsLineCoverageRequirement();
                            File coverageExecFile = null;
                            if (coverage)
                            {
                                final File jacocoAgentJarFile = jacocoFolder.getFile("jacocoagent.jar").getValue();
                                coverageExecFile = javaOutputsFolder.getFile("coverage.exec").getValue();
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

                            final Integer exitCode = java.run();
                            testsPassed = (exitCode != null && exitCode.intValue() == 0);

                            if (coverage && sourceOutputsFolder != null)
                            {
                                console.writeLine();
                                console.write("Coverage Analysis... ");

                                final Stopwatch coverageStopwatch = console.getStopwatch();
                                coverageStopwatch.start();

                                final Iterable<File> classFiles = sourceOutputsFolder.getFilesRecursively().getValue()
                                    .where((File file) -> file.getFileExtension().equals(".class"));
                                final File jacocoCLIJarFile = jacocoFolder.getFile("jacococli.jar").getValue();
                                final Folder coverageFolder = javaOutputsFolder.getFolder("coverage").getValue();

                                final ProcessBuilder jacococli = console.getProcessBuilder("java").getValue();
                                if (debug)
                                {
                                    jacococli.redirectOutput(console.getOutputAsByteWriteStream());
                                    jacococli.redirectError(console.getErrorAsByteWriteStream());
                                }
                                jacococli.addArguments("-jar", jacocoCLIJarFile.getPath().toString());
                                jacococli.addArgument("report");
                                jacococli.addArgument(coverageExecFile.getPath().toString());

                                final Path currentFolderPath = console.getCurrentFolderPath();
                                for (final File classFile : classFiles)
                                {
                                    jacococli.addArguments("--classfiles", classFile.getPath().relativeTo(currentFolderPath).toString());
                                }
                                jacococli.addArguments("--sourcefiles", sourcesFolder.getPath().toString());
                                jacococli.addArguments("--html", coverageFolder.getPath().toString());

                                File coverageCSVFile = null;
                                if (javaTestsLineCoverageRequirement != null)
                                {
                                    coverageFolder.create();
                                    coverageCSVFile = coverageFolder.getFile("coverage.csv").getValue();
                                    jacococli.addArguments("--csv", coverageCSVFile.toString());
                                }
                                if (debug)
                                {
                                    console.writeLine();
                                    console.writeLine("Command: \"" + jacococli.getCommand() + "\"");
                                    jacococli.redirectOutput(console.getOutputAsByteWriteStream());
                                    jacococli.redirectError(console.getErrorAsByteWriteStream());
                                }
                                jacococli.run();

                                final Duration coverageDuration = coverageStopwatch.stop().toSeconds();
                                console.writeLine("Done (" + coverageDuration.toString("0.0") + ")");

                                if (javaTestsLineCoverageRequirement != null)
                                {
                                    CSVDocument coverageCSVDocument = null;
                                    try (final CharacterReadStream coverageCSVFileReadStream = coverageCSVFile.getContentByteReadStream().getValue().asCharacterReadStream())
                                    {
                                        coverageCSVDocument = CSV.parse(coverageCSVFileReadStream);
                                    }
                                    catch (Exception e)
                                    {
                                        console.writeLine(e.toString());
                                    }

                                    final Function1<CSVRow, String> getFullClassName = (CSVRow coverageEntry) -> coverageEntry.get(1) + "." + coverageEntry.get(2);
                                    final Function1<CSVRow, Integer> getLinesMissed = (CSVRow coverageEntry) -> Integer.parseInt(coverageEntry.get(7));
                                    final Function1<CSVRow, Integer> getLinesCovered = (CSVRow coverageEntry) -> Integer.parseInt(coverageEntry.get(8));
                                    final Function1<CSVRow, Integer> getTotalLines = (CSVRow coverageEntry) -> getLinesMissed.run(coverageEntry) + getLinesCovered.run(coverageEntry);

                                    final Iterable<CSVRow> coverageEntries = coverageCSVDocument.skipFirst();
                                    int maxFullClassNameLength = 0;
                                    for (final CSVRow coverageEntry : coverageEntries)
                                    {
                                        final String fullClassName = getFullClassName.run(coverageEntry);
                                        maxFullClassNameLength = Math.maximum(fullClassName.length(), maxFullClassNameLength);
                                    }

                                    int maxTotalLinesLength = 0;
                                    for (final CSVRow coverageEntry : coverageEntries)
                                    {
                                        final int totalLines = getTotalLines.run(coverageEntry);
                                        maxTotalLinesLength = Math.maximum(maxTotalLinesLength, Integer.toString(totalLines).length());
                                    }

                                    final String formatString = "  %-" + maxFullClassNameLength + "s %" + maxTotalLinesLength + "d / %" + maxTotalLinesLength + "d (%3d%%)";
                                    for (final CSVRow coverageEntry : coverageEntries)
                                    {
                                        final int linesCovered = getLinesCovered.run(coverageEntry);
                                        final int totalLines = getTotalLines.run(coverageEntry);
                                        final int lineCoverage = (int)(100 * (linesCovered / (double)totalLines));
                                        if (lineCoverage < javaTestsLineCoverageRequirement.intValue())
                                        {
                                            testsPassed = false;

                                            final String fullClassName = getFullClassName.run(coverageEntry);
                                            console.writeLine(formatString, fullClassName, linesCovered, totalLines, lineCoverage);
                                        }
                                    }

                                    console.writeLine("Coverage Requirement (%d%%): %s", javaTestsLineCoverageRequirement.intValue(), (testsPassed ? "Passed" : "Failed"));
                                }

                                try
                                {
                                    Desktop.getDesktop().open(new java.io.File(coverageFolder.getFile("index.html").getValue().getPath().toString()));
                                }
                                catch (java.io.IOException e)
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
