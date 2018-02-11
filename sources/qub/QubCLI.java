package qub;

public class QubCLI
{
    public static void main(String[] args)
    {
        QubCLI.main(new Console(args));
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
        else
        {
            console.writeLine("Expected \"sources\" to not exist, or to be a quoted string property.");
        }

        return sourcesFolder;
    }

    static Iterable<File> getTestFiles(Console console, Folder testsFolder)
    {
        Iterable<File> result = null;

        if (testsFolder != null)
        {
            final Iterable<File> testsFolderFiles = testsFolder.getFilesRecursively();
            if (testsFolderFiles != null && testsFolderFiles.any())
            {
                result = testsFolderFiles.where((File file) -> file.getFileExtension().equals(".java"));
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
                result = sourcesFolderFiles.where((File file) -> file.getFileExtension().equals(".java"));
            }
        }

        if (result == null || !result.any())
        {
            console.writeLine("No source files found to compile.");
        }

        return result;
    }

    static List<String> getClasspaths(Console console, JSONObject javaSegment)
    {
        final List<String> classpaths = new ArrayList<>();
        final JSONSegment classpathSegment = javaSegment.getPropertyValue("classpath");
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
        return classpaths;
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
}