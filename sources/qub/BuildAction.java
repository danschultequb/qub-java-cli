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

        final Folder currentFolder = console.getCurrentFolder();

        final File projectJsonFile = currentFolder.getFile("project.json");
        if (!projectJsonFile.exists())
        {
            console.writeLine("project.json file doesn't exist in the current folder.");
        }
        else
        {
            JSONDocument projectJsonDocument;
            try (final CharacterReadStream projectJsonFileReadStream = projectJsonFile.getContentCharacterReadStream())
            {
                projectJsonDocument = JSON.parse(projectJsonFileReadStream);
            }

            final JSONSegment rootSegment = projectJsonDocument.getRoot();
            if (!(rootSegment instanceof JSONObject))
            {
                console.writeLine("project.json root segment must be a JSON object.");
            }
            else
            {
                final JSONObject root = (JSONObject)rootSegment;
                final JSONSegment javaSegment = root.getPropertyValue("java");
                if (javaSegment == null)
                {
                    console.writeLine("project.json root object must contain a \"java\" property.");
                }
                else if (!(javaSegment instanceof JSONObject))
                {
                    console.writeLine("\"java\" property must be a JSON object.");
                }
                else
                {
                    final JSONObject java = (JSONObject)javaSegment;

                    Folder outputsFolder = null;
                    final JSONSegment outputsSegment = java.getPropertyValue("outputs");
                    if (outputsSegment == null)
                    {
                        outputsFolder = currentFolder.getFolder("outputs");
                    }
                    else if (outputsSegment instanceof JSONQuotedString)
                    {
                        outputsFolder = currentFolder.getFolder(((JSONQuotedString)outputsSegment).toUnquotedString());
                    }
                    else
                    {
                        console.writeLine("Expected \"outputs\" property to be a quoted string.");
                    }

                    if (outputsFolder != null)
                    {
                        final List<String> classpaths = ArrayList.fromValues();
                        final JSONSegment classpathSegment = java.getPropertyValue("classpath");
                        if (classpathSegment != null)
                        {
                            if (classpathSegment instanceof JSONQuotedString)
                            {
                                final JSONQuotedString classpath = (JSONQuotedString)classpathSegment;
                                classpaths.add(classpath.toUnquotedString());
                            }
                            else if (classpathSegment instanceof JSONArray)
                            {
                                final JSONArray classpathArray = (JSONArray)classpathSegment;
                                for (final JSONSegment classpathElementSegment : classpathArray.getElements())
                                {
                                    if (!(classpathElementSegment instanceof JSONQuotedString))
                                    {
                                        console.writeLine("Expected element of \"classpath\" array to be a quoted string.");
                                    }
                                    else
                                    {
                                        final JSONQuotedString classpath = (JSONQuotedString)classpathElementSegment;
                                        classpaths.add(classpath.toUnquotedString());
                                    }
                                }
                            }
                            else
                            {
                                console.writeLine("Expected \"classpath\" to be either a quoted string or an array of quoted strings.");
                            }
                        }

                        final Function1<File, Boolean> isJavaFile = (File file) -> file.getFileExtension().equals(".java");

                        Folder sourcesFolder = null;
                        final JSONSegment sourcesSegment = java.getPropertyValue("sources");
                        if (sourcesSegment == null)
                        {
                            sourcesFolder = currentFolder.getFolder("sources");
                        }
                        else if (sourcesSegment instanceof JSONQuotedString)
                        {
                            sourcesFolder = currentFolder.getFolder(((JSONQuotedString)sourcesSegment).toUnquotedString());
                        }
                        else
                        {
                            console.writeLine("Expected \"sources\" to not exist, or to be a quoted string property.");
                        }

                        Folder sourceOutputsFolder = null;

                        if (sourcesFolder != null)
                        {
                            final Iterable<File> sourceFiles = sourcesFolder.getFilesRecursively().where(isJavaFile);
                            if (sourceFiles == null || !sourceFiles.any())
                            {
                                console.writeLine("No source files found to compile.");
                            }
                            else
                            {
                                sourceOutputsFolder = outputsFolder.getFolder(sourcesFolder.getName());

                                final List<String> sourceClasspaths = ArrayList.fromValues(classpaths);
                                sourceClasspaths.add(sourceOutputsFolder.getPath().toString());

                                compile(console, sourceClasspaths, sourceFiles, sourceOutputsFolder, debug);
                            }
                        }

                        Folder testsFolder = null;
                        final JSONSegment testsSegment = java.getPropertyValue("tests");
                        if (testsSegment == null)
                        {
                            testsFolder = currentFolder.getFolder("tests");
                        }
                        else if (testsSegment instanceof JSONQuotedString)
                        {
                            testsFolder = currentFolder.getFolder(((JSONQuotedString)testsSegment).toUnquotedString());
                        }
                        else
                        {
                            console.writeLine("Expected \"tests\" to not exist, or to be a quoted string property.");
                        }

                        if (testsFolder != null)
                        {
                            final Iterable<File> testFiles = testsFolder.getFilesRecursively().where(isJavaFile);
                            if (testFiles == null || !testFiles.any())
                            {
                                console.writeLine("No test files found to compile.");
                            }
                            else
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

    private static void compile(Console console, Iterable<String> classpaths, Iterable<File> filesToCompile, Folder outputFolder, boolean debug)
    {
        final ProcessBuilder javac = console.getProcessBuilder("javac");
        javac.redirectOutput(console.getOutputAsByteWriteStream());
        javac.redirectError(console.getErrorAsByteWriteStream());

        addNamedArgument(javac, "-classpath", String.join(";", classpaths));
        addNamedArgument(javac, "-d", outputFolder.getPath().toString());
        javac.addArgument("-g");

        javac.addArguments(filesToCompile.map(file -> file.getPath().toString()));

        if (debug)
        {
            console.writeLine(javac.getCommand());
        }

        javac.run();
    }

    private static void addNamedArgument(ProcessBuilder builder, String argumentName, String argumentValue)
    {
        if (argumentValue != null && !argumentValue.isEmpty())
        {
            builder.addArguments(argumentName, argumentValue);
        }
    }
}
