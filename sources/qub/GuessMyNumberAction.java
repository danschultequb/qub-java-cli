package qub;

public class GuessMyNumberAction implements Action
{
    @Override
    public String getName()
    {
        return "GuessMyNumber";
    }

    @Override
    public String getDescription()
    {
        return "Play a guess my number game.";
    }

    @Override
    public String getArgumentUsage()
    {
        return null;
    }

    @Override
    public void run(Console console)
    {
        console.writeLine("Welcome to Qub Guess My Number!");
        console.writeLine();

        boolean done = false;

        final int initialLowerBound = 1;
        final int initialUpperBound = 100;

        int lowerBound = initialLowerBound;
        int upperBound = initialUpperBound;
        boolean firstGuess = true;

        while (!done)
        {
            if (firstGuess)
            {
                firstGuess = false;
                console.writeLine("Think of a number between 1 and 100...");
            }

            final int guess = (upperBound + lowerBound) / 2;
            console.write("Is it less than, equal to, or greater than " + guess + "? [less, equal, greater] ");

            final String response = console.readLine();

            if (response.equalsIgnoreCase("equal"))
            {
                console.writeLine("Hot dog! I got it!");
                console.writeLine();

                done = !playAgain(console);
                lowerBound = initialLowerBound;
                upperBound = initialUpperBound;
                firstGuess = true;
            }
            else if (response.equalsIgnoreCase("less"))
            {
                upperBound = guess - 1;
            }
            else if (response.equalsIgnoreCase("greater"))
            {
                lowerBound = guess + 1;
            }
            else if (response.equalsIgnoreCase("quit"))
            {
                done = true;
            }
            else
            {
                console.writeLine("Sorry, I didn't get that.");
            }

            if (upperBound < lowerBound)
            {
                console.writeLine("Hey! No cheating!");
                console.writeLine();

                done = !playAgain(console);
                lowerBound = initialLowerBound;
                upperBound = initialUpperBound;
                firstGuess = true;
            }
        }

        console.writeLine("Thanks for playing!");
    }

    static boolean playAgain(Console console)
    {
        console.write("Do you want to main again? [yes, no] ");

        final boolean playAgain = console.readLine().equalsIgnoreCase("yes");

        console.writeLine();

        return playAgain;
    }
}
