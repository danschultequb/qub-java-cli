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
        "  Install: Install the coding project in the current folder into the system's Qub.\n" +
        "    Usage: Install\n" +
        "\n" +
        "  Test: Run the tests for the coding project in the current directory.\n" +
        "    Usage: Test\n" +
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
                runner.test("with " + runner.escapeAndQuote(""), (Test test) ->
                {
                    final Console console = createConsole(new String[0]);
                    final InMemoryCharacterWriteStream output = new InMemoryCharacterWriteStream();
                    console.setOutput(output);

                    QubCLI.main(console);

                    test.assertEqual(expectedUsageString, output.getText());
                });

                runner.test("with " + runner.escapeAndQuote("-?"), (Test test) ->
                {
                    final Console console = createConsole(new String[] { "-?" });
                    final InMemoryCharacterWriteStream output = new InMemoryCharacterWriteStream();
                    console.setOutput(output);

                    QubCLI.main(console);

                    test.assertEqual(expectedUsageString, output.getText());
                });

                runner.test("with " + runner.escapeAndQuote("/?"), (Test test) ->
                {
                    final Console console = createConsole(new String[] { "/?" });
                    final InMemoryCharacterWriteStream output = new InMemoryCharacterWriteStream();
                    console.setOutput(output);

                    QubCLI.main(console);

                    test.assertEqual(expectedUsageString, output.getText());
                });

                runner.test("with " + runner.escapeAndQuote("spam"), (Test test) ->
                {
                    final Console console = createConsole(new String[] { "spam" });
                    final InMemoryCharacterWriteStream output = new InMemoryCharacterWriteStream();
                    console.setOutput(output);

                    QubCLI.main(console);

                    test.assertEqual("Unrecognized action: \"spam\"\n", output.getText());
                });
            });
        });
    }
}
