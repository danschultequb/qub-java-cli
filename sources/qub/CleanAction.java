package qub;

/**
 * A QubCLI Action that deletes code projects' outputs folder.
 */
public class CleanAction implements Action
{
    @Override
    public String getName()
    {
        return "Clean";
    }

    @Override
    public String getDescription()
    {
        return "Clean the coding project in the current folder.";
    }

    @Override
    public String getArgumentUsage()
    {
        return null;
    }

    @Override
    public void run(Console console)
    {
        final ProjectJson projectJson = ProjectJson.parse(console);
        if (projectJson != null)
        {
            final Folder outputsFolder = projectJson.getJavaOutputsFolder();
            if (outputsFolder != null)
            {
                console.write("Deleting folder " + outputsFolder + "...");
                if (outputsFolder.delete())
                {
                    console.writeLine(" Done.");
                }
                else
                {
                    console.writeLine(" Failed.");
                }
            }
        }
    }
}
