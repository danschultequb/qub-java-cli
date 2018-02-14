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

                            compiledSourcesSuccessfully = compile(console, sourceClasspaths, sourceFiles, sourceOutputsFolder, debug);
                            if (compiledSourcesSuccessfully)
                            {
                                String jarFileName = null;
                                final JSONSegment jarFileNameSegment = java.getPropertyValue("jarFileName");
                                if (jarFileNameSegment instanceof JSONQuotedString)
                                {
                                    jarFileName = ((JSONQuotedString)jarFileNameSegment).toUnquotedString();
                                }

                                if (jarFileName == null || jarFileName.isEmpty())
                                {
                                    final JSONSegment projectSegment = projectJsonRoot.getPropertyValue("project");
                                    if (projectSegment instanceof JSONQuotedString)
                                    {
                                        jarFileName = ((JSONQuotedString)projectSegment).toUnquotedString();
                                    }
                                }

                                if (jarFileName == null || jarFileName.isEmpty())
                                {
                                    console.writeLine("Could not determine the desired jar file's name from the \"jarFileName\" property or the \"project\" property.");
                                }
                                else
                                {
                                    if (!jarFileName.endsWith(".jar"))
                                    {
                                        jarFileName += ".jar";
                                    }
                                    final File jarFile = outputsFolder.getFile(jarFileName);

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

                                    if (debug)
                                    {
                                        console.writeLine(jar.getCommand());
                                    }

                                    jar.run();
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

                                compile(console, testClasspaths, testFiles, testOutputsFolder, debug);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean compile(Console console, Iterable<String> classpaths, Iterable<File> filesToCompile, Folder outputFolder, boolean debug)
    {
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
            console.writeLine(javac.getCommand());
        }

        final int exitCode = javac.run();

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
