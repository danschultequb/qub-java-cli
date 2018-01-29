package qub;

import org.junit.Test;

import static org.junit.Assert.*;

public class QubCLITests
{
    private static final String expectedUsageString =
        "Usage: qub <action> [<action-options>]\n" +
        "Possible Actions:\n" +
        "  Build: Build the coding project in the current folder.\n" +
        "    Usage: Build\n" +
        "\n" +
        "  Delete: Delete a provided file or folder.\n" +
        "    Usage: Delete [-file] [-folder] <file-folder-or-filter-to-delete> [<additional-file-folder-or-filter-to-delete> ...]\n" +
        "\n";

    @Test
    public void mainWithNoArguments()
    {
        final Console console = new Console();
        final InMemoryCharacterWriteStream output = new InMemoryCharacterWriteStream();
        console.setOutput(output);

        QubCLI.main(console);

        assertEquals(expectedUsageString, output.getText());
    }

    @Test
    public void mainWithDashQuestionMarkAction()
    {
        final Console console = new Console(new String[] { "-?" });
        final InMemoryCharacterWriteStream output = new InMemoryCharacterWriteStream();
        console.setOutput(output);

        QubCLI.main(console);

        assertEquals(expectedUsageString, output.getText());
    }

    @Test
    public void mainWithForwardSlashQuestionMarkAction()
    {
        final Console console = new Console(new String[] { "/?" });
        final InMemoryCharacterWriteStream output = new InMemoryCharacterWriteStream();
        console.setOutput(output);

        QubCLI.main(console);

        assertEquals(expectedUsageString, output.getText());
    }

    @Test
    public void mainWithUnrecognizedAction()
    {
        final Console console = new Console(new String[] { "spam" });
        final InMemoryCharacterWriteStream output = new InMemoryCharacterWriteStream();
        console.setOutput(output);

        QubCLI.main(console);

        assertEquals("Unrecognized action: \"spam\"\n", output.getText());
    }
}
