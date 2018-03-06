package qub;

import java.util.Objects;

public class KitchenAction implements Action
{
    @Override
    public String getName()
    {
        return "Kitchen";
    }

    @Override
    public String getDescription()
    {
        return "An application that manages recipes.";
    }

    @Override
    public String getArgumentUsage()
    {
        return null;
    }

    @Override
    public void run(Console console)
    {
        console.writeLine("Welcome to Qub Kitchen!");
        console.writeLine();

        final Folder currentFolder = console.getCurrentFolder();
        final Kitchen kitchen = Kitchen.parse(currentFolder);
        final Value<Boolean> done = new Value<>(false);

        final NamedAction quitAction = new NamedAction("Quit", Array.fromValues(new String[] { "Exit" }), () ->
        {
            done.set(true);
            kitchen.save(currentFolder);
        });

        final NamedAction addRecipeAction = new NamedAction("Add recipe", () ->
        {
            final Kitchen.Recipe.Creator recipeCreator = kitchen.getRecipeCreator();
            console.write("Enter recipe name: ");

            String input = readNonEmptyLine(console);
            console.writeLine();
            if (input.equalsIgnoreCase("quit"))
            {
                quitAction.run();
            }
            else
            {
                recipeCreator.setName(input);

                boolean addIngredients = true;
                while (addIngredients)
                {
                    console.write("Enter ingredient or \"done\": ");
                    input = readNonEmptyLine(console);
                    if (input.equalsIgnoreCase("done") || quitAction.matches(input))
                    {
                        addIngredients = false;
                    }
                    else
                    {
                        recipeCreator.addIngredient(input);
                    }
                }
                console.writeLine();

                if (quitAction.matches(input))
                {
                    quitAction.run();
                }
                else
                {
                    boolean addSteps = true;
                    while (addSteps)
                    {
                        console.write("Enter step or \"done\": ");
                        input = readNonEmptyLine(console);
                        if (input.equalsIgnoreCase("done") || quitAction.matches(input))
                        {
                            addSteps = false;
                        }
                        else
                        {
                            recipeCreator.addStep(input);
                        }
                    }

                    if (quitAction.matches(input))
                    {
                        quitAction.run();
                    }
                    else
                    {
                        recipeCreator.apply();
                        console.writeLine();
                        console.writeLine("Added recipe.");
                        console.writeLine();
                    }
                }
            }
        });

        final NamedAction listRecipesAction = new NamedAction("List recipes", () ->
        {
            console.writeLine("Recipes:");
            int recipeNumber = 1;
            for (final String recipeName : kitchen.getRecipes().map(Kitchen.Recipe::getName))
            {
                console.writeLine(recipeNumber + ") " + recipeName);
                ++recipeNumber;
            }
            console.writeLine();
        });

        final NamedAction readRecipeAction = new NamedAction("Read recipe", () ->
        {
            final Kitchen.Recipe recipe = selectRecipe(console, kitchen);
            if (recipe != null)
            {
                console.writeLine();
                console.writeLine(recipe.getName());
                console.writeLine();

                final Iterable<String> ingredients = recipe.getIngredients();
                if (ingredients.any())
                {
                    console.writeLine("Ingredients:");
                    for (final String ingredientName : ingredients)
                    {
                        console.writeLine("  - " + ingredientName);
                    }
                    console.writeLine();
                }

                final Iterable<String> steps = recipe.getSteps();
                if (steps.any())
                {
                    console.writeLine("Steps:");
                    int stepNumber = 1;
                    for (final String step : steps)
                    {
                        console.writeLine("  " + stepNumber + ". " + step);
                        ++stepNumber;
                    }
                    console.writeLine();
                }
            }
        });

        final NamedAction editRecipeAction = new NamedAction("Edit recipe", () ->
        {
            final Kitchen.Recipe recipeToEdit = selectRecipe(console, kitchen);
            if (recipeToEdit != null)
            {
                final Kitchen.Recipe.Editor recipeEditor = kitchen.getRecipeEditor(recipeToEdit);

                console.write("Enter new recipe name: ");
                recipeEditor.setName(readNonEmptyLine(console));
                recipeEditor.apply();

                console.writeLine();
                console.writeLine("Recipe updated.");
            }
        });

        final NamedAction removeRecipeAction = new NamedAction("Remove recipe", () ->
        {
            final Kitchen.Recipe recipeToRemove = selectRecipe(console, kitchen);
            if (recipeToRemove != null)
            {
                kitchen.removeRecipe(recipeToRemove);

                console.writeLine();
                console.writeLine("Recipe removed.");
                console.writeLine();
            }
        });

        final Array<NamedAction> namedActions = Array.fromValues(new NamedAction[]
            {
                addRecipeAction,
                listRecipesAction,
                readRecipeAction,
                editRecipeAction,
                removeRecipeAction,
                quitAction
            });

        while (!done.get())
        {
            console.writeLine("Please pick an action:");
            for (int i = 0; i < namedActions.getCount(); ++i)
            {
                console.writeLine((i + 1) + ") " + namedActions.get(i).getName());
            }
            console.writeLine();

            final String input = readNonEmptyLine(console).toLowerCase();
            NamedAction selectedAction = namedActions.first((NamedAction namedAction) -> namedAction.matches(input));
            if (selectedAction == null)
            {
                try
                {
                    final int actionNumber = Integer.parseInt(input);
                    final int actionIndex = actionNumber - 1;
                    selectedAction = namedActions.get(actionIndex);
                }
                catch (NumberFormatException ignored)
                {
                }
            }

            if (selectedAction == null || !selectedAction.matches("quit"))
            {
                console.writeLine();
            }

            if (selectedAction == null)
            {
                console.writeLine("Sorry, I didn't recognize your selection. Please try again.");
                console.writeLine();
            }
            else
            {
                selectedAction.run();
            }
        }
    }

