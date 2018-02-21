package qub;

public class ProjectJson
{
    private final JSONObject rootObject;
    private final String publisher;
    private final String project;
    private final String version;
    private final JSONObject javaObject;
    private final String mainClass;
    private final JSONObject javaSourcesObject;
    private final Folder javaSourcesFolder;
    private final String javaSourcesVersion;
    private final Folder javaOutputsFolder;

    ProjectJson(JSONObject rootObject,
                String publisher,
                String project,
                String version,
                JSONObject javaObject,
                String mainClass,
                JSONObject javaSourcesObject,
                Folder javaSourcesFolder,
                String javaSourcesVersion,
                Folder javaOutputsFolder)
    {
        this.rootObject = rootObject;
        this.publisher = publisher;
        this.project = project;
        this.version = version;
        this.javaObject = javaObject;
        this.mainClass = mainClass;
        this.javaSourcesObject = javaSourcesObject;
        this.javaSourcesFolder = javaSourcesFolder;
        this.javaSourcesVersion = javaSourcesVersion;
        this.javaOutputsFolder = javaOutputsFolder;
    }

    public JSONObject getRootObject()
    {
        return rootObject;
    }

    public String getPublisher()
    {
        return publisher;
    }

    public String getProject()
    {
        return project;
    }

    public String getVersion()
    {
        return version;
    }

    public JSONObject getJavaObject()
    {
        return javaObject;
    }

    public String getMainClass()
    {
        return mainClass;
    }

    public JSONObject getJavaSourcesObject()
    {
        return javaSourcesObject;
    }

    public Folder getJavaSourcesFolder()
    {
        return javaSourcesFolder;
    }

    public String getJavaSourcesVersion()
    {
        return javaSourcesVersion;
    }

    public Folder getJavaOutputsFolder()
    {
        return javaOutputsFolder;
    }

