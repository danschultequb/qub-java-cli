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
            JSONDocument projectJsonDocument = null;
            try (final CharacterReadStream projectJsonFileReadStream = projectJsonFile.getContentByteReadStream().asCharacterReadStream())
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

                    final String output;
                    final JSONSegment outputSegment = java.getPropertyValue("output");
                    if (outputSegment == null)
                    {
                        output = "output";
                    }
                    else if (!(outputSegment instanceof JSONQuotedString))
                    {
                        console.writeLine("Expected \"output\" property to be a quoted string.");
                        output = "output";
                    }
                    else
                    {
                        final JSONQuotedString outputString = (JSONQuotedString)outputSegment;
                        output = outputString.toUnquotedString();
                    }

                    final List<String> classpaths = ArrayList.fromValues(output);
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

                    final List<File> sourceFiles = new ArrayList<>();
                    final JSONSegment sourcesSegment = java.getPropertyValue("sources");
                    if (sourcesSegment == null)
                    {
                        console.writeLine("Expected \"java\" to have a \"sources\" quoted string property.");
                    }
                    else if (!(sourcesSegment instanceof JSONQuotedString))
                    {
                        console.writeLine("Expected \"sources\" to be a quoted string property.");
                    }
                    else
                    {
                        final JSONQuotedString sourcesString = (JSONQuotedString)sourcesSegment;
                        final Path sourcesPath = Path.parse(sourcesString.toUnquotedString());
                        if (currentFolder.fileExists(sourcesPath))
                        {
                            sourceFiles.add(currentFolder.getFile(sourcesPath));
                        }
                        else if (currentFolder.folderExists(sourcesPath))
                        {
                            final Folder sourceFolder = currentFolder.getFolder(sourcesPath);
                            sourceFiles.addAll(sourceFolder.getFilesRecursively()
                                .where(file -> file.getFileExtension().equals(".java")));
                        }
                        else
                        {
                            final PathPattern sourcesPattern = PathPattern.parse(sourcesPath);
                            sourceFiles.addAll(currentFolder.getFilesRecursively()
                                .where(file -> sourcesPattern.isMatch(file.getPath())));
                        }

                        final JSONSegment testsSegment = java.getPropertyValue("tests");
                        if (testsSegment != null)
                        {
                            if (!(testsSegment instanceof JSONQuotedString))
                            {
                                console.writeLine("Expected \"tests\" to be a quoted string property.");
                            }
                            else
                            {
                                final JSONQuotedString testsString = (JSONQuotedString)testsSegment;
                                final Path testsPath = Path.parse(testsString.toUnquotedString());
                                if (currentFolder.fileExists(testsPath))
                                {
                                    sourceFiles.add(currentFolder.getFile(testsPath));
                                }
                                else if (currentFolder.folderExists(testsPath))
                                {
                                    final Folder testFolder = currentFolder.getFolder(testsPath);
                                    sourceFiles.addAll(testFolder.getFilesRecursively()
                                        .where(file -> file.getFileExtension().equals(".java")));
                                }
                                else
                                {
                                    final PathPattern testsPattern = PathPattern.parse(testsPath);
                                    sourceFiles.addAll(currentFolder.getFilesRecursively()
                                        .where(file -> testsPattern.isMatch(file.getPath())));
                                }
                            }
                        }

                        if (!sourceFiles.any())
                        {
                            console.writeLine("Didn't find any Java source files.");
                        }
                        else
                        {
                            final ProcessBuilder javac = console.getProcessBuilder("javac");
                            javac.redirectOutput(console.getOutputAsByteWriteStream());
                            javac.redirectError(console.getErrorAsByteWriteStream());

                            addNamedArgument(javac, "-classpath", String.join(";", classpaths));
                            addNamedArgument(javac, "-d", output);
                            javac.addArgument("-g");

                            javac.addArguments(sourceFiles.map(file -> file.getPath().toString()));

                            if (debug)
                            {
                                console.writeLine(javac.getCommand());
                            }

                            javac.run();
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
