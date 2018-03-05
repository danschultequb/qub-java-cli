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
        if (TestAction.run(console, false, null, false))
        {
            console.writeLine();

            final Stopwatch totalInstall = console.getStopwatch();
            totalInstall.start();

            final ProjectJson projectJson = ProjectJson.parse(console);
            if (projectJson != null)
            {
                final JSONObject rootObject = projectJson.getRootObject();
                if (rootObject != null)
                {
                    final String publisher = projectJson.getPublisher();
                    if (publisher != null)
                    {
                        final String project = projectJson.getProject();
                        if (project != null)
                        {
                            final String version = projectJson.getVersion();
                            if (version != null)
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
                                        final File outputsJarFile = javaOutputsFolder.getFile(jarFileName);

                                        final Folder qubFolder = QubCLI.getQubFolder(console);
                                        final Folder publisherFolder = qubFolder.getFolder(publisher);
                                        final Folder projectFolder = publisherFolder.getFolder(project);
                                        final Folder versionFolder = projectFolder.getFolder(version);

                                        final File installedJarFile = versionFolder.getFile(jarFileName);

                                        final Stopwatch stopwatch = console.getStopwatch();
                                        console.write("Copying " + outputsJarFile + " to " + installedJarFile + "...");
                                        stopwatch.start();
                                        installedJarFile.setContents(outputsJarFile.getContents());
                                        console.writeLine(" Done (" + stopwatch.stop().toSeconds().toString("#.#") + ")");

                                        final File installedProjectJsonFile = versionFolder.getFile("project.json");
                                        console.write("Copying project.json to " + installedProjectJsonFile + "...");
                                        stopwatch.start();
                                        installedProjectJsonFile.setContents(rootObject.toString(), CharacterEncoding.UTF_8);
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
                                            for (final Dependency dependency : projectJson.getDependencies())
                                            {
                                                classpath += ";%~dp0" + dependency.toString();
                                            }

                                            final File shortcutFile = qubFolder.getFile(shortcutName + ".cmd");
                                            final String shortcutFileContents =
                                                "@echo OFF\n" +
                                                "java -cp " + classpath + " " + mainClass + " %*\n";
                                            console.write("Writing " + shortcutFile + "...");
                                            stopwatch.start();
                                            shortcutFile.setContents(shortcutFileContents, CharacterEncoding.UTF_8);
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
