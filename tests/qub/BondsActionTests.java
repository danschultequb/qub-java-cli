package qub;

public class BondsActionTests
{
    private static Console createConsole(Iterable<String> commandLineArguments)
    {
        final Console result = new Console(commandLineArguments);
        result.setLineSeparator("\n");
        return result;
    }

    public static void test(final TestRunner runner)
    {
        runner.testGroup(BondsAction.class, () ->
        {
            final Action2<String[],String> bondsTest = (String[] commandLineArguments, String expectedOutput) ->
            {
                runner.test("with " + runner.escapeAndQuote(String.join(" ", commandLineArguments)), (Test test) ->
                {
                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();

                    final List<String> arguments = new ArrayList<>();
                    arguments.add("bonds");
                    arguments.addAll(commandLineArguments);
                    final Console console = createConsole(arguments);
                    console.setOutput(output);

                    final BondsAction action = new BondsAction();
                    action.run(console);

                    test.assertEqual(expectedOutput, output.getText());
                });
            };

            bondsTest.run(new String[0], "Please provide an amount to invest.\n");

            bondsTest.run(new String[] { "spam" }, "Please provide a dollar amount to invest.\n");

            bondsTest.run(new String[] { "$10000" },
                "  4-week:  $1500\n" +
                "  13-week: $1400\n" +
                "  26-week: $1300\n" +
                "  1-year:  $1200\n" +
                "  2-year:  $1100\n" +
                "  3-year:  $900\n" +
                "  5-year:  $800\n" +
                "  7-year:  $700\n" +
                "  10-year: $600\n" +
                "  30-year: $500\n");

            final Action2<String,String> cascadeTest = (String amountToInvest, String expectedOutput) ->
            {
                bondsTest.run(new String[] { "-strategy=cascade", amountToInvest }, expectedOutput);
            };
            cascadeTest.run("0",
                "");
            cascadeTest.run("99",
                "");
            cascadeTest.run("100",
                "  4-week:  $100\n");
            cascadeTest.run("$199.99",
                "  4-week:  $100\n");
            cascadeTest.run("200",
                "  4-week:  $200\n");
            cascadeTest.run("300",
                "  4-week:  $200\n" +
                "  13-week: $100\n");
            cascadeTest.run("400",
                "  4-week:  $300\n" +
                "  13-week: $100\n");
            cascadeTest.run("500",
                "  4-week:  $300\n" +
                    "  13-week: $200\n");
            cascadeTest.run("600",
                "  4-week:  $300\n" +
                    "  13-week: $200\n" +
                    "  26-week: $100\n");
            cascadeTest.run("700",
                "  4-week:  $400\n" +
                    "  13-week: $200\n" +
                    "  26-week: $100\n");
            cascadeTest.run("800",
                "  4-week:  $400\n" +
                    "  13-week: $300\n" +
                    "  26-week: $100\n");
            cascadeTest.run("900",
                "  4-week:  $400\n" +
                    "  13-week: $300\n" +
                    "  26-week: $200\n");
            cascadeTest.run("1000",
                "  4-week:  $400\n" +
                    "  13-week: $300\n" +
                    "  26-week: $200\n" +
                    "  1-year:  $100\n");
            cascadeTest.run("1100",
                "  4-week:  $500\n" +
                    "  13-week: $300\n" +
                    "  26-week: $200\n" +
                    "  1-year:  $100\n");
            cascadeTest.run("1200",
                "  4-week:  $500\n" +
                    "  13-week: $400\n" +
                    "  26-week: $200\n" +
                    "  1-year:  $100\n");
            cascadeTest.run("1300",
                "  4-week:  $500\n" +
                    "  13-week: $400\n" +
                    "  26-week: $300\n" +
                    "  1-year:  $100\n");
            cascadeTest.run("1400",
                "  4-week:  $500\n" +
                    "  13-week: $400\n" +
                    "  26-week: $300\n" +
                    "  1-year:  $200\n");
            cascadeTest.run("1500",
                "  4-week:  $500\n" +
                    "  13-week: $400\n" +
                    "  26-week: $300\n" +
                    "  1-year:  $200\n" +
                    "  2-year:  $100\n");
            cascadeTest.run("$10000",
                "  4-week:  $1500\n" +
                    "  13-week: $1400\n" +
                    "  26-week: $1300\n" +
                    "  1-year:  $1200\n" +
                    "  2-year:  $1100\n" +
                    "  3-year:  $900\n" +
                    "  5-year:  $800\n" +
                    "  7-year:  $700\n" +
                    "  10-year: $600\n" +
                    "  30-year: $500\n");

            final Action2<String,String> doubleCascadeTest = (String amountToInvest, String expectedOutput) ->
            {
                bondsTest.run(new String[] { "-strategy=double-cascade", amountToInvest }, expectedOutput);
            };
            doubleCascadeTest.run("0",
                "");
            doubleCascadeTest.run("99",
                "");
            doubleCascadeTest.run("100",
                "  4-week:  $100\n");
            doubleCascadeTest.run("$199.99",
                "  4-week:  $100\n");
            doubleCascadeTest.run("200",
                "  4-week:  $200\n");
            doubleCascadeTest.run("300",
                "  4-week:  $200\n" +
                    "  13-week: $100\n");
            doubleCascadeTest.run("400",
                "  4-week:  $300\n" +
                    "  13-week: $100\n");
            doubleCascadeTest.run("500",
                "  4-week:  $400\n" +
                    "  13-week: $100\n");
            doubleCascadeTest.run("600",
                "  4-week:  $400\n" +
                    "  13-week: $200\n");
            doubleCascadeTest.run("700",
                "  4-week:  $400\n" +
                    "  13-week: $200\n" +
                    "  26-week: $100\n");
            doubleCascadeTest.run("800",
                "  4-week:  $500\n" +
                    "  13-week: $200\n" +
                    "  26-week: $100\n");
            doubleCascadeTest.run("900",
                "  4-week:  $600\n" +
                    "  13-week: $200\n" +
                    "  26-week: $100\n");
            doubleCascadeTest.run("1000",
                "  4-week:  $600\n" +
                    "  13-week: $300\n" +
                    "  26-week: $100\n");
            doubleCascadeTest.run("1100",
                "  4-week:  $700\n" +
                    "  13-week: $300\n" +
                    "  26-week: $100\n");
            doubleCascadeTest.run("1200",
                "  4-week:  $800\n" +
                    "  13-week: $300\n" +
                    "  26-week: $100\n");
            doubleCascadeTest.run("1300",
                "  4-week:  $800\n" +
                    "  13-week: $400\n" +
                    "  26-week: $100\n");
            doubleCascadeTest.run("1400",
                "  4-week:  $800\n" +
                    "  13-week: $400\n" +
                    "  26-week: $200\n");
            doubleCascadeTest.run("1500",
                "  4-week:  $800\n" +
                    "  13-week: $400\n" +
                    "  26-week: $200\n" +
                    "  1-year:  $100\n");
            doubleCascadeTest.run("$10000",
                "  4-week:  $5200\n" +
                    "  13-week: $2600\n" +
                    "  26-week: $1200\n" +
                    "  1-year:  $600\n" +
                    "  2-year:  $300\n" +
                    "  3-year:  $100\n");
        });
    }
}
