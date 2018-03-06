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
        addAction(actions, new CleanAction());
        addAction(actions, new DeleteAction());
        addAction(actions, new InstallAction());
        addAction(actions, new TestAction());
        addAction(actions, new TextAdventureAction());

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

    static Folder getQubFolder(Console console)
    {
        return console.getFileSystem().getFolder("C:/qub");
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