package qub;

/**
 * An action for the QubCLI.
 */
public interface Action
{
    /**
     * Get the name of this Action.
     * @return The name of this Action.
     */
    String getName();

    /**
     * Get the description of this Action.
     * @return The description of this Action.
     */
    String getDescription();

    /**
     * Get the argument usage of this Action.
     * @return The argument usage of this Action.
     */
    String getArgumentUsage();

    /**
     * Run this action on the provided Console.
     * @param console The Console on which to run this action.
     */
    void run(Console console);
}
