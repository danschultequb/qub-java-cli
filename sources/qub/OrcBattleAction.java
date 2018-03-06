package qub;

import java.io.IOException;

public class OrcBattleAction implements Action
{
    @Override
    public String getName()
    {
        return "OrcBattle";
    }

    @Override
    public String getDescription()
    {
        return "Battle monsters to the death.";
    }

    @Override
    public String getArgumentUsage()
    {
        return null;
    }

    @Override
    public void run(Console console)
    {
        console.setOutput(new DelayedByteWriteStream(console.getOutputAsByteWriteStream(), 100));

        console.writeLine("Welcome to Orc Battle!");
        console.writeLine();

        final Player player = new Player();

        final Random random = console.getRandom();

        // Create monsters
        final ArrayList<Monster> monsters = createMonsters(random);

        // Game loop
        while (!player.isDead() && !monstersDead(monsters))
        {
            player.show(console.getOutputAsLineWriteStream());
            console.writeLine();

            // Player actions
            final int playerActions = (player.getAgility() / 15) + 1;
            for (int actionIndex = 0; actionIndex < playerActions; ++actionIndex)
            {
                // Show monsters
                for (int j = 0; j < monsters.getCount(); ++j)
                {
                    final Monster monster = monsters.get(j);
                    console.write(j + ": ");

                    final int monsterHealth = monster.getHealth();
                    if (monsterHealth > 0)
                    {
                        console.write("(Health=" + monsterHealth + ") ");
                        monster.show(console.getOutputAsLineWriteStream());
                    }
                    else
                    {
                        console.writeLine("**dead**");
                    }
                }
                console.writeLine();

                // Player action
                boolean performedAction = false;
                while (!performedAction)
                {
                    console.write("Attack style - [s]tab, [d]ouble swing, [r]oundhouse: ");
                    final String playerInput = console.readLine();
                    final String playerInputLower = playerInput.toLowerCase();

                    if (playerInputLower.equals("s") || playerInputLower.equals("stab"))
                    {
                        final int damage = 2 + random.getRandomIntegerBetween(0, player.getStrength() / 2);
                        final int monsterIndex = pickMonsterIndex(console, monsters);

                        final Monster monster = monsters.get(monsterIndex);
                        monster.takeDamage(damage, console.getOutputAsLineWriteStream());

                        performedAction = true;
                    }
                    else if (playerInputLower.equals("d") || playerInputLower.equals("double swing"))
                    {
                        final int damage = random.getRandomIntegerBetween(1, player.getStrength() / 6);
                        console.writeLine("Your double swing has a strength of " + damage + ".");
                        console.writeLine();

                        for (int attackNumber = 1; attackNumber <= 2 && !monstersDead(monsters); ++attackNumber)
                        {
                            console.writeLine("Swing " + attackNumber);

                            final int monsterIndex = pickMonsterIndex(console, monsters);
                            final Monster monster = monsters.get(monsterIndex);
                            monster.takeDamage(damage, console.getOutputAsLineWriteStream());

                            console.writeLine();
                        }

                        performedAction = true;
                    }
                    else if (playerInputLower.equals("r") || playerInputLower.equals("roundhouse"))
                    {
                        final int monstersHit = random.getRandomIntegerBetween(1, player.getStrength() / 3);

                        for (int monstersHitIndex = 0; monstersHitIndex < monstersHit && !monstersDead(monsters); ++monstersHitIndex)
                        {
                            int monsterIndex = random.getRandomIntegerBetween(0, monsters.getCount() - 1);
                            while (true)
                            {
                                final Monster monster = monsters.get(monsterIndex);
                                if (monster.isDead())
                                {
                                    break;
                                }
                                else
                                {
                                    monsterIndex = (monsterIndex + 1) % monsters.getCount();
                                }
                            }

                            final Monster monster = monsters.get(monsterIndex);
                            monster.takeDamage(1, console.getOutputAsLineWriteStream());
                        }

                        console.writeLine();
                        performedAction = true;
                    }
                    else
                    {
                        console.writeLine("Unrecognized action.");
                    }
                }

                console.writeLine();

                if (monstersDead(monsters))
                {
                    break;
                }
            }

            // Monster actions
            for (int monsterIndex = 0; monsterIndex < monsters.getCount(); ++monsterIndex)
            {
                final Monster monster = monsters.get(monsterIndex);
                if (!monster.isDead())
                {
                    monster.act(player, random, console.getOutputAsLineWriteStream());

                    if (player.isDead())
                    {
                        console.writeLine();
                        break;
                    }
                }
            }

            console.writeLine();
        }

        if (player.isDead())
        {
            console.writeLine("You were defeated. You lose.");
        }
        else
        {
            console.writeLine("You defeated all of the monsters! You win!");
        }

        console.writeLine();
        console.writeLine("Press [Enter] to exit...");

        console.readLine();
    }

