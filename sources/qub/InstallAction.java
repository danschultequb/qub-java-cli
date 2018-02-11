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
        final boolean debug = (console.getCommandLine().get("debug") != null);

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

                final JSONSegment projectSegment = projectJsonRoot.getPropertyValue("project");
                if (projectSegment == null)
                {
                    console.writeLine("A \"project\" quoted-string property must be specified in the root object of the project.json file.");
                }
                else if (!(projectSegment instanceof JSONQuotedString))
                {
                    console.writeLine("The \"project\" property in the root object of the project.json file must be a quoted-string.");
                }
                else
                {
                    final String project = ((JSONQuotedString)projectSegment).toUnquotedString();

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

                                    final Folder qubFolder = console.getFileSystem().getFolder("C:/qub");
                                    final Folder publisherFolder = qubFolder.getFolder(publisher);
                                    final Folder projectFolder = publisherFolder.getFolder(project);
                                    final Folder versionFolder = projectFolder.getFolder(version);
                                    final File installedJarFile = versionFolder.getFile(jarFileName);

                                    installedJarFile.setContents(outputsJarFile.getContents());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
