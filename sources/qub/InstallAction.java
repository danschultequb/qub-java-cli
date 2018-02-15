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
        final Stopwatch totalInstall = console.getStopwatch();
        totalInstall.start();

        final JSONObject projectJsonRoot = QubCLI.readProjectJson(console);
        if (projectJsonRoot != null)
        {
            final JSONSegment publisherSegment = projectJsonRoot.getPropertyValue("publisher");
            if (publisherSegment == null)
            {
                console.writeLine("A \"publisher\" quoted-string property must be specified in the root object of the project.json file.");
            }
            else if (!(publisherSegment instanceof JSONQuotedString))
            {
                console.writeLine("The \"publisher\" property in the root object of the project.json file must be a quoted-string.");
            }
            else
            {
                final String publisher = ((JSONQuotedString)publisherSegment).toUnquotedString();

                final String project = QubCLI.getProject(console, projectJsonRoot);
                if (project != null && !project.isEmpty())
                {
                    final JSONSegment versionSegment = projectJsonRoot.getPropertyValue("version");
                    if (versionSegment == null)
                    {
                        console.writeLine("A \"version\" quoted-string property must be specified in the root object of the project.json file.");
                    }
                    else if (!(versionSegment instanceof JSONQuotedString))
                    {
                        console.writeLine("The \"version\" property in the root object of the project.json file must be a quoted-string.");
                    }
                    else
                    {
                        final String version = ((JSONQuotedString)versionSegment).toUnquotedString();

                        final JSONObject java = QubCLI.getJavaSegment(console, projectJsonRoot);
                        if (java != null)
                        {
                            final Folder outputsFolder = QubCLI.getOutputsFolder(console, java);
                            if (outputsFolder != null)
                            {
                                String jarFileName = null;
                                final JSONSegment jarFileNameSegment = java.getPropertyValue("jarFileName");
                                if (jarFileNameSegment instanceof JSONQuotedString)
                                {
                                    jarFileName = ((JSONQuotedString)jarFileNameSegment).toUnquotedString();
                                }
                                if (jarFileName == null || jarFileName.isEmpty())
                                {
                                    jarFileName = project;
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
                                    final File outputsJarFile = outputsFolder.getFile(jarFileName);

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
                                    installedProjectJsonFile.setContents(projectJsonRoot.toString(), CharacterEncoding.UTF_8);
                                    console.writeLine(" Done (" + stopwatch.stop().toSeconds().toString("#.#") + ")");

                                    final String mainClass = QubCLI.getMainClass(console, java);
                                    if (mainClass != null && !mainClass.isEmpty())
                                    {
                                        String shortcutName = null;
                                        final JSONSegment shortcutNameSegment = java.getPropertyValue("shortcutName");
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

                                        String classpath = "%~dp0" + installedJarFile.getPath().relativeTo(qubFolder.getPath()).toString();
                                        for (final Dependency dependency : QubCLI.getDependencies(console, java))
                                        {
                                            final String dependencyPublisher = dependency.getPublisher();
                                            final String dependencyProject = dependency.getProject();
                                            final String dependencyVersion = dependency.getVersion();
                                            classpath += ";%~dp0" + dependencyPublisher + "/" + dependencyProject + "/" + dependencyVersion + "/" + dependencyProject + ".jar";
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
        console.writeLine("Total Duration: " + totalBuildDuration.toString("0.0"));
    }
}