    /**
     * Create a random list of Monsters.
     * @param random The random number generator to use to generate the list of Monsters.
     * @return The created list of Monsters.
     */
    static ArrayList<Monster> createMonsters(Random random)
    {
        final ArrayList<Monster> monsters = new ArrayList<>();

        for (int i = 0; i < 12; ++i)
        {
            final int monsterType = random.getRandomIntegerBetween(1, 4);
            Monster monsterToAdd = null;
            switch (monsterType)
            {
                case 1:
                    monsterToAdd = new Hydra(random);
                    break;

                case 2:
                    monsterToAdd = new Slime(random);
                    break;

                case 3:
                    monsterToAdd = new Brigand(random);
                    break;

                case 4:
                    monsterToAdd = new Orc(random);
                    break;
            }
            monsters.add(monsterToAdd);
        }

        return monsters;
    }

    /**
     * Get whether or not all of the Monsters in the provided Iterable are dead.
     * @param monsters The monsters to check.
     * @return Whether or not all of the Monsters in the provided Iterable are dead.
     */
    static boolean monstersDead(Iterable<Monster> monsters)
    {
        boolean result = true;

        if (monsters.any())
        {
            for (final Monster monster : monsters)
            {
                if (!monster.isDead())
                {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Pick a monster to perform an action against.
     * @param console The console to use to write output to and read input from.
     * @param monsters The monsters to pick from.
     * @return The index of the monster to perform an action against.
     */
    static int pickMonsterIndex(Console console, ArrayList<Monster> monsters)
    {
        int monsterIndex = -1;
        while (true)
        {
            console.write("Monster #: ");

            final String input = console.readLine();
            if (input != null && !input.isEmpty())
            {
                try
                {
                    monsterIndex = Integer.valueOf(input);
                }
                catch (NumberFormatException ignored)
                {
                }

                if (monsterIndex < 0 || monsters.getCount() <= monsterIndex)
                {
                    console.writeLine("Enter a number between 1 and " + monsters.getCount() + ".");
                }
                else if (monsters.get(monsterIndex).isDead())
                {
                    console.writeLine("Enter a number for a monster that isn't dead.");
                    monsterIndex = -1;
                }
                else
                {
                    break;
                }
            }
        }

        return monsterIndex;
    }

    static class Player
    {
        private int health;
        private int strength;
        private int agility;

        /**
         * Create a new Player object.
         */
        public Player()
        {
            health = 30;
            strength = 30;
            agility = 30;
        }

        public int getHealth()
        {
            return health;
        }

        /**
         * Take away the provided damage from this Player's health.
         * @param damage The damage to take away from this Player's health.
         */
        public void subtractHealth(int damage)
        {
            health -= damage;
            if (health < 0)
            {
                health = 0;
            }
        }

        /**
         * Get whether or not the player is dead.
         * @return Whether or not the player is dead.
         */
        public boolean isDead()
        {
            return health <= 0;
        }

        public int getAgility()
        {
            return agility;
        }

        /**
         * Take away the provided agility from this Player's agility.
         * @param agilityDecrease The amount to take away from this Player's agility.
         */
        public void subtractAgility(int agilityDecrease)
        {
            agility -= agilityDecrease;
            if (agility < 1)
            {
                agility = 1;
            }
        }

        public int getStrength()
        {
            return strength;
        }

        /**
         * Take away the provided strength from this Player's strength.
         * @param strengthDecrease The amount to take away from this Player's strength.
         */
        public void subtractStrength(int strengthDecrease)
        {
            strength -= strengthDecrease;
            if (strength < 1)
            {
                strength = 1;
            }
        }

        /**
         * Write the details of this Player to the provided writeStream.
         * @param writeStream The TextWriteStream to write the details of this Player to.
         */
        public void show(LineWriteStream writeStream)
        {
            writeStream.writeLine("You are a valiant knight with a health of " + health + ", an agility of " + agility + ", and a strength of " + strength + ".");
        }
    }

    static abstract class Monster
    {
        private int health;

        /**
         * Create a new Monster.
         * @param random The random number generator to use to initialize the Monster's health.
         */
        protected Monster(Random random)
        {
            health = random.getRandomIntegerBetween(1, 10);
        }

        /**
         * Get the amount of health that this Monster has left.
         * @return The amount of health that this Monster has left.
         */
        protected int getHealth()
        {
            return health;
        }

        /**
         * Add the provided health to this Monster's health.
         * @param toAdd The health to add to this Monster's health.
         */
        protected void addHealth(int toAdd)
        {
            health += toAdd;
        }

        /**
         * Take away the provided damage from this Monster's health.
         * @param damage The damage to take away from this Monster's health.
         */
        protected void subtractHealth(int damage)
        {
            health -= damage;
        }

        /**
         * Get whether or not this Monster is dead.
         * @return Whether or not this Monster is dead.
         */
        public boolean isDead()
        {
            return health <= 0;
        }

        /**
         * Write the details of this Monster to the provided writeStream.
         * @param writeStream The TextWriteStream to write the details of this Monster to.
         */
        public abstract void show(LineWriteStream writeStream);

        /**
         * Take off the provided amount of damage and write a message to the provided TextWriteStream.
         * @param damage The damage to take off of this Monster.
         * @param writeStream The TextWriteStream to write a message to.
         */
        public abstract void takeDamage(int damage, LineWriteStream writeStream);

        /**
         * Perform this Monster's action against the provided Player and write a message to the provided
         * TextWriteStream.
         * @param player The Player to act against.
         * @param random The random number generator to use to generate random values.
         * @param writeStream The TextWriteStream to write a message to.
         */
        public abstract void act(Player player, Random random, LineWriteStream writeStream);
    }

    static class Brigand extends Monster
    {
        public Brigand(Random random)
        {
            super(random);
        }

        @Override
        public void show(LineWriteStream writeStream)
        {
            writeStream.writeLine("A fierce brigand.");
        }

        @Override
        public void takeDamage(int damage, LineWriteStream writeStream)
        {
            subtractHealth(damage);

            if (isDead())
            {
                writeStream.writeLine("You killed the brigand!");
            }
            else
            {
                writeStream.writeLine("You hit the brigand, knocking off " + damage + " health points!");
            }
        }

        @Override
        public void act(Player player, Random random, LineWriteStream writeStream)
        {
            if (player.getHealth() >= player.getAgility() && player.getHealth() >= player.getStrength())
            {
                writeStream.writeLine("A brigand hits you with his slingshot, taking off 2 health points!");
                player.subtractHealth(2);
            }
            else if (player.getAgility() >= player.getStrength())
            {
                writeStream.writeLine("A brigand catches your leg with his whip, taking off 2 agility points!");
                player.subtractAgility(2);
            }
            else
            {
                writeStream.writeLine("A brigand cuts your arm with his whip, taking off 2 strength points!");
                player.subtractStrength(2);
            }
        }
    }

    static class Hydra extends Monster
    {
        /**
         * Create a new Hydra monster.
         * @param random The random number generator to use to initialize the Hydra's health.
         */
        public Hydra(Random random)
        {
            super(random);
        }

        @Override
        public void show(LineWriteStream writeStream)
        {
            writeStream.writeLine("A malicious hydra with " + getHealth() + " heads.");
        }

        @Override
        public void takeDamage(int damage, LineWriteStream writeStream)
        {
            subtractHealth(damage);
            if (getHealth() <= 0)
            {
                writeStream.writeLine("The corpse of the fully decapitated and decapacitated hydra falls to the floor!");
            }
            else
            {
                writeStream.writeLine("You lop off " + damage + " of the hydra's heads!");
            }
        }

        @Override
        public void act(Player player, Random random, LineWriteStream writeStream)
        {
            final int damage = random.getRandomIntegerBetween(0, getHealth() / 2);
            if (damage >= 1)
            {
                writeStream.writeLine("A hydra attacks you with " + damage + " of its heads! It also grows back one more head!");

                addHealth(1);
                player.subtractHealth(damage);
            }
        }
    }

    static class Orc extends Monster
    {
        private final int clubLevel;

        public Orc(Random random)
        {
            super(random);

            clubLevel = random.getRandomIntegerBetween(1, 8);
        }

        @Override
        public void show(LineWriteStream writeStream)
        {
            writeStream.writeLine("A wicked orc with a level " + clubLevel + " club.");
        }

        @Override
        public void takeDamage(int damage, LineWriteStream writeStream)
        {
            subtractHealth(damage);

            if (isDead())
            {
                writeStream.writeLine("You killed the orc!");
            }
            else
            {
                writeStream.writeLine("You hit the orc, knocking off " + damage + " health points!");
            }
        }

        @Override
        public void act(Player player, Random random, LineWriteStream writeStream)
        {
            final int damage = random.getRandomIntegerBetween(1, clubLevel);
            writeStream.writeLine("An orc swings his club at you and knocks off " + damage + " of your health points.");
            player.subtractHealth(damage);
        }
    }

    static class Slime extends Monster
    {
        private int sliminess;

        public Slime(Random random)
        {
            super(random);

            sliminess = random.getRandomIntegerBetween(1, 5);
        }

        @Override
        public void show(LineWriteStream writeStream)
        {
            writeStream.writeLine("A slime mold with a sliminess of " + sliminess + ".");
        }

        @Override
        public void takeDamage(int damage, LineWriteStream writeStream)
        {
            subtractHealth(damage);

            if (isDead())
            {
                writeStream.writeLine("You killed the slime!");
            }
            else
            {
                writeStream.writeLine("You hit the slime, knocking off " + damage + " health points.");
            }
        }

        @Override
        public void act(Player player, Random random, LineWriteStream writeStream)
        {
            final int agilityDecrease = random.getRandomIntegerBetween(0, sliminess);
            if (agilityDecrease > 0)
            {
                writeStream.write("A slime mold wraps around your legs and decreases your agility by " + agilityDecrease + "!");

                player.subtractAgility(agilityDecrease);

                if (random.getRandomBoolean())
                {
                    writeStream.write(" It also squirts in your face, taking away a health point!");
                    player.subtractHealth(1);
                }

                writeStream.writeLine();
            }
        }
    }

    static class DelayedByteWriteStream extends ByteWriteStreamBase
    {
        private final ByteWriteStream innerByteWriteStream;
        private final int millisecondDelay;

        public DelayedByteWriteStream(ByteWriteStream innerByteWriteStream, int millisecondDelay)
        {
            this.innerByteWriteStream = innerByteWriteStream;
            this.millisecondDelay = millisecondDelay;
        }

        @Override
        public void setExceptionHandler(Action1<IOException> exceptionHandler)
        {
            innerByteWriteStream.setExceptionHandler(exceptionHandler);
        }

        @Override
        public boolean write(byte b)
        {
            final boolean result = innerByteWriteStream.write(b);
            if (b == '\n')
            {
                try
                {
                    Thread.sleep(millisecondDelay);
                }
                catch (InterruptedException ignored)
                {
                }
            }
            return result;
        }

        @Override
        public boolean isOpen()
        {
            return innerByteWriteStream.isOpen();
        }

        @Override
        public void close()
        {
            innerByteWriteStream.close();
        }
    }
}
