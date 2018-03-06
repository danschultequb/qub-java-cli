package qub;

import static qub.OrcBattleAction.*;

public class OrcBattleActionTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(OrcBattleAction.class, () ->
        {
            runner.test("constructor()", (Test test) ->
            {
                new OrcBattleAction();
            });

            runner.test("createMonsters()", (Test test) ->
            {
                final FixedRandom random = new FixedRandom(5);

                final ArrayList<Monster> monsters = OrcBattleAction.createMonsters(random);
                test.assertEqual(12, monsters.getCount());
                for (int i = 0; i < monsters.getCount(); ++i)
                {
                    test.assertNotNull(monsters.get(i));
                }
            });

            runner.test("monstersDead()", (Test test) ->
            {
                final FixedRandom random = new FixedRandom(10);
                final InMemoryLineWriteStream lineWriteStream = new InMemoryLineWriteStream();

                final Array<Monster> monsters = new Array<>(2);
                monsters.set(0, new Brigand(random));
                monsters.set(1, new Orc(random));

                test.assertFalse(monsters.get(0).isDead());
                test.assertFalse(monsters.get(1).isDead());
                test.assertFalse(OrcBattleAction.monstersDead(monsters));

                monsters.get(0).takeDamage(monsters.get(0).getHealth(), lineWriteStream);

                test.assertTrue(monsters.get(0).isDead());
                test.assertFalse(monsters.get(1).isDead());
                test.assertFalse(OrcBattleAction.monstersDead(monsters));

                monsters.get(1).takeDamage(monsters.get(1).getHealth(), lineWriteStream);

                test.assertTrue(monsters.get(0).isDead());
                test.assertTrue(monsters.get(1).isDead());
                test.assertTrue(OrcBattleAction.monstersDead(monsters));
            });

            final Action1<Function1<Random,Monster>> monsterTests = (Function1<Random,Monster> createMonster) ->
            {
                runner.testGroup(Monster.class, () ->
                {
                    runner.test("addHealth()", (Test test) ->
                    {
                        final FixedRandom random = new FixedRandom(0);
                        final Monster monster = createMonster.run(random);
                        monster.addHealth(0);
                        test.assertEqual(1, monster.getHealth());

                        monster.addHealth(1);
                        test.assertEqual(2, monster.getHealth());

                        monster.addHealth(1000);
                        test.assertEqual(1002, monster.getHealth());

                        monster.addHealth(-997);
                        test.assertEqual(5, monster.getHealth());
                    });

                    runner.test("subtractHealth()", (Test test) ->
                    {
                        final FixedRandom random = new FixedRandom(0);
                        final Monster monster = createMonster.run(random);
                        monster.subtractHealth(0);
                        test.assertEqual(1, monster.getHealth());

                        monster.subtractHealth(1);
                        test.assertEqual(0, monster.getHealth());

                        monster.subtractHealth(1000);
                        test.assertEqual(-1000, monster.getHealth());

                        monster.subtractHealth(-1005);
                        test.assertEqual(5, monster.getHealth());
                    });

                    runner.test("isDead()", (Test test) ->
                    {
                        final FixedRandom random = new FixedRandom(0);
                        final Monster monster = createMonster.run(random);
                        test.assertFalse(monster.isDead());

                        monster.addHealth(5);
                        test.assertFalse(monster.isDead());

                        monster.subtractHealth(monster.getHealth() - 1);
                        test.assertFalse(monster.isDead());

                        monster.subtractHealth(1);
                        test.assertTrue(monster.isDead());

                        monster.subtractHealth(1);
                        test.assertTrue(monster.isDead());
                    });
                });
            };

            runner.testGroup(Brigand.class, () ->
            {
                monsterTests.run(Brigand::new);

                runner.test("constructor()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(0);
                    final Brigand brigand = new Brigand(random);
                    test.assertEqual(1, brigand.getHealth());
                });

                runner.test("show()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(0);
                    final Brigand brigand = new Brigand(random);

                    final InMemoryLineWriteStream textWriteStream = new InMemoryLineWriteStream(CharacterEncoding.UTF_8, "\n");
                    brigand.show(textWriteStream);

                    test.assertEqual("A fierce brigand.\n", textWriteStream.getText());
                });

                runner.test("takeDamage()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(1);
                    final Brigand brigand = new Brigand(random);
                    test.assertEqual(2, brigand.getHealth());

                    final InMemoryLineWriteStream textWriteStream = new InMemoryLineWriteStream(CharacterEncoding.UTF_8, "\n");

                    brigand.takeDamage(1, textWriteStream);
                    test.assertEqual(1, brigand.getHealth());
                    test.assertEqual(
                        "You hit the brigand, knocking off 1 health points!\n",
                        textWriteStream.getText());

                    brigand.takeDamage(1, textWriteStream);
                    test.assertEqual(0, brigand.getHealth());
                    test.assertEqual(
                        "You hit the brigand, knocking off 1 health points!\n" +
                            "You killed the brigand!\n",
                        textWriteStream.getText());

                    brigand.takeDamage(10, textWriteStream);
                    test.assertEqual(-10, brigand.getHealth());
                    test.assertEqual(
                        "You hit the brigand, knocking off 1 health points!\n" +
                            "You killed the brigand!\n" +
                            "You killed the brigand!\n",
                        textWriteStream.getText());
                });

                runner.test("act()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(1);
                    final Brigand brigand = new Brigand(random);
                    final Player player = new Player();
                    final InMemoryLineWriteStream textWriteStream = new InMemoryLineWriteStream(CharacterEncoding.UTF_8, "\n");

                    brigand.act(player, random, textWriteStream);
                    test.assertEqual(28, player.getHealth());
                    test.assertEqual(30, player.getAgility());
                    test.assertEqual(30, player.getStrength());
                    test.assertEqual(
                        "A brigand hits you with his slingshot, taking off 2 health points!\n",
                        textWriteStream.getText());

                    brigand.act(player, random, textWriteStream);
                    test.assertEqual(28, player.getHealth());
                    test.assertEqual(28, player.getAgility());
                    test.assertEqual(30, player.getStrength());
                    test.assertEqual(
                        "A brigand hits you with his slingshot, taking off 2 health points!\n" +
                            "A brigand catches your leg with his whip, taking off 2 agility points!\n",
                        textWriteStream.getText());

                    brigand.act(player, random, textWriteStream);
                    test.assertEqual(28, player.getHealth());
                    test.assertEqual(28, player.getAgility());
                    test.assertEqual(28, player.getStrength());
                    test.assertEqual(
                        "A brigand hits you with his slingshot, taking off 2 health points!\n" +
                            "A brigand catches your leg with his whip, taking off 2 agility points!\n" +
                            "A brigand cuts your arm with his whip, taking off 2 strength points!\n", textWriteStream.getText());
                });
            });

            runner.testGroup(Hydra.class, () ->
            {
                monsterTests.run(Hydra::new);

                runner.test("constructor()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(0);
                    final Hydra hydra = new Hydra(random);
                    test.assertEqual(1, hydra.getHealth());
                });

                runner.test("show()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(0);
                    final Hydra hydra = new Hydra(random);

                    final InMemoryLineWriteStream textWriteStream = new InMemoryLineWriteStream(CharacterEncoding.UTF_8, "\n");
                    hydra.show(textWriteStream);

                    test.assertEqual("A malicious hydra with 1 heads.\n", textWriteStream.getText());
                });

                runner.test("takeDamage()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(1);
                    final Hydra hydra = new Hydra(random);
                    test.assertEqual(2, hydra.getHealth());

                    final InMemoryLineWriteStream textWriteStream = new InMemoryLineWriteStream(CharacterEncoding.UTF_8, "\n");

                    hydra.takeDamage(1, textWriteStream);
                    test.assertEqual(1, hydra.getHealth());
                    test.assertEqual(
                        "You lop off 1 of the hydra's heads!\n",
                        textWriteStream.getText());

                    hydra.takeDamage(1, textWriteStream);
                    test.assertEqual(0, hydra.getHealth());
                    test.assertEqual(
                        "You lop off 1 of the hydra's heads!\n" +
                            "The corpse of the fully decapitated and decapacitated hydra falls to the floor!\n",
                        textWriteStream.getText());

                    hydra.takeDamage(10, textWriteStream);
                    test.assertEqual(-10, hydra.getHealth());
                    test.assertEqual(
                        "You lop off 1 of the hydra's heads!\n" +
                            "The corpse of the fully decapitated and decapacitated hydra falls to the floor!\n" +
                            "The corpse of the fully decapitated and decapacitated hydra falls to the floor!\n",
                        textWriteStream.getText());
                });

                runner.test("act()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(1);
                    final Hydra hydra = new Hydra(random);
                    final Player player = new Player();
                    final InMemoryLineWriteStream textWriteStream = new InMemoryLineWriteStream(CharacterEncoding.UTF_8, "\n");

                    hydra.act(player, random, textWriteStream);
                    test.assertEqual(29, player.getHealth());
                    test.assertEqual(3, hydra.getHealth());
                    test.assertEqual(
                        "A hydra attacks you with 1 of its heads! It also grows back one more head!\n",
                        textWriteStream.getText());

                    hydra.act(player, random, textWriteStream);
                    test.assertEqual(28, player.getHealth());
                    test.assertEqual(4, hydra.getHealth());
                    test.assertEqual(
                        "A hydra attacks you with 1 of its heads! It also grows back one more head!\n" +
                            "A hydra attacks you with 1 of its heads! It also grows back one more head!\n",
                        textWriteStream.getText());

                    hydra.act(player, random, textWriteStream);
                    test.assertEqual(27, player.getHealth());
                    test.assertEqual(5, hydra.getHealth());
                    test.assertEqual(
                        "A hydra attacks you with 1 of its heads! It also grows back one more head!\n" +
                            "A hydra attacks you with 1 of its heads! It also grows back one more head!\n" +
                            "A hydra attacks you with 1 of its heads! It also grows back one more head!\n",
                        textWriteStream.getText());
                });
            });

            runner.testGroup(Orc.class, () ->
            {
                monsterTests.run(Orc::new);

                runner.test("constructor()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(0);
                    final Orc orc = new Orc(random);
                    test.assertEqual(1, orc.getHealth());
                });

                runner.test("show()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(0);
                    final Orc orc = new Orc(random);

                    final InMemoryLineWriteStream textWriteStream = new InMemoryLineWriteStream(CharacterEncoding.UTF_8, "\n");
                    orc.show(textWriteStream);

                    test.assertEqual("A wicked orc with a level 1 club.\n", textWriteStream.getText());
                });

                runner.test("takeDamage()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(1);
                    final Orc orc = new Orc(random);
                    test.assertEqual(2, orc.getHealth());

                    final InMemoryLineWriteStream textWriteStream = new InMemoryLineWriteStream(CharacterEncoding.UTF_8, "\n");

                    orc.takeDamage(1, textWriteStream);
                    test.assertEqual(1, orc.getHealth());
                    test.assertEqual(
                        "You hit the orc, knocking off 1 health points!\n",
                        textWriteStream.getText());

                    orc.takeDamage(1, textWriteStream);
                    test.assertEqual(0, orc.getHealth());
                    test.assertEqual(
                        "You hit the orc, knocking off 1 health points!\n" +
                            "You killed the orc!\n",
                        textWriteStream.getText());

                    orc.takeDamage(10, textWriteStream);
                    test.assertEqual(-10, orc.getHealth());
                    test.assertEqual(
                        "You hit the orc, knocking off 1 health points!\n" +
                            "You killed the orc!\n" +
                            "You killed the orc!\n",
                        textWriteStream.getText());
                });

                runner.test("act()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(1);
                    final Orc orc = new Orc(random);
                    final Player player = new Player();
                    final InMemoryLineWriteStream textWriteStream = new InMemoryLineWriteStream(CharacterEncoding.UTF_8, "\n");

                    orc.act(player, random, textWriteStream);
                    test.assertEqual(28, player.getHealth());
                    test.assertEqual(
                        "An orc swings his club at you and knocks off 2 of your health points.\n",
                        textWriteStream.getText());

                    orc.act(player, random, textWriteStream);
                    test.assertEqual(26, player.getHealth());
                    test.assertEqual(
                        "An orc swings his club at you and knocks off 2 of your health points.\n" +
                            "An orc swings his club at you and knocks off 2 of your health points.\n",
                        textWriteStream.getText());

                    orc.act(player, random, textWriteStream);
                    test.assertEqual(24, player.getHealth());
                    test.assertEqual(
                        "An orc swings his club at you and knocks off 2 of your health points.\n" +
                            "An orc swings his club at you and knocks off 2 of your health points.\n" +
                            "An orc swings his club at you and knocks off 2 of your health points.\n",
                        textWriteStream.getText());
                });
            });

            runner.testGroup(Player.class, () ->
            {
                runner.test("subtractHealth()", (Test test) ->
                {
                    final Player player = new Player();
                    player.subtractHealth(5);
                    test.assertEqual(25, player.getHealth());

                    player.subtractHealth(25);
                    test.assertEqual(0, player.getHealth());

                    player.subtractHealth(1);
                    test.assertEqual(0, player.getHealth());
                });

                runner.test("isDead()", (Test test) ->
                {
                    final Player player = new Player();
                    test.assertFalse(player.isDead());

                    player.subtractHealth(player.getHealth());
                    test.assertTrue(player.isDead());
                });

                runner.test("subtractAgility()", (Test test) ->
                {
                    final Player player = new Player();
                    player.subtractAgility(5);
                    test.assertEqual(25, player.getAgility());

                    player.subtractAgility(25);
                    test.assertEqual(1, player.getAgility());

                    player.subtractAgility(1);
                    test.assertEqual(1, player.getAgility());
                });

                runner.test("subtractStrength()", (Test test) ->
                {
                    final Player player = new Player();
                    player.subtractStrength(5);
                    test.assertEqual(25, player.getStrength());

                    player.subtractStrength(25);
                    test.assertEqual(1, player.getStrength());

                    player.subtractStrength(1);
                    test.assertEqual(1, player.getStrength());
                });

                runner.test("show()", (Test test) ->
                {
                    final Player player = new Player();
                    final InMemoryLineWriteStream lineWriteStream = new InMemoryLineWriteStream(CharacterEncoding.UTF_8, "\n");

                    player.show(lineWriteStream);
                    test.assertEqual(
                        "You are a valiant knight with a health of 30, an agility of 30, and a strength of 30.\n",
                        lineWriteStream.getText());

                    player.subtractHealth(1);
                    player.subtractAgility(2);
                    player.subtractStrength(3);

                    player.show(lineWriteStream);
                    test.assertEqual(
                        "You are a valiant knight with a health of 30, an agility of 30, and a strength of 30.\n" +
                            "You are a valiant knight with a health of 29, an agility of 28, and a strength of 27.\n",
                        lineWriteStream.getText());
                });
            });

            runner.testGroup(Slime.class, () ->
            {
                monsterTests.run(Slime::new);

                runner.test("constructor()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(0);
                    final Slime slime = new Slime(random);
                    test.assertEqual(1, slime.getHealth());
                });

                runner.test("show()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(0);
                    final Slime slime = new Slime(random);

                    final InMemoryLineWriteStream textWriteStream = new InMemoryLineWriteStream(CharacterEncoding.UTF_8, "\n");
                    slime.show(textWriteStream);

                    test.assertEqual("A slime mold with a sliminess of 1.\n", textWriteStream.getText());
                });

                runner.test("takeDamage()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(1);
                    final Slime slime = new Slime(random);
                    test.assertEqual(2, slime.getHealth());

                    final InMemoryLineWriteStream textWriteStream = new InMemoryLineWriteStream(CharacterEncoding.UTF_8, "\n");

                    slime.takeDamage(1, textWriteStream);
                    test.assertEqual(1, slime.getHealth());
                    test.assertEqual(
                        "You hit the slime, knocking off 1 health points.\n",
                        textWriteStream.getText());

                    slime.takeDamage(1, textWriteStream);
                    test.assertEqual(0, slime.getHealth());
                    test.assertEqual(
                        "You hit the slime, knocking off 1 health points.\n" +
                            "You killed the slime!\n",
                        textWriteStream.getText());

                    slime.takeDamage(10, textWriteStream);
                    test.assertEqual(-10, slime.getHealth());
                    test.assertEqual(
                        "You hit the slime, knocking off 1 health points.\n" +
                            "You killed the slime!\n" +
                            "You killed the slime!\n",
                        textWriteStream.getText());
                });

                runner.test("act()", (Test test) ->
                {
                    final FixedRandom random = new FixedRandom(1);
                    final Slime slime = new Slime(random);
                    final Player player = new Player();
                    final InMemoryLineWriteStream textWriteStream = new InMemoryLineWriteStream(CharacterEncoding.UTF_8, "\n");

                    slime.act(player, random, textWriteStream);
                    test.assertEqual(30, player.getHealth());
                    test.assertEqual(29, player.getAgility());
                    test.assertEqual(
                        "A slime mold wraps around your legs and decreases your agility by 1!\n",
                        textWriteStream.getText());

                    random.setValue(2);

                    slime.act(player, random, textWriteStream);
                    test.assertEqual(29, player.getHealth());
                    test.assertEqual(27, player.getAgility());
                    test.assertEqual(
                        "A slime mold wraps around your legs and decreases your agility by 1!\n" +
                            "A slime mold wraps around your legs and decreases your agility by 2! It also squirts in your face, taking away a health point!\n",
                        textWriteStream.getText());

                    slime.act(player, random, textWriteStream);
                    test.assertEqual(28, player.getHealth());
                    test.assertEqual(25, player.getAgility());
                    test.assertEqual(
                        "A slime mold wraps around your legs and decreases your agility by 1!\n" +
                            "A slime mold wraps around your legs and decreases your agility by 2! It also squirts in your face, taking away a health point!\n" +
                            "A slime mold wraps around your legs and decreases your agility by 2! It also squirts in your face, taking away a health point!\n",
                        textWriteStream.getText());
                });
            });
        });
    }
}
