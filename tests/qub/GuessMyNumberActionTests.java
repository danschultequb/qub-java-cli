package qub;

public class GuessMyNumberActionTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(GuessMyNumberAction.class, () ->
        {
            runner.test("constructor()", (Test test) ->
            {
                new GuessMyNumberAction();
            });

            runner.testGroup("run(Console)", () ->
            {
                final Action3<String,String[],String> playTest = (String testName, String[] inputLines, String expectedTextToFind) ->
                {
                    runner.test(testName, (Test test) ->
                    {
                        final Console console = new Console();

                        final InMemoryLineWriteStream writeStream = new InMemoryLineWriteStream();
                        console.setOutput(writeStream);

                        final String inputText = String.join("\n", inputLines);
                        final InMemoryLineReadStream readStream = new InMemoryLineReadStream(inputText);
                        console.setInput(readStream);

                        console.writeLine("TEST LOG");
                        try
                        {
                            final GuessMyNumberAction action = new GuessMyNumberAction();
                            action.run(console);
                        }
                        catch(Exception ignored)
                        {
                            test.fail(ignored);
                        }

                        test.assertTrue(writeStream.getText().contains(expectedTextToFind));
                        test.assertNull(readStream.readLine());
                    });
                };

                playTest.run("with unrecognized answer",
                    new String[]
                        {
                            "blarg!",
                            "quit"
                        },
                    "Sorry, I didn't get that.");

                playTest.run("with quit first answer",
                    new String[]
                        {
                            "quit"
                        },
                    "Thanks for playing!");

                playTest.run("with correct first guess",
                    new String[]
                        {
                            "equal",
                            "no"
                        },
                    "Hot dog! I got it!");

                playTest.run("with only greater answers",
                    new String[]
                        {
                            "greater",
                            "greater",
                            "greater",
                            "greater",
                            "greater",
                            "greater",
                            "greater",
                            "no"
                        },
                    "Hey! No cheating!");

                playTest.run("with only less answers",
                    new String[]
                        {
                            "less",
                            "less",
                            "less",
                            "less",
                            "less",
                            "less",
                            "no"
                        },
                    "Hey! No cheating!");
            });

            runner.testGroup("playAgain(Console)", () ->
            {
                final Action2<String,Boolean> playAgainTest = (String line, Boolean expectedPlayAgain) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(line), (Test test) ->
                    {
                        final Console console = new Console();

                        console.setOutput((LineWriteStream)null);

                        console.setInput(new InMemoryLineReadStream(line));

                        try
                        {
                            test.assertEqual(expectedPlayAgain, GuessMyNumberAction.playAgain(console));
                        }
                        catch (Exception e)
                        {
                            test.fail(e);
                        }
                    });
                };

                playAgainTest.run("yes", true);
                playAgainTest.run("no", false);
                playAgainTest.run("blah", false);
            });
        });
    }
}
