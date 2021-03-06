package qub;

public class QubCLI
{
    public static void main(String[] args)
    {
        try (final Console console = new Console(args))
        {
            QubCLI.main(console);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void main(Console console)
    {
        final MutableMap<String,Action> actions = Map.create();
        addAction(actions, new BuildAction());
        addAction(actions, new CleanAction());
        addAction(actions, new DeleteAction());
        addAction(actions, new InstallAction());
        addAction(actions, new TestAction());

        final CommandLine commandLine = console.getCommandLine();

        if (!commandLine.any())
        {
            showUsage(console, actions);
        }
        else
        {
            final CommandLineArgument actionArgument = commandLine.get(0);
            final String actionString = actionArgument.toString();
            if (actionString.equals("-?") || actionString.equals("/?"))
            {
                showUsage(console, actions);
            }
            else
            {
                getAction(actions, actionString)
                    .then((Action action) -> action.run(console))
                    .catchError(() -> console.writeLine("Unrecognized action: " + Strings.escapeAndQuote(actionString)));
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

    private static void addAction(MutableMap<String,Action> actions, Action toAdd)
    {
        final String actionKey = getActionKey(toAdd);
        actions.set(actionKey, toAdd);
    }

    private static Result<Action> getAction(Map<String,Action> actions, String actionName)
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

    static Folder getQubFolder(Console console)
    {
        return console.getFileSystem().getFolder("C:/qub").getValue();
    }

    static Folder getJavaFolder(Console console)
    {
        return console.getFileSystem().getFolder("C:/Program Files/Java/").getValue();
    }

    static boolean parseDebug(Console console)
    {
        return parseDebug(console.getCommandLine());
    }

    static boolean parseDebug(CommandLine commandLine)
    {
        final CommandLineArgument debugArgument = commandLine.remove("debug");
        return debugArgument != null && (debugArgument.getValue() == null || debugArgument.getValue().equalsIgnoreCase("true"));
    }
}