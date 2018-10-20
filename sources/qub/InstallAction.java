package qub;

/**
 * A QubCLI action that installs code projects.
 */
public class InstallAction implements Action
{
    @Override
    public String getName()
    {
        return "Install";
    }

    @Override
    public String getDescription()
    {
        return "Install the coding project in the current folder into the system's Qub.";
    }

    @Override
    public String getArgumentUsage()
    {
        return null;
    }

    @Override
    public void run(Console console)
    {
        final boolean runTests = parseRunTests(console);

        final ProjectJson projectJson = ProjectJson.parse(console);
        if (projectJson == null)
        {
            console.writeLine("Not installing because there was no project.json.");
        }
        else
        {
            boolean shouldInstall = true;

            if (runTests)
            {
                console.writeLine("Running tests...");
                final boolean coverage = projectJson.getJavaTestsLineCoverageRequirement() != null;
                shouldInstall = TestAction.run(console, false, null, coverage);
            }
            else
            {
                console.writeLine("Skipping tests...");
                shouldInstall = BuildAction.run(console, false);
            }

            if (!shouldInstall)
            {
                console.writeLine();
                console.writeLine("Not installing because the tests failed.");
            }
            if (shouldInstall)
            {
                console.writeLine();

                final Stopwatch totalInstall = console.getStopwatch();
                totalInstall.start();

                final JSONObject rootObject = projectJson.getRootObject();
                if (rootObject == null)
                {
                    console.writeLine("The project.json file must have a root object.");
                }
                else
                {
                    final String publisher = projectJson.getPublisher();
                    if (Strings.isNullOrEmpty(publisher))
                    {
                        console.writeLine("The project.json file must have a publisher property.");
                    }
                    else
                    {
                        final String project = projectJson.getProject();
                        if (Strings.isNullOrEmpty(project))
                        {
                            console.writeLine("The project.json file must have a project property.");
                        }
                        else
                        {
                            final String version = projectJson.getVersion();
                            if (Strings.isNullOrEmpty(version))
                            {
                                console.writeLine("The project.json file must have a version property.");
                            }
                            else
                            {
                                final JSONObject javaObject = projectJson.getJavaObject();
                                if (javaObject != null)
                                {
                                    final Folder javaOutputsFolder = projectJson.getJavaOutputsFolder();
                                    if (javaOutputsFolder != null)
                                    {
                                        String jarFileName = null;
                                        final JSONSegment jarFileNameSegment = javaObject.getPropertyValue("jarFileName");
                                        if (jarFileNameSegment instanceof JSONQuotedString)
                                        {
                                            jarFileName = ((JSONQuotedString)jarFileNameSegment).toUnquotedString();
                                        }
                                        if (jarFileName == null || jarFileName.isEmpty())
                                        {
                                            jarFileName = project;
                                        }

                                        if (!jarFileName.endsWith(".jar"))
                                        {
                                            jarFileName += ".jar";
                                        }
                                        final File outputsJarFile = javaOutputsFolder.getFile(jarFileName).getValue();

                                        final Folder qubFolder = QubCLI.getQubFolder(console);
                                        final Folder publisherFolder = qubFolder.getFolder(publisher).getValue();
                                        final Folder projectFolder = publisherFolder.getFolder(project).getValue();
                                        final Folder versionFolder = projectFolder.getFolder(version).getValue();

                                        if (versionFolder.exists().getValue())
                                        {
                                            console.writeLine("This package (" + publisher + "/" + project + ":" + version + ") can't be installed because a package with that signature already exists.");
                                        }
                                        else
                                        {
                                            final File installedJarFile = versionFolder.getFile(jarFileName).getValue();

                                            final Stopwatch stopwatch = console.getStopwatch();
                                            console.write("Copying " + outputsJarFile + " to " + installedJarFile + "...");
                                            stopwatch.start();
                                            installedJarFile.setContents(outputsJarFile.getContents().getValue());
                                            console.writeLine(" Done (" + stopwatch.stop().toSeconds().toString("#.#") + ")");

                                            final File installedProjectJsonFile = versionFolder.getFile("project.json").getValue();
                                            console.write("Copying project.json to " + installedProjectJsonFile + "...");
                                            stopwatch.start();
                                            installedProjectJsonFile.setContents(CharacterEncoding.UTF_8.encode(rootObject.toString()).getValue());
                                            console.writeLine(" Done (" + stopwatch.stop().toSeconds().toString("#.#") + ")");

                                            final String mainClass = projectJson.getMainClass();
                                            if (mainClass != null)
                                            {
                                                String shortcutName = null;
                                                final JSONSegment shortcutNameSegment = javaObject.getPropertyValue("shortcutName");
                                                if (shortcutNameSegment != null)
                                                {
                                                    if (!(shortcutNameSegment instanceof JSONQuotedString))
                                                    {
                                                        console.writeLine("The \"shortcutName\" property in the java section of the project.json file must be a quoted-string.");
                                                    }
                                                    else
                                                    {
                                                        shortcutName = ((JSONQuotedString)shortcutNameSegment).toUnquotedString();
                                                    }
                                                }
                                                if (shortcutName == null || shortcutName.isEmpty())
                                                {
                                                    shortcutName = installedJarFile.getNameWithoutFileExtension();
                                                }

                                                String classpath = "%~dp0" + installedJarFile.relativeTo(qubFolder);
                                                for (final Dependency dependency : projectJson.getResolvedDependencies(qubFolder))
                                                {
                                                    classpath += ";%~dp0" + dependency.toString();
                                                }

                                                final File shortcutFile = qubFolder.getFile(shortcutName + ".cmd").getValue();
                                                final String shortcutFileContents =
                                                    "@echo OFF\n" +
                                                    "java -cp " + classpath + " " + mainClass + " %*\n";
                                                console.write("Writing " + shortcutFile + "...");
                                                stopwatch.start();
                                                shortcutFile.setContents(CharacterEncoding.UTF_8.encode(shortcutFileContents).getValue());
                                                console.writeLine(" Done (" + stopwatch.stop().toSeconds().toString("#.#") + ")");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                final Duration totalBuildDuration = totalInstall.stop().toSeconds();
                console.writeLine("Install Duration: " + totalBuildDuration.toString("0.0"));
            }
        }
    }

    static boolean parseRunTests(Console console)
    {
        CommandLineArgument runTestsArgument = console.getCommandLine().remove("runTests");
        if (runTestsArgument == null)
        {
            runTestsArgument = console.getCommandLine().remove("run-tests");
        }
        return runTestsArgument == null || (runTestsArgument.getValue() == null || runTestsArgument.getValue().equalsIgnoreCase("true"));
    }
}
