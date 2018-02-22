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
    private final JSONObject javaTestsObject;
    private final Folder javaTestsFolder;
    private final String javaTestsVersion;
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
                JSONObject javaTestsObject,
                Folder javaTestsFolder,
                String javaTestsVersion,
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
        this.javaTestsObject = javaTestsObject;
        this.javaTestsFolder = javaTestsFolder;
        this.javaTestsVersion = javaTestsVersion;
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

    public JSONObject getJavaTestsObject()
    {
        return javaTestsObject;
    }

    public Folder getJavaTestsFolder()
    {
        return javaTestsFolder;
    }

    public String getJavaTestsVersion()
    {
        return javaTestsVersion;
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
                JSONObject javaTestsObject = null;
                Folder javaSourcesFolder = null;
                String javaSourcesVersion = null;
                JSONObject javaSourcesObject = null;
                Folder javaTestsFolder = null;
                String javaTestsVersion = null;
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
                                if (mainClass.isEmpty())
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

                        final JSONSegment javaSourcesSegment = javaObject.getPropertyValue("sources");
                        String sources = null;
                        if (javaSourcesSegment == null)
                        {
                            sources = "sources";
                        }
                        else if (javaSourcesSegment instanceof JSONQuotedString)
                        {
                            sources = ((JSONQuotedString)javaSourcesSegment).toUnquotedString();
                            if (sources.isEmpty())
                            {
                                console.writeLine("Expected \"sources\" to not exist, be a non-empty quoted-string property, or be an object property.");
                                sources = null;
                            }
                        }
                        else if (javaSourcesSegment instanceof JSONObject)
                        {
                            javaSourcesObject = (JSONObject)javaSourcesSegment;

                            final JSONSegment javaSourcesFolderSegment = javaSourcesObject.getPropertyValue("folder");
                            if (javaSourcesFolderSegment == null)
                            {
                                sources = "sources";
                            }
                            else if (javaSourcesFolderSegment instanceof JSONQuotedString)
                            {
                                sources = ((JSONQuotedString)javaSourcesFolderSegment).toUnquotedString();
                                if (sources.isEmpty())
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
                                    if (javaSourcesVersion.isEmpty())
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

                        final JSONSegment testsSegment = javaObject.getPropertyValue("tests");
                        String tests = null;
                        if (testsSegment == null)
                        {
                            tests = "tests";
                        }
                        else if (testsSegment instanceof JSONQuotedString)
                        {
                            tests = ((JSONQuotedString)testsSegment).toUnquotedString();
                            if (tests.isEmpty())
                            {
                                console.writeLine("Expected \"tests\" to not exist, be a non-empty quoted-string property, or be an object property.");
                                tests = null;
                            }
                        }
                        else if (testsSegment instanceof JSONObject)
                        {
                            javaTestsObject = (JSONObject)testsSegment;

                            final JSONSegment javaTestsFolderSegment = javaTestsObject.getPropertyValue("folder");
                            if (javaTestsFolderSegment == null)
                            {
                                tests = "tests";
                            }
                            else if (javaTestsFolderSegment instanceof JSONQuotedString)
                            {
                                tests = ((JSONQuotedString)javaTestsFolderSegment).toUnquotedString();
                                if (tests.isEmpty())
                                {
                                    console.writeLine("Expected \"folder\" property in \"tests\" section to be a non-empty quoted-string property.");
                                    tests = null;
                                }
                            }
                            else
                            {
                                console.writeLine("Expected \"folder\" property in the \"tests\" section to be a non-empty quoted-string property.");
                            }

                            final JSONSegment javaTestsVersionSegment = javaTestsObject.getPropertyValue("version");
                            if (javaTestsVersionSegment != null)
                            {
                                if (!(javaTestsVersionSegment instanceof JSONQuotedString))
                                {
                                    console.writeLine("Expected \"version\" property in \"tests\" section to be a non-empty quoted-string property.");
                                }
                                else
                                {
                                    javaTestsVersion = ((JSONQuotedString)javaTestsVersionSegment).toUnquotedString();
                                    if (javaTestsVersion.isEmpty())
                                    {
                                        console.writeLine("Expected \"version\" property in \"tests\" section to be a non-empty quoted-string property.");
                                        javaTestsVersion = null;
                                    }
                                }
                            }
                        }
                        else
                        {
                            console.writeLine("Expected \"tests\" to not exist, be a non-empty quoted-string property, or be an object property.");
                        }

                        if (tests != null)
                        {
                            javaTestsFolder = currentFolder.getFolder(tests);
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
                            if (outputs.isEmpty())
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
                    javaTestsObject,
                    javaTestsFolder,
                    javaTestsVersion,
                    javaOutputsFolder);
            }
        }

        return result;
    }
}
