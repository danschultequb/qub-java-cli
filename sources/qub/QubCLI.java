package qub;

public class QubCLI
{
    public static void main(String[] args)
    {
        try (final Console console = new Console(args))
        {
            QubCLI.main(console);
        }
    }

    static void main(Console console)
    {
        final Map<String,Action> actions = new ListMap<>();
        addAction(actions, new BondsAction());
        addAction(actions, new BuildAction());
        addAction(actions, new DeleteAction());
        addAction(actions, new InstallAction());
        addAction(actions, new TestAction());

        final CommandLine commandLine = console.getCommandLine();

        final CommandLineArgument actionArgument = commandLine.get(0);
        if (actionArgument == null)
        {
            showUsage(console, actions);
        }
        else
        {
            final String actionString = actionArgument.toString();
            if (actionString.equals("-?") || actionString.equals("/?"))
            {
                showUsage(console, actions);
            }
            else
            {
                final Action action = getAction(actions, actionString);
                if (action == null)
                {
                    console.writeLine("Unrecognized action: \"" + actionString + "\"");
                }
                else
                {
                    action.run(console);
                }
            }
        }
    }

    private static String getActionKey(Action action)
    {
        return action == null ? null : getActionKey(action.getName());
    }

    private static String getActionKey(String actionName)
    {
        return actionName == null ? null : actionName.toLowerCase();
    }

    private static void addAction(Map<String,Action> actions, Action toAdd)
    {
        final String actionKey = getActionKey(toAdd);
        actions.set(actionKey, toAdd);
    }

    private static Action getAction(Map<String,Action> actions, String actionName)
    {
        final String actionKey = getActionKey(actionName);
        return actions.get(actionKey);
    }

    private static void showUsage(Console console, Map<String,Action> actions)
    {
        console.writeLine("Usage: qub <action> [<action-options>]");
        console.writeLine("Possible Actions:");
        for (final Action action : actions.getValues())
        {
            console.writeLine("  " + action.getName() + ": " + action.getDescription());

            console.write("    Usage: " + action.getName());
            final String argumentUsage = action.getArgumentUsage();
            if (argumentUsage != null)
            {
                console.write(" " + argumentUsage);
            }
            console.writeLine();

            console.writeLine();
        }
    }

    static Folder getSourcesFolder(Console console, JSONObject javaSegment)
    {
        Folder sourcesFolder = null;

        final Folder currentFolder = console.getCurrentFolder();
        final JSONSegment sourcesSegment = javaSegment.getPropertyValue("sources");
        if (sourcesSegment == null)
        {
            sourcesFolder = currentFolder.getFolder("sources");
        }
        else if (sourcesSegment instanceof JSONQuotedString)
        {
            sourcesFolder = currentFolder.getFolder(((JSONQuotedString)sourcesSegment).toUnquotedString());
        }
        else if (sourcesSegment instanceof JSONObject)
        {
            final JSONSegment folderSegment = ((JSONObject)sourcesSegment).getPropertyValue("folder");
            if (folderSegment == null)
            {
                sourcesFolder = currentFolder.getFolder("sources");
            }
            else if (folderSegment instanceof JSONQuotedString)
            {
                sourcesFolder = currentFolder.getFolder(((JSONQuotedString)folderSegment).toUnquotedString());
            }
            else
            {
                console.writeLine("Expected \"folder\" property in the \"sources\" section to be a quoted-string property.");
            }
        }
        else
        {
            console.writeLine("Expected \"sources\" to not exist, be a quoted string property, or be an object property.");
        }

        return sourcesFolder;
    }

    static String getSourcesJavaVersion(Console console, JSONObject javaObject)
    {
        String javaVersion = null;

        final JSONSegment sourcesSegment = javaObject.getPropertyValue("sources");
        if (sourcesSegment != null && sourcesSegment instanceof JSONObject)
        {
            final JSONSegment versionSegment = ((JSONObject)sourcesSegment).getPropertyValue("version");
            if (versionSegment != null)
            {
                if (!(versionSegment instanceof JSONQuotedString))
                {
                    console.writeLine("Expected \"version\" property in \"sources\" section to be a quoted-string property.");
                }
                else
                {
                    javaVersion = ((JSONQuotedString)versionSegment).toUnquotedString();
                }
            }
        }

        return javaVersion;
    }

