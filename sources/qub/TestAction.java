package qub;

import java.awt.*;
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
                final JSONObject root = (JSONObject) rootSegment;
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
                    final JSONObject javaObject = (JSONObject) javaSegment;

                    final String output;
                    final JSONSegment outputSegment = javaObject.getPropertyValue("output");
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
                        final JSONQuotedString outputString = (JSONQuotedString) outputSegment;
                        output = outputString.toUnquotedString();
                    }

                    if (debug)
                    {
                        console.writeLine("Output: \"" + output + "\"");
                    }

                    final List<String> classpaths = ArrayList.fromValues(output);
                    final JSONSegment classpathSegment = javaObject.getPropertyValue("classpath");
                    if (classpathSegment != null)
                    {
                        if (classpathSegment instanceof JSONQuotedString)
                        {
                            final JSONQuotedString classpath = (JSONQuotedString) classpathSegment;
                            classpaths.add(classpath.toUnquotedString());
                        }
                        else if (classpathSegment instanceof JSONArray)
                        {
                            final JSONArray classpathArray = (JSONArray) classpathSegment;
                            for (final JSONSegment classpathElementSegment : classpathArray.getElements())
                            {
                                if (!(classpathElementSegment instanceof JSONQuotedString))
                                {
                                    console.writeLine("Expected element of \"classpath\" array to be a quoted string.");
                                }
                                else
                                {
                                    final JSONQuotedString classpath = (JSONQuotedString) classpathElementSegment;
                                    classpaths.add(classpath.toUnquotedString());
                                }
                            }
                        }
                        else
                        {
                            console.writeLine("Expected \"classpath\" to be either a quoted string or an array of quoted strings.");
                        }
                    }
                    final String classpath = String.join(";", classpaths);

                    if (debug)
                    {
                        console.writeLine("Classpath: \"" + classpath + "\"");
                    }

                    Path outputFolderPath = Path.parse(output);
                    if (!outputFolderPath.isRooted())
                    {
                        outputFolderPath = currentFolder.getPath().concatenateSegment(outputFolderPath);
                    }

                    final Folder outputFolder = console.getFileSystem().getFolder(outputFolderPath);
                    final Iterable<File> outputFiles = outputFolder.getFilesRecursively();
                    if (outputFiles == null || !outputFiles.any())
                    {
                        console.writeLine("No files found in output folder (" + outputFolder.getPath().toString() + ").");
                    }
                    else
                    {
                        final JSONSegment testsSegment = javaObject.getPropertyValue("tests");
                        if (testsSegment != null)
                        {
                            if (!(testsSegment instanceof JSONQuotedString))
                            {
                                console.writeLine("Expected \"tests\" to be a quoted string property.");
                            }
                            else
                            {
                                final JSONQuotedString testsString = (JSONQuotedString) testsSegment;
                                final Path testsPath = Path.parse(testsString.toUnquotedString());
                                if (!currentFolder.folderExists(testsPath))
                                {
                                    console.writeLine("No folder found at provided \"tests\" property.");
                                }
                                else
                                {
                                    final Folder testFolder = currentFolder.getFolder(testsPath);
                                    final Iterable<Path> relativeTestSourcePaths = testFolder.getFilesRecursively()
                                        .where(file -> file.getFileExtension().equals(".java"))
                                        .map(file -> file.getPath().relativeTo(testFolder.getPath()));

                                    final Iterable<String> fullTestClassNames = relativeTestSourcePaths
                                        .where((Path relativeTestSourcePath) ->
                                        {
                                            if (debug)
                                            {
                                                console.writeLine("Checking if \"" + relativeTestSourcePath.toString() + "\" has .class file.");
                                            }
                                            final Path relativeTestSourcePathWithoutExtension = relativeTestSourcePath.withoutFileExtension();
                                            final Path testClassFilePath = relativeTestSourcePathWithoutExtension.concatenate(".class");
                                            return outputFolder.fileExists(testClassFilePath);
                                        })
                                        .map((Path relativeTestSourcePath) ->
                                            relativeTestSourcePath.withoutFileExtension().toString().replace('/', '.').replace('\\', '.'));

                                    final ProcessBuilder java = console.getProcessBuilder("java");
                                    java.redirectOutput(console.getOutputAsByteWriteStream());
                                    java.redirectError(console.getErrorAsByteWriteStream());

                                    if (coverage)
                                    {
                                        java.addArgument("-javaagent:C:/qub/jacoco/jacococli/0.8.0/jacocoagent.jar=destfile=output/coverage.exec");
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

                                    if (coverage)
                                    {
                                        final ProcessBuilder jacococli = console.getProcessBuilder("jacococli");
                                        jacococli.redirectOutput(console.getOutputAsByteWriteStream());
                                        jacococli.redirectError(console.getErrorAsByteWriteStream());
                                        jacococli.addArgument("report");
                                        jacococli.addArgument("output/coverage.exec");
                                        jacococli.addArguments("--classfiles", "output");
                                        jacococli.addArguments("--sourcefiles", "src/main/java");
                                        jacococli.addArguments("--html", "output/coverage/");
                                        if (debug)
                                        {
                                            console.writeLine("Command: \"" + jacococli.getCommand() + "\"");
                                        }
                                        jacococli.run();

                                        try
                                        {
                                            Desktop.getDesktop().open(new java.io.File("output/coverage/index.html"));
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
