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

        final Folder currentFolder = console.getCurrentFolder().getValue();
        final Kitchen kitchen = Kitchen.parse(currentFolder);
        final Value<Boolean> done = new Value<>(false);

        final NamedAction quitAction = new NamedAction("Quit", Array.fromValues(new String[] { "Exit" }), () ->
        {
            done.set(true);
            kitchen.save(currentFolder);
        });

        final NamedAction addRecipeAction = new NamedAction("Add recipe", () ->
        {
            console.writeLine();

            final RecipeCreator recipeCreator = kitchen.getRecipeCreator();
            console.writeLine("Enter recipe name: ");

            String input = readNonEmptyLine(console);
            console.writeLine();
            if (quitAction.matches(input))
            {
                quitAction.run();
            }
            else
            {
                recipeCreator.setName(input);

                console.writeLine("Enter ingredients. Press enter with an empty line to stop:");
                boolean addIngredients = true;
                while (addIngredients)
                {
                    input = readTrimmedLine(console);
                    if (input == null || input.isEmpty() || quitAction.matches(input))
                    {
                        addIngredients = false;
                    }
                    else
                    {
                        recipeCreator.addIngredient(input);
                    }
                }

                if (quitAction.matches(input))
                {
                    quitAction.run();
                }
                else
                {
                    console.writeLine("Enter steps. Press enter with an empty line to stop:");
                    boolean addSteps = true;
                    while (addSteps)
                    {
                        input = readTrimmedLine(console);
                        if (input == null || input.isEmpty() || quitAction.matches(input))
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
                        console.writeLine("Enter source:");
                        input = readTrimmedLine(console);
                        console.writeLine();

                        if (quitAction.matches(input))
                        {
                            quitAction.run();
                        }
                        else
                        {
                            recipeCreator.setSource(input);

                            console.writeLine("Enter notes. Press enter with an empty line to stop:");
                            boolean addNotes = true;
                            while (addNotes)
                            {
                                input = readTrimmedLine(console);
                                if (input == null || input.isEmpty() || quitAction.matches(input))
                                {
                                    addNotes = false;
                                }
                                else
                                {
                                    recipeCreator.addNote(input);
                                }
                            }

                            recipeCreator.apply();
                            console.writeLine("Added recipe.");
                            console.writeLine();
                        }
                    }
                }
            }
        });

        final NamedAction listRecipesAction = new NamedAction("List recipes", () ->
        {
            console.writeLine();
            console.writeLine("Recipes:");
            int recipeNumber = 1;
            for (final String recipeName : kitchen.getRecipes().map(Recipe::getName))
            {
                console.writeLine(recipeNumber + ") " + recipeName);
                ++recipeNumber;
            }
            console.writeLine();
        });

        final NamedAction readRecipeAction = new NamedAction("Read recipe", () ->
        {
            console.writeLine();
            final Recipe recipe = selectRecipe(console, kitchen);
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

                final Iterable<String> notes = recipe.getNotes();
                if (notes.any())
                {
                    console.writeLine("Notes:");
                    for (final String note : notes)
                    {
                        console.writeLine("  - " + note);
                    }
                    console.writeLine();
                }

                final String source = recipe.getSource();
                if (source != null && !source.isEmpty())
                {
                    console.writeLine("Source:");
                    console.writeLine("  " + source);
                    console.writeLine();
                }
            }
        });

        final NamedAction editRecipeAction = new NamedAction("Edit recipe", () ->
        {
            console.writeLine();
            final Recipe recipeToEdit = selectRecipe(console, kitchen);
            if (recipeToEdit != null)
            {
                final RecipeEditor recipeEditor = kitchen.getRecipeEditor(recipeToEdit);

                final Value<Boolean> doneEditing = new Value<>(false);

                final NamedAction editNameAction = new NamedAction("Name", () ->
                {
                    console.writeLine();
                    console.writeLine("Enter the new recipe name:");
                    recipeEditor.setName(readNonEmptyLine(console));
                    console.writeLine();
                });

                final NamedAction editSourceAction = new NamedAction("Source", () ->
                {
                    console.writeLine();
                    console.writeLine("Enter the new recipe source:");
                    recipeEditor.setSource(readTrimmedLine(console));
                    console.writeLine();
                });

                final NamedAction editNotesAction = new NamedAction("Notes", () ->
                {
                    console.writeLine();

                    final Iterable<String> notes = recipeToEdit.getNotes();
                    if (!notes.any())
                    {
                        console.writeLine("Enter notes. Press enter with an empty line to stop:");
                        boolean addNotes = true;
                        while (addNotes)
                        {
                            final String input = readTrimmedLine(console);
                            if (input == null || input.isEmpty() || quitAction.matches(input))
                            {
                                addNotes = false;
                            }
                            else
                            {
                                recipeEditor.addNote(input);
                            }
                        }
                    }
                    else
                    {
                        console.writeLine("Enter number of note to edit, \"New\" to add a new notes, or \"Done\" to go back:");
                        int noteNumber = 0;
                        for (final String note : notes)
                        {
                            console.writeLine(++noteNumber + ") " + note);
                        }
                        final int newNoteNumber = ++noteNumber;
                        console.writeLine(newNoteNumber + ") New");

                        final int doneNoteNumber = ++noteNumber;
                        console.writeLine(doneNoteNumber + ") Done");

                        String input = readNonEmptyLine(console);

                        int noteNumberSelection;
                        try
                        {
                            noteNumberSelection = Integer.valueOf(input);
                        }
                        catch (NumberFormatException ignored)
                        {
                            noteNumberSelection = -1;
                        }

                        if (quitAction.matches(input))
                        {
                            quitAction.run();
                        }
                        else if (noteNumberSelection == doneNoteNumber || input.equalsIgnoreCase("Done"))
                        {
                        }
                        else if (noteNumberSelection == newNoteNumber || input.equalsIgnoreCase("New"))
                        {
                            console.writeLine("Enter notes. Press enter with an empty line to stop:");
                            boolean addNotes = true;
                            while (addNotes)
                            {
                                input = readTrimmedLine(console);
                                if (input == null || input.isEmpty() || quitAction.matches(input))
                                {
                                    addNotes = false;
                                }
                                else
                                {
                                    recipeEditor.addNote(input);
                                }
                            }
                        }
                        else if (0 <= noteNumberSelection && noteNumberSelection < newNoteNumber)
                        {
                            console.writeLine("Enter note. Press enter with an empty line to remove note " + noteNumberSelection + ":");
                            input = readTrimmedLine(console);
                            if (quitAction.matches(input))
                            {
                                quitAction.run();
                            }
                            else if (input == null || input.isEmpty())
                            {
                                recipeEditor.removeNote(noteNumberSelection);
                            }
                            else
                            {
                                recipeEditor.setNote(noteNumberSelection, input);
                            }
                        }
                        else
                        {
                            console.writeLine("Sorry, I didn't recognize your selection.");
                            console.writeLine();
                        }
                    }
                });

                final NamedAction doneEditingAction = new NamedAction("Done", () ->
                {
                    doneEditing.set(true);
                });

                final Array<NamedAction> editingActions = Array.fromValues(new NamedAction[]
                {
                    editNameAction,
                    editSourceAction,
                    editNotesAction,
                    doneEditingAction
                });

                while (!done.get() && !doneEditing.get())
                {
                    selectAndRunAction(console, "What do you want to edit?", editingActions);
                }

                if (!done.get())
                {
                    recipeEditor.apply();

                    console.writeLine();
                    console.writeLine("Recipe updated.");
                    console.writeLine();
                }
            }
        });

        final NamedAction removeRecipeAction = new NamedAction("Remove recipe", () ->
        {
            console.writeLine();
            final Recipe recipeToRemove = selectRecipe(console, kitchen);
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
            selectAndRunAction(console, "Please pick an action:", namedActions);
        }
    }

    private static void selectAndRunAction(Console console, String prompt, Indexable<NamedAction> actions)
    {
        console.writeLine(prompt);
        for (int i = 0; i < actions.getCount(); ++i)
        {
            console.writeLine((i + 1) + ") " + actions.get(i).getName());
        }
        console.writeLine();

        final String input = readNonEmptyLine(console).toLowerCase();
        NamedAction selectedAction = actions.first((NamedAction namedAction) -> namedAction.matches(input));
        if (selectedAction == null)
        {
            try
            {
                final int actionNumber = Integer.parseInt(input);
                final int actionIndex = actionNumber - 1;
                selectedAction = actions.get(actionIndex);
            }
            catch (NumberFormatException ignored)
            {
            }
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

    private static Recipe selectRecipe(Console console, Kitchen kitchen)
    {
        console.write("Which recipe: ");

        final String recipeIdentifier = readNonEmptyLine(console);
        final Indexable<Recipe> recipes = kitchen.getRecipes();
        Recipe selectedRecipe = recipes.first((Recipe recipe) -> recipe.getName().equalsIgnoreCase(recipeIdentifier));
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
        String result = null;
        while (result == null || result.isEmpty())
        {
            result = readTrimmedLine(console);
        }
        return result;
    }

    private static String readTrimmedLine(Console console)
    {
        String result = console.readLine().getValue();
        if (result != null)
        {
            result = result.trim();
        }
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

        RecipeCreator getRecipeCreator()
        {
            return new RecipeCreator(this);
        }

        RecipeEditor getRecipeEditor(Recipe recipe)
        {
            return new RecipeEditor(this, recipe);
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
            return parse(CharacterEncoding.UTF_8.decode(kitchenFile.getContents().getValue()).getValue());
        }

        static Kitchen parse(char[] kitchenContents)
        {
            return parse(kitchenContents == null ? null : String.valueOf(kitchenContents));
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

                                final JSONQuotedString sourceSegment = (JSONQuotedString)jsonRecipe.getPropertyValue("source");
                                final String source = sourceSegment == null ? null : sourceSegment.toUnquotedString();

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

                                final List<String> notes = new ArrayList<>();
                                final JSONArray notesSegment = Types.as(jsonRecipe.getPropertyValue("notes"), JSONArray.class);
                                if (notesSegment != null)
                                {
                                    notes.addAll(notesSegment
                                        .getElements()
                                        .instanceOf(JSONQuotedString.class)
                                        .map(JSONQuotedString::toUnquotedString));
                                }

                                recipe = new Recipe(source, recipeName, ingredients, steps, notes);
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
                final InMemoryLineStream lineStream = new InMemoryLineStream();
                save(lineStream);
                kitchenFile.setContents(lineStream.getBytes());
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

                            final String source = recipe.getSource();
                            if (source != null && !source.isEmpty())
                            {
                                recipeSegment.writeQuotedStringProperty("source", source);
                            }

                            final Iterable<String> notes = recipe.getNotes();
                            if (notes.any())
                            {
                                recipeSegment.writeArrayProperty("notes", (JSONArrayWriteStream notesSegment) ->
                                {
                                    for (final String note : notes)
                                    {
                                        notesSegment.writeQuotedString(note);
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
            return folder.getFile("kitchen.json").getValue();
        }
    }

    static class Recipe
    {
        private String source;
        private String name;
        private final Iterable<String> ingredients;
        private final Iterable<String> steps;
        private Iterable<String> notes;

        Recipe(String source, String name, Iterable<String> ingredients, Iterable<String> steps, Iterable<String> notes)
        {
            this.source = source;
            this.name = name;
            this.ingredients = ingredients;
            this.steps = steps;
            this.notes = notes;
        }

        String getSource()
        {
            return source;
        }

        void setSource(String source)
        {
            this.source = source;
        }

        String getName()
        {
            return name;
        }

        void setName(String name)
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

        Iterable<String> getNotes()
        {
            return notes;
        }

        void setNotes(Iterable<String> notes)
        {
            this.notes = notes;
        }
    }

    static class RecipeCreator
    {
        private final Kitchen kitchen;
        private String name;
        private final List<String> ingredients;
        private final List<String> steps;
        private String source;
        private final List<String> notes;

        RecipeCreator(Kitchen kitchen)
        {
            this.kitchen = kitchen;
            ingredients = new ArrayList<>();
            steps = new ArrayList<>();
            notes = new ArrayList<>();
        }

        void setSource(String source)
        {
            this.source = source;
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

        void addNote(String note)
        {
            notes.add(note);
        }

        void apply()
        {
            kitchen.addRecipe(new Recipe(source, name, ingredients, steps, notes));
        }
    }

    static class RecipeEditor
    {
        private final Kitchen kitchen;
        private final Recipe toEdit;
        private Value<String> name;
        private Value<List<String>> notes;
        private Value<String> source;

        RecipeEditor(Kitchen kitchen, Recipe toEdit)
        {
            this.kitchen = kitchen;
            this.toEdit = toEdit;
            this.name = new Value<>();
            this.notes = new Value<>();
            this.source = new Value<>();
        }

        void setName(String name)
        {
            this.name.set(name);
        }

        private void initializeNotes()
        {
            if (!this.notes.hasValue())
            {
                this.notes.set(ArrayList.fromValues(toEdit.getNotes()));
            }
        }

        void addNote(String note)
        {
            initializeNotes();
            this.notes.get().add(note);
        }

        public void setNote(int noteNumber, String note)
        {
            initializeNotes();
            this.notes.get().set(noteNumber, note);
        }

        void removeNote(int noteNumber)
        {
            initializeNotes();
            this.notes.get().removeAt(noteNumber - 1);
        }

        void setSource(String source)
        {
            this.source.set(source);
        }

        void apply()
        {
            boolean changed = false;

            if (name.hasValue() && !Comparer.equal(name.get(), toEdit.getName()))
            {
                toEdit.setName(name.get());
                changed = true;
            }

            if (notes.hasValue() && !Comparer.equal(notes.get(), toEdit.getNotes()))
            {
                toEdit.setNotes(notes.get());
                changed = true;
            }

            if (source.hasValue() && !Comparer.equal(source.get(), toEdit.getSource()))
            {
                toEdit.setSource(source.get());
                changed = true;
            }

            if (changed)
            {
                kitchen.setChanged();
            }
        }
    }
}
