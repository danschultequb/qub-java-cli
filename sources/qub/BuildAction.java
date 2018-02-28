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

        final boolean debug = QubCLI.parseDebug(console.getCommandLine());

        final ProjectJson projectJson = ProjectJson.parse(console);
        if (projectJson != null)
        {
            final Folder javaOutputsFolder = projectJson.getJavaOutputsFolder();
            if (javaOutputsFolder != null)
            {
                final Iterable<String> classpaths = projectJson.getAllClasspaths(QubCLI.getQubFolder(console));

                boolean shouldCompileSources = true;
                boolean compiledSourcesSuccessfully = true;

                final Folder sourcesFolder = projectJson.getJavaSourcesFolder();
                Folder sourceOutputsFolder = null;
                if (sourcesFolder != null)
                {
                    final Iterable<File> sourceFiles = projectJson.getJavaSourceFiles();
                    if (!sourceFiles.any())
                    {
                        console.writeLine("No source files found to compile.");
                    }
                    else
                    {
                        sourceOutputsFolder = javaOutputsFolder.getFolder(sourcesFolder.getName());

                        shouldCompileSources = shouldCompile(sourcesFolder, sourceFiles, sourceOutputsFolder);
                        if (!shouldCompileSources)
                        {
                            console.writeLine("No source files need to be compiled.");
                        }
                        else
                        {
                            final List<String> sourceClasspaths = ArrayList.fromValues(classpaths);
                            sourceClasspaths.add(sourceOutputsFolder.getPath().toString());

                            final String sourcesJavaVersion = projectJson.getJavaSourcesVersion();
                            compiledSourcesSuccessfully = compile("sources", console, sourceClasspaths, sourcesFolder, sourceFiles, sourceOutputsFolder, sourcesJavaVersion, debug);
                        }

                        if (shouldCompileSources && compiledSourcesSuccessfully)
                        {
                            final String project = projectJson.getProject();
                            if (project == null || project.isEmpty())
                            {
                                console.writeLine("Could not determine the desired jar file's name from the \"project\" property.");
                            }
                            else
                            {
                                final File jarFile = javaOutputsFolder.getFile(project + ".jar");

                                File manifestFile = null;
                                final String mainClass = projectJson.getMainClass();
                                if (mainClass != null)
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
                    final Folder testsFolder = projectJson.getJavaTestsFolder();
                    if (testsFolder != null)
                    {
                        final Iterable<File> testFiles = projectJson.getJavaTestFiles();
                        if (!testFiles.any())
                        {
                            console.writeLine("No test files found to compile.");
                        }
                        else
                        {
                            final Folder testOutputsFolder = javaOutputsFolder.getFolder(testsFolder.getName());
                            final boolean shouldCompileTests = shouldCompileSources || shouldCompile(testsFolder, testFiles, testOutputsFolder);
                            if (!shouldCompileTests)
                            {
                                console.writeLine("No test files need to be compiled.");
                            }
                            else
                            {
                                final String testsJavaVersion = projectJson.getJavaTestsVersion();

                                final List<String> testClasspaths = ArrayList.fromValues(classpaths);
                                testClasspaths.add(testOutputsFolder.getPath().toString());
                                if (sourceOutputsFolder != null)
                                {
                                    testClasspaths.add(sourceOutputsFolder.getPath().toString());
                                }

                                compile("tests", console, testClasspaths, testsFolder, testFiles, testOutputsFolder, testsJavaVersion, debug);
                            }
                        }
                    }
                }
            }
        }

        final Duration totalBuildDuration = totalBuild.stop().toSeconds();
        console.writeLine("Total Duration: " + totalBuildDuration.toString("0.0"));
    }

    private static boolean shouldCompile(Folder sourceFileFolder, Iterable<File> sourceFiles, Folder outputFolder)
    {
        return sourceFiles.contains((File sourceFile) ->
        {
            final Path relativePath = sourceFile.getPath().relativeTo(sourceFileFolder.getPath());
            final File classFile = outputFolder.getFile(relativePath.withoutFileExtension().concatenate(".class"));
            boolean needsCompile;
            if (!classFile.exists())
            {
                needsCompile = true;
            }
            else if (sourceFile.getLastModified().greaterThan(classFile.getLastModified()))
            {
                needsCompile = true;
            }
            else
            {
                needsCompile = false;
            }
            return needsCompile;
        });
    }

    private static boolean compile(String label, Console console, Iterable<String> classpaths, Folder folderToCompile, Iterable<File> filesToCompile, Folder outputFolder, String javaVersion, boolean debug)
    {
        final Stopwatch stopwatch = console.getStopwatch();
        console.write("Compiling " + label + "...");
        if (debug)
        {
            console.writeLine();
        }
        stopwatch.start();

        final ProcessBuilder javac = console.getProcessBuilder("javac");
        javac.redirectOutput(console.getOutputAsByteWriteStream());
        javac.redirectError(console.getErrorAsByteWriteStream());

        addNamedArgument(javac, "-classpath", String.join(";", classpaths));
        addNamedArgument(javac, "-d", outputFolder.getPath().toString());
        javac.addArgument("-g");
        javac.addArgument("-Xlint:unchecked");

        if (javaVersion != null && !javaVersion.isEmpty())
        {
            javac.addArguments("-source", javaVersion);
            javac.addArguments("-target", javaVersion);

            if (javaVersion.equals("1.7") || javaVersion.equals("7"))
            {
                final Folder qubFolder = QubCLI.getQubFolder(console);
                addNamedArgument(javac, "-bootclasspath",
                    qubFolder.getPath().
                        concatenateSegment("oracle").
                        concatenateSegment("jre").
                        concatenateSegment("1.7").
                        concatenateSegment("lib").
                        concatenateSegment("rt.jar")
                        .toString());
            }
            else if (javaVersion.equals("1.8") || javaVersion.equals("8"))
            {
                final Folder qubFolder = QubCLI.getQubFolder(console);
                addNamedArgument(javac, "-bootclasspath",
                    qubFolder.getPath().
                        concatenateSegment("oracle").
                        concatenateSegment("jre").
                        concatenateSegment("1.8").
                        concatenateSegment("lib").
                        concatenateSegment("rt.jar")
                        .toString());
            }
        }

        javac.addArguments(filesToCompile.map(FileSystemEntry::toString));

        if (debug)
        {
            console.writeLine(javac.getCommand());
        }

        final int exitCode = javac.run();

        final Path outputFolderPath = outputFolder.getPath();
        for (final File outputFile : outputFolder.getFilesRecursively())
        {
            if (outputFile.getFileExtension().equals(".class"))
            {
                final Path relativeClassFilePath = outputFile.getPath().relativeTo(outputFolderPath).withoutFileExtension();

                final int dollarSignIndex = relativeClassFilePath.toString().indexOf('$');

                String relativeJavaFilePath = relativeClassFilePath.toString();
                if (dollarSignIndex >= 0)
                {
                    relativeJavaFilePath = relativeJavaFilePath.substring(0, dollarSignIndex);
                }
                relativeJavaFilePath += ".java";

                if (!folderToCompile.fileExists(relativeJavaFilePath))
                {
                    outputFile.delete();
                }
            }
        }

        final Duration compilationDuration = stopwatch.stop().toSeconds();
        console.writeLine(" Done (" + compilationDuration.toString("0.0") + ")");

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
