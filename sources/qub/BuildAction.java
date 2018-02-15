package qub;

/**
 * A QubCLI action that builds code projects.
 */
public class BuildAction implements Action
{
    @Override
    public String getName()
    {
        return "Build";
    }

    @Override
    public String getDescription()
    {
        return "Build the coding project in the current folder.";
    }

    @Override
    public String getArgumentUsage()
    {
        return null;
    }

    @Override
    public void run(Console console)
    {
        final Stopwatch totalBuild = console.getStopwatch();
        totalBuild.start();

        final boolean debug = (console.getCommandLine().get("debug") != null);

        final JSONObject projectJsonRoot = QubCLI.readProjectJson(console);
        if (projectJsonRoot != null)
        {
            final JSONObject java = QubCLI.getJavaSegment(console, projectJsonRoot);
            if (java != null)
            {
                final Folder outputsFolder = QubCLI.getOutputsFolder(console, java);
                if (outputsFolder != null)
                {
                    final Iterable<String> classpaths = QubCLI.getClasspaths(console, java);

                    boolean compiledSourcesSuccessfully = true;

                    final Folder sourcesFolder = QubCLI.getSourcesFolder(console, java);
                    Folder sourceOutputsFolder = null;
                    if (sourcesFolder != null)
                    {
                        final Iterable<File> sourceFiles = QubCLI.getSourceFiles(console, sourcesFolder);
                        if (sourceFiles != null && sourceFiles.any())
                        {
                            sourceOutputsFolder = outputsFolder.getFolder(sourcesFolder.getName());

                            final List<String> sourceClasspaths = ArrayList.fromValues(classpaths);
                            sourceClasspaths.add(sourceOutputsFolder.getPath().toString());

                            compiledSourcesSuccessfully = compile("sources", console, sourceClasspaths, sourceFiles, sourceOutputsFolder, debug);
                            if (compiledSourcesSuccessfully)
                            {
                                final String project = QubCLI.getProject(console, projectJsonRoot);
                                if (project == null || project.isEmpty())
                                {
                                    console.writeLine("Could not determine the desired jar file's name from the \"project\" property.");
                                }
                                else
                                {
                                    final File jarFile = outputsFolder.getFile(project + ".jar");

                                    File manifestFile = null;
                                    final String mainClass = QubCLI.getMainClass(console, java);
                                    if (mainClass != null && !mainClass.isEmpty())
                                    {
                                        manifestFile = sourceOutputsFolder.getFolder("META-INF").getFile("MANIFEST.MF");
                                        final String manifestFileContents =
                                            "Manifest-Version: 1.0\n" +
                                            "Main-Class: " + mainClass + "\n";
                                        manifestFile.setContents(manifestFileContents, CharacterEncoding.UTF_8);
                                    }

                                    final ProcessBuilder jar = console.getProcessBuilder("jar");
                                    jar.setWorkingFolder(sourceOutputsFolder);
                                    jar.redirectOutput(console.getOutputAsByteWriteStream());
                                    jar.redirectError(console.getErrorAsByteWriteStream());

                                    String jarArguments = "cf";
                                    if (manifestFile != null)
                                    {
                                        jarArguments += 'm';
                                    }
                                    jar.addArgument(jarArguments);

                                    jar.addArgument(jarFile.getPath().toString());

                                    if (manifestFile != null)
                                    {
                                        jar.addArgument(manifestFile.getPath().relativeTo(sourceOutputsFolder.getPath()).toString());
                                    }

                                    jar.addArgument(".");

                                    final Stopwatch stopwatch = console.getStopwatch();
                                    console.write("Creating sources jar file...");
                                    if (debug)
                                    {
                                        console.writeLine();
                                        console.writeLine(jar.getCommand());
                                    }

                                    stopwatch.start();
                                    jar.run();
                                    final Duration jarFileCreationDuration = stopwatch.stop().toSeconds();

                                    console.writeLine(" Done (" + jarFileCreationDuration.toString("#.#") + ")");
                                }
                            }
                        }
                    }

                    if (compiledSourcesSuccessfully)
                    {
                        final Folder testsFolder = QubCLI.getTestsFolder(console, java);
                        if (testsFolder != null)
                        {
                            final Iterable<File> testFiles = QubCLI.getTestFiles(console, testsFolder);
                            if (testFiles != null && testFiles.any())
                            {
                                final Folder testOutputsFolder = outputsFolder.getFolder(testsFolder.getName());

                                final List<String> testClasspaths = ArrayList.fromValues(classpaths);
                                testClasspaths.add(testOutputsFolder.getPath().toString());
                                if (sourceOutputsFolder != null)
                                {
                                    testClasspaths.add(sourceOutputsFolder.getPath().toString());
                                }

                                compile("tests", console, testClasspaths, testFiles, testOutputsFolder, debug);
                            }
                        }
                    }
                }
            }
        }

        final Duration totalBuildDuration = totalBuild.stop().toSeconds();
        console.writeLine("Total Duration: " + totalBuildDuration.toString("0.0"));
    }

    private static boolean compile(String label, Console console, Iterable<String> classpaths, Iterable<File> filesToCompile, Folder outputFolder, boolean debug)
    {
        final Stopwatch stopwatch = console.getStopwatch();
        console.write("Compiling " + label + "...");
        stopwatch.start();

        final ProcessBuilder javac = console.getProcessBuilder("javac");
        javac.redirectOutput(console.getOutputAsByteWriteStream());
        javac.redirectError(console.getErrorAsByteWriteStream());

        addNamedArgument(javac, "-classpath", String.join(";", classpaths));
        addNamedArgument(javac, "-d", outputFolder.getPath().toString());
        javac.addArgument("-g");
        javac.addArgument("-Xlint:unchecked");

        javac.addArguments(filesToCompile.map(file -> file.getPath().toString()));

        if (debug)
        {
            console.writeLine();
            console.writeLine(javac.getCommand());
        }

        final int exitCode = javac.run();

        final Duration compilationDuration = stopwatch.stop().toSeconds();
        console.writeLine(" Done (" + compilationDuration.toString("#.#") + ")");

        return exitCode == 0;
    }

    private static void addNamedArgument(ProcessBuilder builder, String argumentName, String argumentValue)
    {
        if (argumentValue != null && !argumentValue.isEmpty())
        {
            builder.addArguments(argumentName, argumentValue);
        }
    }
}
