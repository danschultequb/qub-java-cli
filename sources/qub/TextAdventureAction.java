package qub;

public class TextAdventureAction implements Action
{
    @Override
    public String getName()
    {
        return "TextAdventure";
    }

    @Override
    public String getDescription()
    {
        return "A text adventure where the life and death of puppies is on the line.";
    }

    @Override
    public String getArgumentUsage()
    {
        return null;
    }

    @Override
    public void run(Console console)
    {
        console.writeLine("Welcome to Qub Text Adventure!");
        console.writeLine();

        final Place attic = new Place("Attic", "It's pretty dusty up here. The wizard should clean more.");
        final Place livingRoom = new Place("Living Room", "The wizard is snoring pretty loudly. I don't know what it would take to wake him up.");
        final Place garden = new Place("Garden", "Oh my! The nearby forest is on fire!");

        attic.addMoveOption(new MoveOption("LADDER", "downstairs", livingRoom));
        livingRoom.addMoveOption(new MoveOption("LADDER", "upstairs", attic));
        livingRoom.addMoveOption(new MoveOption("DOOR", "outside", garden));
        garden.addMoveOption(new MoveOption("DOOR", "inside", livingRoom));

        final Thing whisky = new Thing("WHISKY", garden, "The fire bursts into a huge explosion which shakes the entire house. This, of\ncourse, knocks the wizard off of the couch. As he gathers his senses, his nose\nbegins sniffing the air. Upon smelling the smoke from the fire, the wizard runs\noutside to see the roaring inferno. Immediately he pulls out his magic wand and\nconjours a water demon to put the fire out. If it wasn't for your quick\nthinking, the house, the wizard, the nearby village, and all of their newly\nborn puppies would have perished in the catastrophe! You're so brave and valiant.", true);

        attic.addThing(whisky);

        final int linesBetweenActions = 1;
        Place currentPlace = livingRoom;
        Thing currentThing = null;
        boolean done = false;

        while (!done)
        {
            // Print place state
            console.writeLine("You are in the " + currentPlace.getName() + ".");
            console.writeLine(currentPlace.getDescription());
            console.writeLine();

            final Iterable<Thing> currentPlaceThings = currentPlace.getThings();
            if (currentPlaceThings.any())
            {
                for (final Thing thing : currentPlaceThings)
                {
                    console.writeLine("You see a " + thing.getName() + ".");
                }
                console.writeLine();
            }

            boolean validInput = false;
            while (!validInput)
            {
                for (final MoveOption moveOption : currentPlace.getMoveOptions())
                {
                    console.writeLine("There is a " + moveOption.getName() + " that goes " + moveOption.getDirection() + ".");
                }
                console.writeLine();

                if (currentThing != null)
                {
                    console.writeLine("You are holding " + currentThing.getName() + ".");
                    console.writeLine();
                }

                String input;
                do
                {
                    input = console.readLine();
                }
                while (input.isEmpty());

                console.writeLine();

                if (currentThing != null && currentThing.getName().equalsIgnoreCase(input))
                {
                    validInput = true;

                    if (currentThing.getTargetPlace() != currentPlace)
                    {
                        console.writeLine("Nothing happens.");
                    }
                    else
                    {
                        console.writeLine(currentThing.getResult());
                        done = currentThing.getFinishGame();
                    }
                }

                if (!validInput)
                {
                    for (final MoveOption moveOption : currentPlace.getMoveOptions())
                    {
                        if (moveOption.getName().equalsIgnoreCase(input))
                        {
                            currentPlace = moveOption.getTargetPlace();
                            validInput = true;
                            break;
                        }
                    }
                }

                if (!validInput)
                {
                    for (final Thing thing : currentPlaceThings)
                    {
                        if (thing.getName().equalsIgnoreCase(input))
                        {
                            validInput = true;
                            if (currentThing == null)
                            {
                                currentThing = thing;
                                currentPlace.takeThing(thing.getName());
                            }
                            else
                            {
                                console.writeLine("Can't pick up " + thing.getName() + " because you're already holding " + currentThing.getName() + ".");
                            }
                            break;
                        }
                    }
                }

                if (!validInput)
                {
                    if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit"))
                    {
                        validInput = true;
                        done = true;
                    }
                    else
                    {
                        console.writeLine("Sorry, I didn't understand that.");
                    }
                }

                for (int i = 0; i < linesBetweenActions; ++i)
                {
                    console.writeLine();
                }
            }
        }

        console.writeLine("Thanks for playing!");
        console.writeLine();
        console.writeLine();
    }

    class MoveOption
    {
        private final String name;
        private final String direction;
        private final Place targetPlace;

        public MoveOption(String name, String direction, Place targetPlace)
        {
            this.name = name;
            this.direction = direction;
            this.targetPlace = targetPlace;
        }

        public String getName()
        {
            return name;
        }

        public String getDirection()
        {
            return direction;
        }

        public Place getTargetPlace()
        {
            return targetPlace;
        }
    }

    class Place
    {
        private final String name;
        private final String description;
        private final ArrayList<Thing> things;
        private final ArrayList<MoveOption> moveOptions;

        public Place(String name, String description)
        {
            this.name = name;
            this.description = description;
            things = new ArrayList<>();
            moveOptions = new ArrayList<>();
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public Iterable<Thing> getThings()
        {
            return things;
        }

        public void addThing(Thing toAdd)
        {
            things.add(toAdd);
        }

        /**
         * Take/remove and return the thing in this Place with the provided (case-insensitive) name. If
         * none of the things in this Place have the provided name, then null will be returned.
         * @param thingName The name of the thing to take.
         * @return The thing with the provided name, or null if no thing in this Place had the provided
         * name.
         */
        public Thing takeThing(String thingName)
        {
            Thing result = null;

            for (int i = 0; i < things.getCount(); ++i)
            {
                final Thing thing = things.get(i);
                if (thing.getName().equalsIgnoreCase(thingName))
                {
                    result = thing;
                    things.removeAt(i);
                    break;
                }
            }

            return result;
        }

        public Iterable<MoveOption> getMoveOptions()
        {
            return moveOptions;
        }

        public void addMoveOption(MoveOption toAdd)
        {
            moveOptions.add(toAdd);
        }
    }

    class Thing
    {
        private final String name;
        private final Place targetPlace;
        private final String result;
        private final boolean finishGame;

        public Thing(String name, Place targetPlace, String result, boolean finishGame)
        {
            this.name = name;
            this.targetPlace = targetPlace;
            this.result = result;
            this.finishGame = finishGame;
        }

        public String getName()
        {
            return name;
        }

        public Place getTargetPlace()
        {
            return targetPlace;
        }

        public String getResult()
        {
            return result;
        }

        public boolean getFinishGame()
        {
            return finishGame;
        }
    }
}
