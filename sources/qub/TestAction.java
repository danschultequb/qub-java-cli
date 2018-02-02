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

                    Folder outputsFolder = null;
                    final JSONSegment outputsSegment = javaObject.getPropertyValue("outputs");
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

                    if (debug)
                    {
                        console.writeLine("Outputs: \"" + outputsFolder + "\"");
                    }

                    if (outputsFolder != null)
                    {
                        Folder testsFolder = null;
                        final JSONSegment testsSegment = javaObject.getPropertyValue("tests");
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
                        Folder testOutputsFolder = testsFolder == null ? null : outputsFolder.getFolder(testsFolder.getName());

                        if (testOutputsFolder != null)
                        {
                            Folder sourcesFolder = null;
                            final JSONSegment sourcesSegment = javaObject.getPropertyValue("sources");
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
                            final Folder sourceOutputsFolder = sourcesFolder == null ? null : outputsFolder.getFolder(sourcesFolder.getName());

                            final List<String> classpaths = new ArrayList<>();
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

                                File coverageExecFile = null;
                                if (coverage)
                                {
                                    coverageExecFile = outputsFolder.getFile("coverage.exec");
                                    java.addArgument("-javaagent:C:/qub/jacoco/jacococli/0.8.0/jacocoagent.jar=destfile=" + coverageExecFile.getPath().toString());
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
                                    final Folder coverageFolder = outputsFolder.getFolder("coverage");

                                    final ProcessBuilder jacococli = console.getProcessBuilder("jacococli");
                                    jacococli.redirectOutput(console.getOutputAsByteWriteStream());
                                    jacococli.redirectError(console.getErrorAsByteWriteStream());
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
    }

    private static void addNamedArgument(ProcessBuilder builder, String argumentName, String argumentValue)
    {
        if (argumentValue != null && !argumentValue.isEmpty())
        {
            builder.addArguments(argumentName, argumentValue);
        }
    }
}
