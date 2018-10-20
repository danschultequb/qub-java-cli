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
        final boolean debug = QubCLI.parseDebug(console.getCommandLine());

        run(console, debug);
    }

    public static boolean run(Console console, boolean debug)
    {
        boolean compilationSucceeded = false;

        final Stopwatch totalBuild = console.getStopwatch();
        totalBuild.start();

        final ProjectJson projectJson = ProjectJson.parse(console);
        if (projectJson != null)
        {
            final Folder javaOutputsFolder = projectJson.getJavaOutputsFolder();
            if (javaOutputsFolder != null)
            {
                compilationSucceeded = true;

                final Iterable<String> classpaths = projectJson.getAllClasspaths(QubCLI.getQubFolder(console));

                boolean shouldCompileSources = true;

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
                        sourceOutputsFolder = javaOutputsFolder.getFolder(sourcesFolder.getName()).getValue();

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
                            compilationSucceeded = compile("sources", console, sourceClasspaths, sourcesFolder, sourceFiles, sourceOutputsFolder, sourcesJavaVersion, debug);
                        }

                        if (shouldCompileSources && compilationSucceeded)
                        {
                            final String project = projectJson.getProject();
                            if (project == null || project.isEmpty())
                            {
                                console.writeLine("Could not determine the desired jar file's name from the \"project\" property.");
                            }
                            else
                            {
                                final File jarFile = javaOutputsFolder.getFile(project + ".jar").getValue();

                                File manifestFile = null;
                                final String mainClass = projectJson.getMainClass();
                                if (mainClass != null)
                                {
                                    manifestFile = sourceOutputsFolder.getFile("META-INF/MANIFEST.MF").getValue();
                                    final String manifestFileContents =
                                        "Manifest-Version: 1.0\n" +
                                        "Main-Class: " + mainClass + "\n";
                                    manifestFile.setContents(CharacterEncoding.UTF_8.encode(manifestFileContents).getValue());
                                }

                                final ProcessBuilder jar = console.getProcessBuilder("jar").getValue();
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
                                    jar.addArgument(manifestFile.relativeTo(sourceOutputsFolder).toString());
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

                if (compilationSucceeded)
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
                            final Folder testOutputsFolder = javaOutputsFolder.getFolder(testsFolder.getName()).getValue();
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

                                compilationSucceeded = compile("tests", console, testClasspaths, testsFolder, testFiles, testOutputsFolder, testsJavaVersion, debug);
                            }
                        }
                    }
                }
            }
        }

        final Duration totalBuildDuration = totalBuild.stop().toSeconds();
        console.writeLine("Build Duration: " + totalBuildDuration.toString("0.0"));

        return compilationSucceeded;
    }

    private static boolean shouldCompile(Folder sourceFileFolder, Iterable<File> sourceFiles, Folder outputFolder)
    {
        return sourceFiles.contains((File sourceFile) ->
        {
            final Path relativePath = sourceFile.relativeTo(sourceFileFolder);
            final File classFile = outputFolder.getFile(relativePath.withoutFileExtension().concatenate(".class")).getValue();
            boolean needsCompile;
            if (!classFile.exists().getValue())
            {
                needsCompile = true;
            }
            else
            {
                needsCompile = sourceFile.getLastModified().getValue().greaterThan(classFile.getLastModified().getValue());
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

        final ProcessBuilder javac = console.getProcessBuilder("javac.exe").getValue();

        final Value<Boolean> wroteNewLineBeforeOutputOrError = new Value<>();

        final ByteWriteStream consoleOutput = console.getOutputAsByteWriteStream();
        javac.redirectOutput(new ByteWriteStreamBase()
        {
            @Override
            public Result<Boolean> write(byte b)
            {
                if (!wroteNewLineBeforeOutputOrError.hasValue())
                {
                    wroteNewLineBeforeOutputOrError.set(true);
                    consoleOutput.asLineWriteStream(console.getLineSeparator()).writeLine();
                }
                return consoleOutput.write(b);
            }

            @Override
            public boolean isDisposed()
            {
                return consoleOutput.isDisposed();
            }

            @Override
            public Result<Boolean> dispose()
            {
                return consoleOutput.dispose();
            }
        });

        final ByteWriteStream consoleError = console.getErrorAsByteWriteStream();
        javac.redirectError(new ByteWriteStreamBase()
        {
            @Override
            public Result<Boolean> write(byte b)
            {
                if (!wroteNewLineBeforeOutputOrError.hasValue())
                {
                    wroteNewLineBeforeOutputOrError.set(true);
                    consoleError.asLineWriteStream(console.getLineSeparator()).writeLine();
                }
                return consoleError.write(b);
            }

            @Override
            public boolean isDisposed()
            {
                return consoleError.isDisposed();
            }

            @Override
            public Result<Boolean> dispose()
            {
                return consoleError.dispose();
            }
        });

        addNamedArgument(javac, "-classpath", String.join(";", classpaths));
        addNamedArgument(javac, "-d", outputFolder.getPath().toString());
        javac.addArgument("-g");
        javac.addArgument("-Xlint:unchecked");
        javac.addArgument("-Xlint:deprecation");

        if (javaVersion != null && !javaVersion.isEmpty())
        {
            javac.addArguments("-source", javaVersion);
            javac.addArguments("-target", javaVersion);

            if (javaVersion.equals("1.8") || javaVersion.equals("8"))
            {
                final Folder javaFolder = QubCLI.getJavaFolder(console);
                addNamedArgument(javac, "-bootclasspath",
                    javaFolder.getPath().
                        concatenateSegment("jre1.8.0_192").
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

        outputFolder.create();
        final int exitCode = javac.run();

        final Iterable<File> outputFolderFiles = outputFolder.getFilesRecursively().getValue();
        if (outputFolderFiles != null)
        {
            for (final File outputFile : outputFolderFiles)
            {
                if (outputFile.getFileExtension().equals(".class"))
                {
                    final Path relativeClassFilePath = outputFile.relativeTo(outputFolder).withoutFileExtension();

                    final int dollarSignIndex = relativeClassFilePath.toString().indexOf('$');

                    String relativeJavaFilePath = relativeClassFilePath.toString();
                    if (dollarSignIndex >= 0)
                    {
                        relativeJavaFilePath = relativeJavaFilePath.substring(0, dollarSignIndex);
                    }
                    relativeJavaFilePath += ".java";

                    if (!folderToCompile.fileExists(relativeJavaFilePath).getValue())
                    {
                        outputFile.delete();
                    }
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
