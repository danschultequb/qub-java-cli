package qub;

public class ProjectJson
{
    private final JSONObject rootObject;
    private final String publisher;
    private final String project;
    private final String version;
    private final JSONObject javaObject;
    private final String mainClass;
    private final Iterable<String> classpath;
    private final JSONObject javaSourcesObject;
    private final Folder javaSourcesFolder;
    private final String javaSourcesVersion;
    private final JSONObject javaTestsObject;
    private final Folder javaTestsFolder;
    private final String javaTestsVersion;
    private final Double javaTestsLineCoverageRequirement;
    private final Folder javaOutputsFolder;
    private final Iterable<Dependency> dependencies;

    ProjectJson(JSONObject rootObject,
                String publisher,
                String project,
                String version,
                JSONObject javaObject,
                String mainClass,
                Iterable<String> classpath,
                JSONObject javaSourcesObject,
                Folder javaSourcesFolder,
                String javaSourcesVersion,
                JSONObject javaTestsObject,
                Folder javaTestsFolder,
                String javaTestsVersion,
                Double javaTestsLineCoverageRequirement,
                Folder javaOutputsFolder,
                Iterable<Dependency> dependencies)
    {
        this.rootObject = rootObject;
        this.publisher = publisher;
        this.project = project;
        this.version = version;
        this.javaObject = javaObject;
        this.mainClass = mainClass;
        this.classpath = classpath;
        this.javaSourcesObject = javaSourcesObject;
        this.javaSourcesFolder = javaSourcesFolder;
        this.javaSourcesVersion = javaSourcesVersion;
        this.javaTestsObject = javaTestsObject;
        this.javaTestsFolder = javaTestsFolder;
        this.javaTestsVersion = javaTestsVersion;
        this.javaTestsLineCoverageRequirement = javaTestsLineCoverageRequirement;
        this.javaOutputsFolder = javaOutputsFolder;
        this.dependencies = dependencies;
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

    public Iterable<String> getClasspath()
    {
        return classpath;
    }

    public Iterable<String> getAllClasspaths(Folder qubFolder)
    {
        final List<String> result = ArrayList.fromValues(classpath);
        result.addAll(dependencies.map((Dependency dependency) -> dependency.toString(qubFolder)));
        return result;
    }

    public JSONObject getJavaSourcesObject()
    {
        return javaSourcesObject;
    }

    public Folder getJavaSourcesFolder()
    {
        return javaSourcesFolder;
    }

    public Iterable<File> getJavaSourceFiles()
    {
        Iterable<File> result;
        if (javaSourcesFolder == null)
        {
            result = new Array<File>(0);
        }
        else
        {
            final Result<Iterable<File>> javaSourcesFolderFiles = javaSourcesFolder.getFilesRecursively();
            if (javaSourcesFolderFiles.getValue() == null)
            {
                result = new Array<File>(0);
            }
            else
            {
                result = javaSourcesFolderFiles.getValue().where((File file) -> file.getFileExtension().equals(".java"));
            }
        }
        return result;
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

    public Iterable<File> getJavaTestFiles()
    {
        Iterable<File> result;
        if (javaTestsFolder == null)
        {
            result = new Array<File>(0);
        }
        else
        {
            final Result<Iterable<File>> javaSourcesFolderFiles = javaTestsFolder.getFilesRecursively();
            if (javaSourcesFolderFiles.getValue() == null)
            {
                result = new Array<File>(0);
            }
            else
            {
                result = javaSourcesFolderFiles.getValue().where((File file) -> file.getFileExtension().equals(".java"));
            }
        }
        return result;
    }

    public String getJavaTestsVersion()
    {
        return javaTestsVersion;
    }

    public Double getJavaTestsLineCoverageRequirement()
    {
        return javaTestsLineCoverageRequirement;
    }

    public Folder getJavaOutputsFolder()
    {
        return javaOutputsFolder;
    }

    public Iterable<Dependency> getDependencies()
    {
        return dependencies;
    }

    public static ProjectJson parse(Console console)
    {
        ProjectJson result = null;

        if (console != null)
        {
            final File projectJsonFile = console.getCurrentFolder().getValue().getFile("project.json").getValue();
            if (!projectJsonFile.exists().getValue())
            {
                console.writeLine("project.json file doesn't exist in the current folder.");
            }
            else
            {
                JSONDocument projectJsonDocument = null;
                try (final CharacterReadStream projectJsonFileReadStream = projectJsonFile.getContentByteReadStream().getValue().asCharacterReadStream())
                {
                    projectJsonDocument = JSON.parse(projectJsonFileReadStream);
                }
                catch (Exception e)
                {
                    console.writeLine(e.toString());
                }

                JSONObject rootObject = null;
                String publisher = null;
                String project = null;
                String version = null;
                JSONObject javaObject = null;
                String mainClass = null;
                List<String> classpath = new ArrayList<>();
                JSONObject javaTestsObject = null;
                Folder javaSourcesFolder = null;
                String javaSourcesVersion = null;
                JSONObject javaSourcesObject = null;
                Folder javaTestsFolder = null;
                String javaTestsVersion = null;
                Double javaTestsLineCoverageRequirement = null;
                Folder javaOutputsFolder = null;
                List<Dependency> dependencies = new ArrayList<>();

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

                        final JSONSegment classpathSegment = javaObject.getPropertyValue("classpath");
                        if (classpathSegment != null)
                        {
                            if (classpathSegment instanceof JSONQuotedString)
                            {
                                final JSONQuotedString classpathQuotedString = (JSONQuotedString)classpathSegment;
                                final String classpathQuotedStringValue = classpathQuotedString.toUnquotedString();
                                if (!classpathQuotedStringValue.isEmpty())
                                {
                                    classpath.add(classpathQuotedString.toUnquotedString());
                                }
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
                                        final JSONQuotedString classpathQuotedString = (JSONQuotedString)classpathElementSegment;
                                        final String classpathQuotedStringValue = classpathQuotedString.toUnquotedString();
                                        if (!classpathQuotedStringValue.isEmpty())
                                        {
                                            classpath.add(classpathQuotedString.toUnquotedString());
                                        }
                                    }
                                }
                            }
                            else
                            {
                                console.writeLine("Expected \"classpath\" to be either a quoted string or an array of quoted strings.");
                            }
                        }

                        final Folder currentFolder = console.getCurrentFolder().getValue();

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
                            javaSourcesFolder = currentFolder.getFolder(sources).getValue();
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
                            javaTestsFolder = currentFolder.getFolder(tests).getValue();
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
                            javaOutputsFolder = currentFolder.getFolder(outputs).getValue();
                        }

                        final JSONSegment dependenciesSegment = javaObject.getPropertyValue("dependencies");
                        if (dependenciesSegment != null)
                        {
                            if (!(dependenciesSegment instanceof JSONArray))
                            {
                                console.writeLine("The \"dependencies\" property in the java section must be an array.");
                            }
                            else
                            {
                                final JSONArray dependenciesArray = (JSONArray)dependenciesSegment;
                                for (final JSONSegment dependencySegment : dependenciesArray.getElements())
                                {
                                    if (!(dependencySegment instanceof JSONObject))
                                    {
                                        console.writeLine("Each dependency in the \"dependencies\" array property must be an object.");
                                    }
                                    else
                                    {
                                        final JSONObject dependencyObject = (JSONObject)dependencySegment;

                                        final JSONSegment dependencyPublisherSegment = dependencyObject.getPropertyValue("publisher");
                                        if (dependencyPublisherSegment == null || !(dependencyPublisherSegment instanceof JSONQuotedString))
                                        {
                                            console.writeLine("Each dependency must have a non-empty quoted-string \"publisher\" property.");
                                        }
                                        else
                                        {
                                            final String dependencyPublisher = ((JSONQuotedString)dependencyPublisherSegment).toUnquotedString();
                                            if (dependencyPublisher.isEmpty())
                                            {
                                                console.writeLine("Each dependency must have a non-empty quoted-string \"publisher\" property.");
                                            }
                                            else
                                            {
                                                final JSONSegment dependencyProjectSegment = dependencyObject.getPropertyValue("project");
                                                if (dependencyProjectSegment == null || !(dependencyProjectSegment instanceof JSONQuotedString))
                                                {
                                                    console.writeLine("Each dependency must have a non-empty quoted-string \"project\" property.");
                                                }
                                                else
                                                {
                                                    final String dependencyProject = ((JSONQuotedString)dependencyProjectSegment).toUnquotedString();
                                                    if (dependencyProject.isEmpty())
                                                    {
                                                        console.writeLine("Each dependency must have a non-empty quoted-string \"project\" property.");
                                                    }
                                                    else
                                                    {
                                                        final JSONSegment dependencyVersionSegment = dependencyObject.getPropertyValue("version");
                                                        if (dependencyVersionSegment == null || !(dependencyVersionSegment instanceof JSONQuotedString))
                                                        {
                                                            console.writeLine("Each dependency must have a non-empty quoted-string \"version\" property.");
                                                        }
                                                        else
                                                        {
                                                            final String dependencyVersion = ((JSONQuotedString)dependencyVersionSegment).toUnquotedString();
                                                            if (dependencyVersion.isEmpty())
                                                            {
                                                                console.writeLine("Each dependency must have a non-empty quoted-string \"version\" property.");
                                                            }
                                                            else
                                                            {
                                                                dependencies.add(new Dependency(dependencyPublisher, dependencyProject, dependencyVersion));
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

                        final JSONSegment javaTestsLineCoverageRequirementSegment = javaObject.getPropertyValue("lineCoverageRequirement");
                        if (javaTestsLineCoverageRequirementSegment != null)
                        {
                            if (!(javaTestsLineCoverageRequirementSegment instanceof JSONToken))
                            {
                                console.writeLine("Expected \"lineCoverageRequirement\" property in \"java\" section to be a number between 0 to 100.");
                            }
                            else
                            {
                                final JSONToken javaTestsLineCoverageRequirementToken = (JSONToken)javaTestsLineCoverageRequirementSegment;
                                if (javaTestsLineCoverageRequirementToken.getType() != JSONTokenType.Number)
                                {
                                    console.writeLine("Expected \"lineCoverageRequirement\" property in \"java\" section to be a number between 0 to 100.");
                                }
                                else
                                {
                                    javaTestsLineCoverageRequirement = Double.parseDouble(javaTestsLineCoverageRequirementToken.toString());
                                    if (javaTestsLineCoverageRequirement < 0)
                                    {
                                        console.writeLine("Expected \"lineCoverageRequirement\" property in \"java\" section to be a number between 0 to 100.");
                                        javaTestsLineCoverageRequirement = 0.0;
                                    }
                                    else if (javaTestsLineCoverageRequirement > 100)
                                    {
                                        console.writeLine("Expected \"lineCoverageRequirement\" property in \"java\" section to be a number between 0 to 100.");
                                        javaTestsLineCoverageRequirement = 100.0;
                                    }
                                }
                            }
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
                    classpath,
                    javaSourcesObject,
                    javaSourcesFolder,
                    javaSourcesVersion,
                    javaTestsObject,
                    javaTestsFolder,
                    javaTestsVersion,
                    javaTestsLineCoverageRequirement,
                    javaOutputsFolder,
                    dependencies);
            }
        }

        return result;
    }
}