    static String getTestsJavaVersion(Console console, JSONObject javaObject)
    {
        String javaVersion = null;

        final JSONSegment testsSegment = javaObject.getPropertyValue("tests");
        if (testsSegment != null && testsSegment instanceof JSONObject)
        {
            final JSONSegment versionSegment = ((JSONObject)testsSegment).getPropertyValue("version");
            if (versionSegment != null)
            {
                if (!(versionSegment instanceof JSONQuotedString))
                {
                    console.writeLine("Expected \"version\" property in \"tests\" section to be a quoted-string property.");
                }
                else
                {
                    javaVersion = ((JSONQuotedString)versionSegment).toUnquotedString();
                }
            }
        }

        return javaVersion;
    }

    static Iterable<File> getTestFiles(Console console, Folder testsFolder)
    {
        Iterable<File> result = null;

        if (testsFolder != null)
        {
            final Iterable<File> testsFolderFiles = testsFolder.getFilesRecursively();
            if (testsFolderFiles != null && testsFolderFiles.any())
            {
                result = testsFolderFiles.where(new Function1<File, Boolean>()
                {
                    @Override
                    public Boolean run(File file)
                    {
                        return file.getFileExtension().equals(".java");
                    }
                });
            }
        }

        if (result == null || !result.any())
        {
            console.writeLine("No test files found to compile.");
        }

        return result;
    }

    static Folder getTestsFolder(Console console, JSONObject javaSegment)
    {
        Folder testsFolder = null;

        final Folder currentFolder = console.getCurrentFolder();
        final JSONSegment testsSegment = javaSegment.getPropertyValue("tests");
        if (testsSegment == null)
        {
            testsFolder = currentFolder.getFolder("tests");
        }
        else if (testsSegment instanceof JSONQuotedString)
        {
            testsFolder = currentFolder.getFolder(((JSONQuotedString)testsSegment).toUnquotedString());
        }
        else if (testsSegment instanceof JSONObject)
        {
            final JSONSegment folderSegment = ((JSONObject)testsSegment).getPropertyValue("folder");
            if (folderSegment == null)
            {
                testsFolder = currentFolder.getFolder("tests");
            }
            else if (folderSegment instanceof JSONQuotedString)
            {
                testsFolder = currentFolder.getFolder(((JSONQuotedString)folderSegment).toUnquotedString());
            }
            else
            {
                console.writeLine("Expected \"folder\" property in the \"tests\" section to be a quoted-string property.");
            }
        }
        else
        {
            console.writeLine("Expected \"tests\" to not exist, or to be a quoted string property.");
        }

        return testsFolder;
    }

    static Iterable<File> getSourceFiles(Console console, Folder sourcesFolder)
    {
        Iterable<File> result = null;

        if (sourcesFolder != null)
        {
            final Iterable<File> sourcesFolderFiles = sourcesFolder.getFilesRecursively();
            if (sourcesFolderFiles != null && sourcesFolderFiles.any())
            {
                result = sourcesFolderFiles.where(new Function1<File, Boolean>()
                {
                    @Override
                    public Boolean run(File file)
                    {
                        return file.getFileExtension().equals(".java");
                    }
                });
            }
        }

        if (result == null || !result.any())
        {
            console.writeLine("No source files found to compile.");
        }

        return result;
    }