    private static Kitchen.Recipe selectRecipe(Console console, Kitchen kitchen)
    {
        console.write("Which recipe: ");

        final String recipeIdentifier = readNonEmptyLine(console);
        final Indexable<Kitchen.Recipe> recipes = kitchen.getRecipes();
        Kitchen.Recipe selectedRecipe = recipes.first((Kitchen.Recipe recipe) -> recipe.getName().equalsIgnoreCase(recipeIdentifier));
        if (selectedRecipe == null)
        {
            try
            {
                final Integer recipeNumber = Integer.valueOf(recipeIdentifier);
                final int recipeIndex = recipeNumber - 1;
                selectedRecipe = recipes.get(recipeIndex);
                if (selectedRecipe == null)
                {
                    console.writeLine("Sorry, I couldn't find a recipe number " + recipeNumber + ".");
                }
            }
            catch (NumberFormatException ignored)
            {
                console.writeLine("Sorry, I couldn't find a recipe named \"" + recipeIdentifier + "\".");
            }
        }

        return selectedRecipe;
    }

    private static String readNonEmptyLine(Console console)
    {
        String result;
        do
        {
            result = console.readLine();
            if (result != null)
            {
                result = result.trim();
            }
        }
        while (result == null || result.isEmpty());

        return result;
    }

    static class NamedAction
    {
        private final String name;
        private final Array<String> aliases;
        private final Action0 action;

        NamedAction(String name, Action0 action)
        {
            this(name, new Array<>(0), action);
        }

        NamedAction(String name, Array<String> aliases, Action0 action)
        {
            this.name = name;
            this.aliases = aliases;
            this.action = action;
        }

        String getName()
        {
            return name;
        }

        boolean matches(String text)
        {
            return name.equalsIgnoreCase(text) ||
                aliases.contains((String alias) -> alias.equalsIgnoreCase(text));
        }

        void run()
        {
            action.run();
        }
    }

    static class Kitchen
    {
        private final List<Recipe> recipes;
        private boolean changed;

        Kitchen(List<Recipe> recipes)
        {
            this.recipes = recipes;
        }

        Recipe.Creator getRecipeCreator()
        {
            return new Recipe.Creator(this);
        }

        Recipe.Editor getRecipeEditor(Recipe recipe)
        {
            return new Recipe.Editor(this, recipe);
        }

        void removeRecipe(Recipe recipe)
        {
            recipes.remove(recipe);
        }

        Indexable<Recipe> getRecipes()
        {
            return recipes;
        }

        private void setChanged()
        {
            changed = true;
        }

        private void addRecipe(Recipe recipe)
        {
            recipes.add(recipe);
            changed = true;
        }

        static Kitchen parse(Folder folder)
        {
            return parse(getKitchenFile(folder));
        }

        static Kitchen parse(File kitchenFile)
        {
            return parse(kitchenFile.getContentsAsString());
        }

