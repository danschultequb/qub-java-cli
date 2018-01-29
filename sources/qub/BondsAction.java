package qub;

/**
 * A Qub CLI action that calculates how much money should be allocated to different time lengths
 * of bond/fixed-income invementments given an initial amount of money.
 */
public class BondsAction implements Action
{
    @Override
    public String getName()
    {
        return "Bonds";
    }

    @Override
    public String getDescription()
    {
        return "Calculates how much money should be allocated to different time lengths of bond/fixed-income invementments given an initial amount of money.";
    }

    @Override
    public String getArgumentUsage()
    {
        return "[-strategy=<cascade,double-cascade>] <amount-to-invest>";
    }

    @Override
    public void run(Console console)
    {
        final CommandLine commandLine = console.getCommandLine();

        final CommandLineArgument amountToInvestArgument = commandLine
            .skip(1)
            .first((CommandLineArgument argument) -> argument.getName() == null);

        if (amountToInvestArgument == null)
        {
            console.writeLine("Please provide an amount to invest.");
        }
        else
        {
            String amountToInvestString = amountToInvestArgument.toString();
            if (amountToInvestString.startsWith("$"))
            {
                amountToInvestString = amountToInvestString.substring(1);
            }

            Double amountToInvest = null;
            try
            {
                amountToInvest = Double.parseDouble(amountToInvestString);
            }
            catch (NumberFormatException e)
            {
                console.writeLine("Please provide a dollar amount to invest.");
            }

            if (amountToInvest != null)
            {
                final String[] allocationDurations = new String[]
                {
                    "4-week",
                    "13-week",
                    "26-week",
                    "1-year",
                    "2-year",
                    "3-year",
                    "5-year",
                    "7-year",
                    "10-year",
                    "30-year"
                };

                int[] allocationAmounts = null;

                final CommandLineArgument strategy = commandLine.get("strategy");
                if (strategy != null)
                {
                    if (strategy.getValue().equalsIgnoreCase("double-cascade"))
                    {
                        allocationAmounts = doubleCascadeStrategy(amountToInvest, allocationDurations);
                    }
                    else if (strategy.getValue().equalsIgnoreCase("cascade"))
                    {
                        allocationAmounts = cascadeStrategy(amountToInvest, allocationDurations);
                    }
                }

                if (allocationAmounts == null)
                {
                    allocationAmounts = cascadeStrategy(amountToInvest, allocationDurations);
                }

                int maximumDurationStringLength = 0;
                for (final String allocationDuration : allocationDurations)
                {
                    if (allocationDuration.length() > maximumDurationStringLength)
                    {
                        maximumDurationStringLength = allocationDuration.length();
                    }
                }

                for (int i = 0; i < allocationAmounts.length; ++i)
                {
                    if (allocationAmounts[i] == 0)
                    {
                        break;
                    }
                    else
                    {
                        console.write("  " + allocationDurations[i] + ':');
                        for (int j = allocationDurations[i].length(); j <= maximumDurationStringLength; ++j)
                        {
                            console.write(' ');
                        }
                        console.writeLine("$" + allocationAmounts[i]);
                    }
                }
            }
        }
    }

    private static int[] cascadeStrategy(double amountToInvest, String[] allocationDurations)
    {
        final int[] allocationAmounts = new int[allocationDurations.length];
        while (amountToInvest >= 100)
        {
            boolean allocated = false;
            for (int i = 0; i < allocationAmounts.length - 1; ++i)
            {
                if (allocationAmounts[i] >= allocationAmounts[i + 1] + 200)
                {
                    allocationAmounts[i + 1] += 100;
                    allocated = true;
                    break;
                }
            }
            if (!allocated)
            {
                allocationAmounts[0] += 100;
            }
            amountToInvest -= 100;
        }
        return allocationAmounts;
    }

    private static int[] doubleCascadeStrategy(double amountToInvest, String[] allocationDurations)
    {
        final int[] allocationAmounts = new int[allocationDurations.length];
        while (amountToInvest >= 100)
        {
            boolean allocated = false;
            for (int i = 0; i < allocationAmounts.length - 1; ++i)
            {
                if (allocationAmounts[i] >= (allocationAmounts[i + 1] + 100) * 2)
                {
                    allocationAmounts[i + 1] += 100;
                    allocated = true;
                    break;
                }
            }
            if (!allocated)
            {
                allocationAmounts[0] += 100;
            }
            amountToInvest -= 100;
        }
        return allocationAmounts;
    }
}
