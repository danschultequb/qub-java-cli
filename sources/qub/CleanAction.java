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
        final JSONObject projectJsonRoot = QubCLI.readProjectJson(console);
        if (projectJsonRoot != null)
        {
            final JSONObject java = QubCLI.getJavaSegment(console, projectJsonRoot);
            if (java != null)
            {
                final Folder outputsFolder = QubCLI.getOutputsFolder(console, java);
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
}