        static Kitchen parse(String kitchenContents)
        {
            final List<Recipe> recipes = new ArrayList<>();

            final JSONDocument recipesDocument = JSON.parse(kitchenContents);
            final JSONObject root = (JSONObject)recipesDocument.getRoot();
            if (root != null)
            {
                final JSONArray recipesSegment = (JSONArray) root.getPropertyValue("recipes");
                if (recipesSegment != null)
                {
                    recipes.addAll(recipesSegment.getElements()
                        .instanceOf(JSONObject.class)
                        .map((JSONObject jsonRecipe) ->
                        {
                            Recipe recipe = null;

                            final JSONQuotedString recipeNameSegment = (JSONQuotedString)jsonRecipe.getPropertyValue("name");
                            if (recipeNameSegment != null)
                            {
                                final String recipeName = recipeNameSegment.toUnquotedString();

                                final List<String> ingredients = new ArrayList<>();
                                final JSONArray ingredientsSegment = (JSONArray)jsonRecipe.getPropertyValue("ingredients");
                                if (ingredientsSegment != null)
                                {
                                    ingredients.addAll(ingredientsSegment
                                        .getElements()
                                        .instanceOf(JSONQuotedString.class)
                                        .map(JSONQuotedString::toUnquotedString));
                                }

                                final List<String> steps = new ArrayList<>();
                                final JSONArray stepsSegment = (JSONArray)jsonRecipe.getPropertyValue("steps");
                                if (stepsSegment != null)
                                {
                                    steps.addAll(stepsSegment
                                        .getElements()
                                        .instanceOf(JSONQuotedString.class)
                                        .map(JSONQuotedString::toUnquotedString));
                                }

                                recipe = new Recipe(recipeName, ingredients, steps);
                            }

                            return recipe;
                        })
                        .where(Objects::nonNull));
                }
            }

            return new Kitchen(recipes);
        }

        void save(Folder folder)
        {
            save(getKitchenFile(folder));
        }

        void save(File kitchenFile)
        {
            if (changed)
            {
                final InMemoryLineWriteStream lineWriteStream = new InMemoryLineWriteStream();
                save(lineWriteStream);
                kitchenFile.setContents(lineWriteStream.getBytes());
            }
        }

        void save(LineWriteStream lineWriteStream)
        {
            final JSONWriteStream jsonWriteStream = new JSONWriteStream(lineWriteStream);
            jsonWriteStream.writeObject((JSONObjectWriteStream root) ->
            {
                root.writeArrayProperty("recipes", (JSONArrayWriteStream recipesSegment) ->
                {
                    for (final Recipe recipe : recipes)
                    {
                        recipesSegment.writeObject((JSONObjectWriteStream recipeSegment) ->
                        {
                            recipeSegment.writeQuotedStringProperty("name", recipe.getName());

                            final Iterable<String> ingredients = recipe.getIngredients();
                            if (ingredients.any())
                            {
                                recipeSegment.writeArrayProperty("ingredients", (JSONArrayWriteStream ingredientsSegment) ->
                                {
                                    for (final String ingredientName : ingredients)
                                    {
                                        ingredientsSegment.writeQuotedString(ingredientName);
                                    }
                                });
                            }

                            final Iterable<String> steps = recipe.getSteps();
                            if (steps.any())
                            {
                                recipeSegment.writeArrayProperty("steps", (JSONArrayWriteStream stepsSegment) ->
                                {
                                    for (final String step : steps)
                                    {
                                        stepsSegment.writeQuotedString(step);
                                    }
                                });
                            }
                        });
                    }
                });
            });
        }

        static File getKitchenFile(Folder folder)
        {
            return folder.getFile("kitchen.json");
        }

        static class Recipe
        {
            private String name;
            private final Iterable<String> ingredients;
            private final Iterable<String> steps;

            Recipe(String name, List<String> ingredients, List<String> steps)
            {
                this.name = name;
                this.ingredients = ingredients;
                this.steps = steps;
            }

            String getName()
            {
                return name;
            }

            private void setName(String name)
            {
                this.name = name;
            }

            Iterable<String> getIngredients()
            {
                return ingredients;
            }

            Iterable<String> getSteps()
            {
                return steps;
            }

            static class Creator
            {
                private final Kitchen kitchen;
                private String name;
                private final List<String> ingredients;
                private final List<String> steps;

                Creator(Kitchen kitchen)
                {
                    this.kitchen = kitchen;
                    ingredients = new ArrayList<>();
                    steps = new ArrayList<>();
                }

                void setName(String name)
                {
                    this.name = name;
                }

                void addIngredient(String ingredient)
                {
                    ingredients.add(ingredient);
                }

                void addStep(String step)
                {
                    steps.add(step);
                }

                void apply()
                {
                    kitchen.addRecipe(new Recipe(name, ingredients, steps));
                }
            }

            static class Editor
            {
                private final Kitchen kitchen;
                private final Recipe toEdit;
                private String name;

                Editor(Kitchen kitchen, Recipe toEdit)
                {
                    this.kitchen = kitchen;
                    this.toEdit = toEdit;
                }

                void setName(String name)
                {
                    this.name = name;
                }

                void apply()
                {
                    if (name != null)
                    {
                        toEdit.setName(name);
                        kitchen.setChanged();
                    }
                }
            }
        }
    }
}