    public static ProjectJson parse(Console console)
    {
        ProjectJson result = null;

        if (console != null)
        {
            final File projectJsonFile = console.getCurrentFolder().getFile("project.json");
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

                JSONObject rootObject = null;
                String publisher = null;
                String project = null;
                String version = null;
                JSONObject javaObject = null;
                String mainClass = null;
                JSONObject javaSourcesObject = null;
                Folder javaSourcesFolder = null;
                String javaSourcesVersion = null;
                Folder javaOutputsFolder = null;

                final JSONSegment rootSegment = projectJsonDocument.getRoot();
                if (!(rootSegment instanceof JSONObject))
                {
                    console.writeLine("project.json root segment must be a JSON object.");
                }
                else
                {
                    rootObject = (JSONObject)rootSegment;

                    final JSONSegment publisherSegment = rootObject.getPropertyValue("publisher");
                    if (publisherSegment == null)
                    {
                        console.writeLine("A \"publisher\" quoted-string property must be specified in the rootObject object of the project.json file.");
                    }
                    else if (!(publisherSegment instanceof JSONQuotedString))
                    {
                        console.writeLine("The \"publisher\" property in the rootObject object of the project.json file must be a non-empty quoted-string.");
                    }
                    else
                    {
                        publisher = ((JSONQuotedString)publisherSegment).toUnquotedString();
                        if (publisher.isEmpty())
                        {
                            console.writeLine("The \"publisher\" property in the rootObject object of the project.json file must be a non-empty quoted-string.");
                            publisher = null;
                        }
                    }

                    final JSONSegment projectSegment = rootObject.getPropertyValue("project");
                    if (projectSegment == null)
                    {
                        console.writeLine("A \"project\" quoted-string property must be specified in the rootObject object of the project.json file.");
                    }
                    else if (!(projectSegment instanceof JSONQuotedString))
                    {
                        console.writeLine("The \"project\" property in the rootObject object of the project.json file must be a non-empty quoted-string.");
                    }
                    else
                    {
                        project = ((JSONQuotedString)projectSegment).toUnquotedString();
                        if (project.isEmpty())
                        {
                            console.writeLine("The \"project\" property in the rootObject object of the project.json file must be a non-empty quoted-string.");
                            project = null;
                        }
                    }

                    final JSONSegment versionSegment = rootObject.getPropertyValue("version");
                    if (versionSegment == null)
                    {
                        console.writeLine("A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.");
                    }
                    else if (!(versionSegment instanceof JSONQuotedString))
                    {
                        console.writeLine("The \"version\" property in the rootObject object of the project.json file must be a non-empty quoted-string.");
                    }
                    else
                    {
                        version = ((JSONQuotedString)versionSegment).toUnquotedString();
                        if (version.isEmpty())
                        {
                            console.writeLine("The \"version\" property in the rootObject object of the project.json file must be a non-empty quoted-string.");
                            version = null;
                        }
                    }

                    final JSONSegment javaSegment = rootObject.getPropertyValue("java");
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
                        javaObject = (JSONObject)javaSegment;

                        final JSONSegment mainClassSegment = javaObject.getPropertyValue("mainClass");
                        if (mainClassSegment != null)
                        {
                            if (mainClassSegment instanceof JSONQuotedString)
                            {
                                mainClass = ((JSONQuotedString)mainClassSegment).toUnquotedString();
                                if (mainClass == null || mainClass.isEmpty())
                                {
                                    console.writeLine("The \"mainClass\" property in the java object of the project.json file must be a non-empty quoted-string.");
                                    mainClass = null;
                                }
                            }
                            else
                            {
                                console.writeLine("The \"mainClass\" property in the java object of the project.json file must be a non-empty quoted-string.");
                            }
                        }

                        final Folder currentFolder = console.getCurrentFolder();

                        final JSONSegment sourcesSegment = javaObject.getPropertyValue("sources");
                        String sources = null;
                        if (sourcesSegment == null)
                        {
                            sources = "sources";
                        }
                        else if (sourcesSegment instanceof JSONQuotedString)
                        {
                            sources = ((JSONQuotedString)sourcesSegment).toUnquotedString();
                            if (sources == null || sources.isEmpty())
                            {
                                console.writeLine("Expected \"sources\" to not exist, be a non-empty quoted-string property, or be an object property.");
                                sources = null;
                            }
                        }
                        else if (sourcesSegment instanceof JSONObject)
                        {
                            javaSourcesObject = (JSONObject)sourcesSegment;

                            final JSONSegment javaSourcesFolderSegment = javaSourcesObject.getPropertyValue("folder");
                            if (javaSourcesFolderSegment == null)
                            {
                                sources = "sources";
                            }
                            else if (javaSourcesFolderSegment instanceof JSONQuotedString)
                            {
                                sources = ((JSONQuotedString)javaSourcesFolderSegment).toUnquotedString();
                                if (sources == null || sources.isEmpty())
                                {
                                    console.writeLine("Expected \"folder\" property in \"sources\" section to not exist, be a non-empty quoted-string property, or be an object property.");
                                    sources = null;
                                }
                            }
                            else
                            {
                                console.writeLine("Expected \"folder\" property in the \"sources\" section to be a non-empty quoted-string property.");
                            }

                            final JSONSegment javaSourcesVersionSegment = javaSourcesObject.getPropertyValue("version");
                            if (javaSourcesVersionSegment != null)
                            {
                                if (!(javaSourcesVersionSegment instanceof JSONQuotedString))
                                {
                                    console.writeLine("Expected \"version\" property in \"sources\" section to be a non-empty quoted-string property.");
                                }
                                else
                                {
                                    javaSourcesVersion = ((JSONQuotedString)javaSourcesVersionSegment).toUnquotedString();
                                    if (javaSourcesVersion == null || javaSourcesVersion.isEmpty())
                                    {
                                        console.writeLine("Expected \"version\" property in \"sources\" section to be a non-empty quoted-string property.");
                                        javaSourcesVersion = null;
                                    }
                                }
                            }
                        }
                        else
                        {
                            console.writeLine("Expected \"sources\" to not exist, be a non-empty quoted-string property, or be an object property.");
                        }

                        if (sources != null)
                        {
                            javaSourcesFolder = currentFolder.getFolder(sources);
                        }

                        final JSONSegment outputsSegment = javaObject.getPropertyValue("outputs");
                        String outputs = null;
                        if (outputsSegment == null)
                        {
                            outputs = "outputs";
                        }
                        else if (outputsSegment instanceof JSONQuotedString)
                        {
                            outputs = ((JSONQuotedString)outputsSegment).toUnquotedString();
                            if (outputs == null || outputs.isEmpty())
                            {
                                console.writeLine("Expected \"outputs\" property in \"java\" section to be a non-empty quoted-string.");
                                outputs = null;
                            }
                        }
                        else
                        {
                            console.writeLine("Expected \"outputs\" property in \"java\" section to be a non-empty quoted-string.");
                        }

                        if (outputs != null)
                        {
                            javaOutputsFolder = currentFolder.getFolder(outputs);
                        }
                    }
                }

                result = new ProjectJson(
                    rootObject,
                    publisher,
                    project,
                    version,
                    javaObject,
                    mainClass,
                    javaSourcesObject,
                    javaSourcesFolder,
                    javaSourcesVersion,
                    javaOutputsFolder);
            }
        }

        return result;
    }
}
