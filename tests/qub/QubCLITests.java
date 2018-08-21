package qub;

public class QubCLITests
{
    private static final String expectedUsageString =
        "Usage: qub <action> [<action-options>]\n" +
        "Possible Actions:\n" +
        "  Bonds: Calculates how much money should be allocated to different time lengths of bond/fixed-income invementments given an initial amount of money.\n" +
        "    Usage: Bonds [-strategy=<cascade,double-cascade>] <amount-to-invest>\n" +
        "\n" +
        "  Build: Build the coding project in the current folder.\n" +
        "    Usage: Build\n" +
        "\n" +
        "  Clean: Clean the coding project in the current folder.\n" +
        "    Usage: Clean\n" +
        "\n" +
        "  Delete: Delete a provided file or folder.\n" +
        "    Usage: Delete [-file] [-folder] <file-folder-or-filter-to-delete> [<additional-file-folder-or-filter-to-delete> ...]\n" +
        "\n" +
        "  GuessMyNumber: Play a guess my number game.\n" +
        "    Usage: GuessMyNumber\n" +
        "\n" +
        "  Install: Install the coding project in the current folder into the system's Qub.\n" +
        "    Usage: Install\n" +
        "\n" +
        "  Kitchen: An application that manages recipes.\n" +
        "    Usage: Kitchen\n" +
        "\n" +
        "  OrcBattle: Battle monsters to the death.\n" +
        "    Usage: OrcBattle\n" +
        "\n" +
        "  Test: Run the tests for the coding project in the current directory.\n" +
        "    Usage: Test\n" +
        "\n" +
        "  TextAdventure: A text adventure where the life and death of puppies is on the line.\n" +
        "    Usage: TextAdventure\n" +
        "\n";

    private static Console createConsole(String[] commandLineArguments)
    {
        final Console result = new Console(commandLineArguments);
        result.setLineSeparator("\n");
        return result;
    }
    
    public static void test(final TestRunner runner)
    {
        runner.testGroup("QubCLI", () ->
        {
            runner.testGroup("main(Console)", () ->
            {
                runner.test("with " + Strings.escapeAndQuote(""), (Test test) ->
                {
                    final Console console = createConsole(new String[0]);
                    final InMemoryCharacterStream output = new InMemoryCharacterStream();
                    console.setOutput(output);

                    QubCLI.main(console);

                    test.assertSuccess(expectedUsageString, output.getText());
                });

                runner.test("with " + Strings.escapeAndQuote("-?"), (Test test) ->
                {
                    final Console console = createConsole(new String[] { "-?" });
                    final InMemoryCharacterStream output = new InMemoryCharacterStream();
                    console.setOutput(output);

                    QubCLI.main(console);

                    test.assertSuccess(expectedUsageString, output.getText());
                });

                runner.test("with " + Strings.escapeAndQuote("/?"), (Test test) ->
                {
                    final Console console = createConsole(new String[] { "/?" });
                    final InMemoryCharacterStream output = new InMemoryCharacterStream();
                    console.setOutput(output);

                    QubCLI.main(console);

                    test.assertSuccess(expectedUsageString, output.getText());
                });

                runner.test("with " + Strings.escapeAndQuote("spam"), (Test test) ->
                {
                    final Console console = createConsole(new String[] { "spam" });
                    final InMemoryCharacterStream output = new InMemoryCharacterStream();
                    console.setOutput(output);

                    QubCLI.main(console);

                    test.assertSuccess("Unrecognized action: \"spam\"\n", output.getText());
                });
            });
        });
    }
}
