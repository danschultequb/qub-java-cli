package qub;

import static qub.KitchenAction.*;

public class KitchenActionTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(KitchenAction.class, () ->
        {
            runner.testGroup("run(Console)", () ->
            {
                runner.test("with \"quit\"", (Test test) ->
                {
                    final String userInput =
                        "quit\n";

                    final String expectedOutput =
                        welcomeMessage() +
                            options();

                    test(test, userInput, expectedOutput);
                });

                runner.test("with \"list recipes\", \"quit\"", (Test test) ->
                {
                    final String userInput =
                        "list recipes\n" +
                            "quit\n";

                    final String expectedOutput =
                        welcomeMessage() +
                            options() +
                            listRecipes() +
                            options();

                    test(test, userInput, expectedOutput);
                });

                runner.test("with \"2\", \"quit\"", (Test test) ->
                {
                    final String userInput =
                        "2\n" +
                            "quit\n";

                    final String expectedOutput =
                        welcomeMessage() +
                            options() +
                            listRecipes() +
                            options();

                    test(test, userInput, expectedOutput);
                });

                runner.test("with \"QUIT\"", (Test test) ->
                {
                    final String userInput =
                        "QUIT\n";

                    final String expectedOutput =
                        welcomeMessage() +
                            options();

                    test(test, userInput, expectedOutput);
                });

                runner.test("with \"Blah\", \"Quit\"", (Test test) ->
                {
                    final String userInput =
                        "Blah\n" +
                            "Quit\n";

                    final String expectedOutput =
                        welcomeMessage() +
                            options() +
                            unrecognizedSelection() +
                            options();

                    test(test, userInput, expectedOutput);
                });

                runner.test("with \"20\", \"Quit\"", (Test test) ->
                {
                    final String userInput =
                        "20\n" +
                            "Quit\n";

                    final String expectedOutput =
                        welcomeMessage() +
                            options() +
                            unrecognizedSelection() +
                            options();

                    test(test, userInput, expectedOutput);
                });

                runner.test("with \"add recipe\", \"quit\"", (Test test) ->
                {
                    final String userInput =
                        "add recipe\n" +
                            "quit\n";

                    final String expectedOutput =
                        welcomeMessage() +
                            options() +
                            enterRecipeName();

                    test(test, userInput, expectedOutput);
                });

                runner.test("with \"1\", \"quit\"", (Test test) ->
                {
                    final String userInput =
                        "1\n" +
                            "quit\n";

                    final String expectedOutput =
                        welcomeMessage() +
                            options() +
                            enterRecipeName();

                    test(test, userInput, expectedOutput);
                });
            });

            runner.testGroup(Kitchen.class, () ->
            {
                runner.test("constructor()", (Test test) ->
                {
                    final Kitchen kitchen = new Kitchen(null);
                    test.assertNotNull(kitchen);
                });

                runner.testGroup("parse(String)", () ->
                {
                    runner.test("with " + Strings.escapeAndQuote(null), (Test test) ->
                    {
                        final Kitchen kitchen = Kitchen.parse((String)null);
                        test.assertNotNull(kitchen);
                        test.assertNotNull(kitchen.getRecipes());
                        test.assertEqual(0, kitchen.getRecipes().getCount());
                    });

                    runner.test("with " + Strings.escapeAndQuote(""), (Test test) ->
                    {
                        final Kitchen kitchen = Kitchen.parse("");
                        test.assertNotNull(kitchen);
                        test.assertNotNull(kitchen.getRecipes());
                        test.assertEqual(0, kitchen.getRecipes().getCount());
                    });

                    runner.test("with " + Strings.escapeAndQuote("One,Two"), (Test test) ->
                    {
                        final Kitchen kitchen = Kitchen.parse("One,Two");
                        test.assertNotNull(kitchen);
                        test.assertNotNull(kitchen.getRecipes());
                        test.assertEqual(0, kitchen.getRecipes().getCount());
                    });

                    runner.test("with " + Strings.escapeAndQuote("{}"), (Test test) ->
                    {
                        final Kitchen kitchen = Kitchen.parse("{}");
                        test.assertNotNull(kitchen);
                        test.assertNotNull(kitchen.getRecipes());
                        test.assertEqual(0, kitchen.getRecipes().getCount());
                    });

                    runner.test("with " + Strings.escapeAndQuote("{\"recipes\":[{\"name\":\"Cereal\"}]}"), (Test test) ->
                    {
                        final Kitchen kitchen = Kitchen.parse("{\"recipes\":[{\"name\":\"Cereal\"}]}");
                        test.assertNotNull(kitchen);
                        test.assertNotNull(kitchen.getRecipes());
                        test.assertEqual(1, kitchen.getRecipes().getCount());
                    });
                });
            });
        });
    }

    private static String welcomeMessage()
    {
        return welcomeMessage("\n");
    }

    private static String welcomeMessage(String lineSeparator)
    {
        return "Welcome to Qub Kitchen!" + lineSeparator;
    }

    private static String options()
    {
        return options("\n");
    }

    private static String options(String lineSeparator)
    {
        return lineSeparator +
            "Please pick an action:" + lineSeparator +
            "1) Add recipe" + lineSeparator +
            "2) List recipes" + lineSeparator +
            "3) Read recipe" + lineSeparator +
            "4) Edit recipe" + lineSeparator +
            "5) Remove recipe" + lineSeparator +
            "6) Quit" + lineSeparator +
            lineSeparator;
    }

    private static String listRecipes()
    {
        return listRecipes(new String[0]);
    }

    private static String listRecipes(String[] expectedRecipes)
    {
        final StringBuilder result = new StringBuilder(
            "\n" +
                "Recipes:\n");

        for (int i = 0; i < expectedRecipes.length; ++i)
        {
            result.append(i + 1).append(") ").append(expectedRecipes[i]).append("\n");
        }

        return result.toString();
    }

    private static String enterRecipeName()
    {
        return "\n" +
            "Enter recipe name: \n" +
            "\n";
    }

    private static String enterNewIngredientName()
    {
        return "Enter ingredient name or \"done\": \n";
    }

    private static String addedRecipe()
    {
        return "Added recipe.\n";
    }

    private static String unrecognizedSelection()
    {
        return "Sorry, I didn't recognize your selection. Please try again.\n";
    }

    private static FileSystem createFileSystem()
    {
        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        return fileSystem;
    }

    private static InMemoryLineWriteStream createLineWriteStream()
    {
        return new InMemoryLineWriteStream(CharacterEncoding.UTF_8, "\n");
    }

    private static Console createConsole(String[] commandLine, InMemoryLineWriteStream stdout, String stdinText, FileSystem fileSystem)
    {
        final Console console = new Console(commandLine);
        console.setLineSeparator("\n");
        console.setOutput(stdout);

        final InMemoryLineReadStream stdin = new InMemoryLineReadStream(stdinText);
        console.setInput(stdin);

        console.setFileSystem(fileSystem);

        console.setCurrentFolderPathString("/");

        return console;
    }

    private static void test(Test test, String userInput, String expectedOutput)
    {
        final FileSystem fileSystem = createFileSystem();
        test(test, userInput, expectedOutput, fileSystem);
    }

    private static void test(Test test, String userInput, String expectedOutput, FileSystem fileSystem)
    {
        test(test, userInput, expectedOutput, fileSystem, new Value<>());
    }

    private static void test(Test test, String userInput, String expectedOutput, Out<Console> outputConsole)
    {
        final FileSystem fileSystem = createFileSystem();
        test(test, userInput, expectedOutput, fileSystem, outputConsole);
    }

    private static void test(Test test, String userInput, String expectedOutput, FileSystem fileSystem, Out<Console> outputConsole)
    {
        final InMemoryLineWriteStream stdout = createLineWriteStream();
        final Console console = createConsole(new String[0], stdout, userInput, fileSystem);

        outputConsole.set(console);

        final KitchenAction action = new KitchenAction();
        action.run(console);

        test.assertEqual(expectedOutput, stdout.getText());
    }
}
