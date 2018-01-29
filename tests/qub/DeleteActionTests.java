package qub;

import org.junit.Test;

import static org.junit.Assert.*;

public class DeleteActionTests
{
    @Test
    public void runWithNoArguments()
    {
        final Console console = new Console(new String[] { "delete" });
        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        new DeleteAction().run(console);

        assertEquals("", output.getText());
    }

    @Test
    public void runWithRootedFilePathThatDoesntExist()
    {
        final Console console = new Console(new String[] { "delete", "/fileThatDoesntExist.txt" });
        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        new DeleteAction().run(console);

        assertEquals("/fileThatDoesntExist.txt doesn't exist.\n", output.getText());
        assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithFileFlagAndRootedFilePathThatDoesntExist()
    {
        final Console console = new Console(new String[] { "delete", "-file", "/fileThatDoesntExist.txt" });
        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        new DeleteAction().run(console);

        assertEquals("/fileThatDoesntExist.txt doesn't exist.\n", output.getText());
        assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithFilesFlagAndRootedFilePathThatDoesntExist()
    {
        final Console console = new Console(new String[] { "delete", "-files", "/fileThatDoesntExist.txt" });
        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        new DeleteAction().run(console);

        assertEquals("/fileThatDoesntExist.txt doesn't exist.\n", output.getText());
        assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithRelativeFilePathThatDoesntExist()
    {
        final Console console = new Console(new String[] { "delete", "fileThatDoesntExist.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("fileThatDoesntExist.txt doesn't exist.\n", output.getText());
        assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithFileFlagAndRelativeFilePathThatDoesntExist()
    {
        final Console console = new Console(new String[] { "delete", "-file", "fileThatDoesntExist.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("fileThatDoesntExist.txt doesn't exist.\n", output.getText());
        assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithFilesFlagAndRelativeFilePathThatDoesntExist()
    {
        final Console console = new Console(new String[] { "delete", "-files", "fileThatDoesntExist.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("fileThatDoesntExist.txt doesn't exist.\n", output.getText());
        assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithRootedFilePathThatExists()
    {
        final Console console = new Console(new String[] { "delete", "/fileThatExists.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFile("/fileThatExists.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Deleting file /fileThatExists.txt... Done.\n", output.getText());
        assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithFileFlagAndRootedFilePathThatExists()
    {
        final Console console = new Console(new String[] { "delete", "-file", "/fileThatExists.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFile("/fileThatExists.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Deleting file /fileThatExists.txt... Done.\n", output.getText());
        assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithFilesFlagAndRootedFilePathThatExists()
    {
        final Console console = new Console(new String[] { "delete", "-files", "/fileThatExists.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFile("/fileThatExists.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Deleting file /fileThatExists.txt... Done.\n", output.getText());
        assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithRelativeFilePathThatExists()
    {
        final Console console = new Console(new String[] { "delete", "fileThatExists.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFile("/fileThatExists.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Deleting file /fileThatExists.txt... Done.\n", output.getText());
        assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithFileFlagRelativeFilePathThatExists()
    {
        final Console console = new Console(new String[] { "delete", "-file", "fileThatExists.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFile("/fileThatExists.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Deleting file /fileThatExists.txt... Done.\n", output.getText());
        assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithFilesFlagRelativeFilePathThatExists()
    {
        final Console console = new Console(new String[] { "delete", "-files", "fileThatExists.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFile("/fileThatExists.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Deleting file /fileThatExists.txt... Done.\n", output.getText());
        assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithFolderFlagAndRootedFolderPathThatDoesntExist()
    {
        final Console console = new Console(new String[] { "delete", "-folder", "/fileThatDoesntExist.txt" });
        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        new DeleteAction().run(console);

        assertEquals("/fileThatDoesntExist.txt doesn't exist.\n", output.getText());
        assertFalse(fileSystem.folderExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithFolderFlagAndRelativeFolderPathThatDoesntExist()
    {
        final Console console = new Console(new String[] { "delete", "-folder", "fileThatDoesntExist.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("fileThatDoesntExist.txt doesn't exist.\n", output.getText());
        assertFalse(fileSystem.folderExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithRootedFolderPathThatExists()
    {
        final Console console = new Console(new String[] { "delete", "/fileThatExists.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFolder("/fileThatExists.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Deleting folder /fileThatExists.txt... Done.\n", output.getText());
        assertFalse(fileSystem.folderExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithFolderFlagAndRootedFolderPathThatExists()
    {
        final Console console = new Console(new String[] { "delete", "-folder", "/fileThatExists.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFolder("/fileThatExists.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Deleting folder /fileThatExists.txt... Done.\n", output.getText());
        assertFalse(fileSystem.folderExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithFoldersFlagAndRootedFolderPathThatExists()
    {
        final Console console = new Console(new String[] { "delete", "-folders", "/fileThatExists.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFolder("/fileThatExists.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Deleting folder /fileThatExists.txt... Done.\n", output.getText());
        assertFalse(fileSystem.folderExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithRelativeFolderPathThatExists()
    {
        final Console console = new Console(new String[] { "delete", "fileThatExists.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFolder("/fileThatExists.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Deleting folder /fileThatExists.txt... Done.\n", output.getText());
        assertFalse(fileSystem.folderExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithFolderFlagAndRelativeFolderPathThatExists()
    {
        final Console console = new Console(new String[] { "delete", "-folder", "fileThatExists.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFolder("/fileThatExists.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Deleting folder /fileThatExists.txt... Done.\n", output.getText());
        assertFalse(fileSystem.folderExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithFoldersFlagAndRelativeFolderPathThatExists()
    {
        final Console console = new Console(new String[] { "delete", "-folders", "fileThatExists.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFolder("/fileThatExists.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Deleting folder /fileThatExists.txt... Done.\n", output.getText());
        assertFalse(fileSystem.folderExists("/fileThatDoesntExist.txt"));
    }

    @Test
    public void runWithNoFlagsAndRelativePathToExistingFileAndFolder()
    {
        final Console console = new Console(new String[] { "delete", "A.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFolder("/A.txt");
        fileSystem.createFile("/A.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Can't delete \"A.txt\" because it is a path to both a file and folder. Please specify whether you want to delete the file (-file) or the folder (-folder), or both at the same time.\n", output.getText());
        assertTrue(fileSystem.folderExists("/A.txt"));
        assertTrue(fileSystem.fileExists("/A.txt"));
    }

    @Test
    public void runWithFileFlagAndRelativePathToExistingFileAndFolder()
    {
        final Console console = new Console(new String[] { "delete", "-file", "A.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFolder("/A.txt");
        fileSystem.createFile("/A.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Deleting file /A.txt... Done.\n", output.getText());
        assertTrue(fileSystem.folderExists("/A.txt"));
        assertFalse(fileSystem.fileExists("/A.txt"));
    }

    @Test
    public void runWithFolderFlagAndRelativePathToExistingFileAndFolder()
    {
        final Console console = new Console(new String[] { "delete", "-folder", "A.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFolder("/A.txt");
        fileSystem.createFile("/A.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        assertEquals("Deleting folder /A.txt... Done.\n", output.getText());
        assertFalse(fileSystem.folderExists("/A.txt"));
        assertTrue(fileSystem.fileExists("/A.txt"));
    }

    @Test
    public void runWithFileAndFolderFlagsAndRelativePathToExistingFileAndFolder()
    {
        final Console console = new Console(new String[] { "delete", "-file", "-folder", "A.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFolder("/A.txt");
        fileSystem.createFile("/A.txt");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        final String expectedOutput =
            "Deleting folder /A.txt... Done.\n" +
            "Deleting file /A.txt... Done.\n";
        assertEquals(expectedOutput, output.getText());
        assertFalse(fileSystem.folderExists("/A.txt"));
        assertFalse(fileSystem.fileExists("/A.txt"));
    }

    @Test
    public void runWithFileAndFolderFlagsAndRelativePathThatDoesntExist()
    {
        final Console console = new Console(new String[] { "delete", "-file", "-folder", "A.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        final String expectedOutput = "A.txt doesn't exist.\n";
        assertEquals(expectedOutput, output.getText());
        assertFalse(fileSystem.folderExists("/A.txt"));
        assertFalse(fileSystem.fileExists("/A.txt"));
    }

    @Test
    public void runWithFileThatFailsToDelete()
    {
        final Console console = new Console(new String[] { "delete", "A.txt" });

        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        fileSystem.createRoot("/");
        fileSystem.createFile("/A.txt");
        fileSystem.setFileCanDelete("/A.txt", false);
        console.setFileSystem(fileSystem);
        console.setCurrentFolderPathString("/");

        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
        console.setOutput(output);

        new DeleteAction().run(console);

        final String expectedOutput = "Deleting file /A.txt... Failed.\n";
        assertEquals(expectedOutput, output.getText());
        assertTrue(fileSystem.fileExists("/A.txt"));
    }
}