    static List<String> getClasspaths(Console console, JSONObject javaObject)
    {
        final List<String> classpaths = new ArrayList<>();
        final JSONSegment classpathSegment = javaObject.getPropertyValue("classpath");
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

        final Folder qubFolder = getQubFolder(console);
        final Iterable<Dependency> dependencies = QubCLI.getDependencies(console, javaObject);
        for (final Dependency dependency : dependencies)
        {
            final String publisher = dependency.getPublisher();
            final String project = dependency.getProject();
            final String version = dependency.getVersion();
            classpaths.add(qubFolder.getPath()
                .concatenateSegment(publisher)
                .concatenateSegment(project)
                .concatenateSegment(version)
                .concatenateSegment(project + ".jar")
                .toString());
        }

        return classpaths;
    }

    static String getProject(Console console, JSONObject projectJsonRoot)
    {
        String project = null;

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
            project = ((JSONQuotedString)projectSegment).toUnquotedString();
        }

        return project;
    }

    static Folder getQubFolder(Console console)
    {
        return console.getFileSystem().getFolder("C:/qub");
    }

    static Folder getOutputsFolder(Console console, JSONObject javaSegment)
    {
        Folder outputsFolder = null;

        final Folder currentFolder = console.getCurrentFolder();
        final JSONSegment outputsSegment = javaSegment.getPropertyValue("outputs");
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

        return outputsFolder;
    }

    static JSONObject getJavaSegment(Console console, JSONObject projectJsonRoot)
    {
        JSONObject result = null;

        final JSONSegment javaSegment = projectJsonRoot.getPropertyValue("java");
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
            result = (JSONObject)javaSegment;
        }

        return result;
    }

    static String getMainClass(Console console, JSONObject javaObject)
    {
        String mainClass = null;

        final JSONSegment mainClassSegment = javaObject.getPropertyValue("mainClass");
        if (mainClassSegment != null)
        {
            if (mainClassSegment instanceof JSONQuotedString)
            {
                mainClass = ((JSONQuotedString)mainClassSegment).toUnquotedString();
            }
            else
            {
                console.writeLine("The \"mainClass\" property in the java object of the project.json file must be a quoted-string.");
            }
        }

        return mainClass;
    }

    static Iterable<Dependency> getDependencies(Console console, JSONObject javaObject)
    {
        List<Dependency> dependencies = new ArrayList<>();

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

                        final JSONSegment publisherSegment = dependencyObject.getPropertyValue("publisher");
                        if (publisherSegment == null || !(publisherSegment instanceof JSONQuotedString))
                        {
                            console.writeLine("Each dependency must have a \"publisher\" quoted-string property.");
                        }
                        else
                        {
                            final String publisher = ((JSONQuotedString)publisherSegment).toUnquotedString();

                            final JSONSegment projectSegment = dependencyObject.getPropertyValue("project");
                            if (projectSegment == null || !(projectSegment instanceof JSONQuotedString))
                            {
                                console.writeLine("Each dependency must have a \"project\" quoted-string property.");
                            }
                            else
                            {
                                final String project = ((JSONQuotedString)projectSegment).toUnquotedString();

                                final JSONSegment versionSegment = dependencyObject.getPropertyValue("version");
                                if (versionSegment == null || !(versionSegment instanceof JSONQuotedString))
                                {
                                    console.writeLine("Each dependency must have a \"version\" quoted-string property.");
                                }
                                else
                                {
                                    final String version = ((JSONQuotedString)versionSegment).toUnquotedString();

                                    dependencies.add(new Dependency(publisher, project, version));
                                }
                            }
                        }
                    }
                }
            }
        }

        return dependencies;
    }

    static JSONObject readProjectJson(Console console)
    {
        JSONObject projectJsonObject = null;

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

            final JSONSegment rootSegment = projectJsonDocument.getRoot();
            if (!(rootSegment instanceof JSONObject))
            {
                console.writeLine("project.json root segment must be a JSON object.");
            }
            else
            {
                projectJsonObject = (JSONObject)rootSegment;
            }
        }

        return projectJsonObject;
    }

    static boolean parseDebug(CommandLine commandLine)
    {
        final CommandLineArgument debugArgument = commandLine.remove("debug");
        return debugArgument != null && (debugArgument.getValue() == null || debugArgument.getValue().equalsIgnoreCase("true"));
    }
}