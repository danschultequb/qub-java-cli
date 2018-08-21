package qub;

public class TextAdventureActionTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(TextAdventureAction.class, () ->
        {
            runner.test("getName()", (Test test) ->
            {
                final TextAdventureAction action = new TextAdventureAction();
                test.assertEqual("TextAdventure", action.getName());
            });

            runner.test("getDescription()", (Test test) ->
            {
                final TextAdventureAction action = new TextAdventureAction();
                test.assertEqual("A text adventure where the life and death of puppies is on the line.", action.getDescription());
            });

            runner.test("getArgumentUsage()", (Test test) ->
            {
                final TextAdventureAction action = new TextAdventureAction();
                test.assertNull(action.getArgumentUsage());
            });

            runner.testGroup("run(Console)", () ->
            {
                final Action2<String,String> runTest = (String inputText, String expectedOutput) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(inputText), (Test test) ->
                    {
                        final Console console = new Console();
                        console.setLineSeparator("\n");
                        console.setInput(new InMemoryLineStream(inputText).endOfStream());
                        final InMemoryLineStream output = new InMemoryLineStream();
                        console.setOutput(output);

                        final TextAdventureAction action = new TextAdventureAction();
                        action.run(console);

                        output.endOfStream();
                        test.assertSuccess(expectedOutput, output.getText());
                    });
                };

                runTest.run("quit",
                    "Welcome to Qub Text Adventure!\n" +
                    "\n" +
                    "You are in the Living Room.\n" +
                    "The wizard is snoring pretty loudly. I don't know what it would take to wake him up.\n" +
                    "\n" +
                    "There is a LADDER that goes upstairs.\n" +
                    "There is a DOOR that goes outside.\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "Thanks for playing!\n" +
                    "\n" +
                    "\n");

                runTest.run("QUIT",
                    "Welcome to Qub Text Adventure!\n" +
                        "\n" +
                        "You are in the Living Room.\n" +
                        "The wizard is snoring pretty loudly. I don't know what it would take to wake him up.\n" +
                        "\n" +
                        "There is a LADDER that goes upstairs.\n" +
                        "There is a DOOR that goes outside.\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "Thanks for playing!\n" +
                        "\n" +
                        "\n");

                runTest.run("exit",
                    "Welcome to Qub Text Adventure!\n" +
                        "\n" +
                        "You are in the Living Room.\n" +
                        "The wizard is snoring pretty loudly. I don't know what it would take to wake him up.\n" +
                        "\n" +
                        "There is a LADDER that goes upstairs.\n" +
                        "There is a DOOR that goes outside.\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "Thanks for playing!\n" +
                        "\n" +
                        "\n");

                runTest.run("Exit",
                    "Welcome to Qub Text Adventure!\n" +
                        "\n" +
                        "You are in the Living Room.\n" +
                        "The wizard is snoring pretty loudly. I don't know what it would take to wake him up.\n" +
                        "\n" +
                        "There is a LADDER that goes upstairs.\n" +
                        "There is a DOOR that goes outside.\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "Thanks for playing!\n" +
                        "\n" +
                        "\n");

                runTest.run("\n\n\nExit",
                    "Welcome to Qub Text Adventure!\n" +
                        "\n" +
                        "You are in the Living Room.\n" +
                        "The wizard is snoring pretty loudly. I don't know what it would take to wake him up.\n" +
                        "\n" +
                        "There is a LADDER that goes upstairs.\n" +
                        "There is a DOOR that goes outside.\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "Thanks for playing!\n" +
                        "\n" +
                        "\n");

                runTest.run("ladder\nquit",
                    "Welcome to Qub Text Adventure!\n" +
                        "\n" +
                        "You are in the Living Room.\n" +
                        "The wizard is snoring pretty loudly. I don't know what it would take to wake him up.\n" +
                        "\n" +
                        "There is a LADDER that goes upstairs.\n" +
                        "There is a DOOR that goes outside.\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "You are in the Attic.\n" +
                        "It's pretty dusty up here. The wizard should clean more.\n" +
                        "\n" +
                        "You see a WHISKY.\n" +
                        "\n" +
                        "There is a LADDER that goes downstairs.\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "Thanks for playing!\n" +
                        "\n" +
                        "\n");

                runTest.run("ladder\nwhisky\nquit",
                    "Welcome to Qub Text Adventure!\n" +
                        "\n" +
                        "You are in the Living Room.\n" +
                        "The wizard is snoring pretty loudly. I don't know what it would take to wake him up.\n" +
                        "\n" +
                        "There is a LADDER that goes upstairs.\n" +
                        "There is a DOOR that goes outside.\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "You are in the Attic.\n" +
                        "It's pretty dusty up here. The wizard should clean more.\n" +
                        "\n" +
                        "You see a WHISKY.\n" +
                        "\n" +
                        "There is a LADDER that goes downstairs.\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "You are in the Attic.\n" +
                        "It's pretty dusty up here. The wizard should clean more.\n" +
                        "\n" +
                        "There is a LADDER that goes downstairs.\n" +
                        "\n" +
                        "You are holding WHISKY.\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "Thanks for playing!\n" +
                        "\n" +
                        "\n");
            });
        });
    }
}
