package qub;

public class DeleteActionTests
{
    private static Console createConsole(String[] commandLineArguments)
    {
        final Console result = new Console(commandLineArguments);
        result.setLineSeparator("\n");
        return result;
    }
    
    public static void test(final TestRunner runner)
    {
        runner.testGroup("DeleteAction", () ->
        {
            runner.testGroup("run(Console)", () ->
            {
                runner.test("with " + Strings.escapeAndQuote(""), test ->
                {
                    final Console console = createConsole(new String[] { "delete" });
                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    new DeleteAction().run(console);

                    test.assertEqual("", output.getText());
                });

                runner.test("with " + Strings.escapeAndQuote("/fileThatDoesntExist.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "/fileThatDoesntExist.txt" });
                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    new DeleteAction().run(console);

                    test.assertEqual("/fileThatDoesntExist.txt doesn't exist.\n", output.getText());
                    test.assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("-flag /fileThatDoesntExist.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-file", "/fileThatDoesntExist.txt" });
                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    new DeleteAction().run(console);

                    test.assertEqual("/fileThatDoesntExist.txt doesn't exist.\n", output.getText());
                    test.assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("-files /fileThatDoesntExist.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-files", "/fileThatDoesntExist.txt" });
                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    new DeleteAction().run(console);

                    test.assertEqual("/fileThatDoesntExist.txt doesn't exist.\n", output.getText());
                    test.assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("fileThatDoesntExist.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "fileThatDoesntExist.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("fileThatDoesntExist.txt doesn't exist.\n", output.getText());
                    test.assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("-file fileThatDoesntExist.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-file", "fileThatDoesntExist.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("fileThatDoesntExist.txt doesn't exist.\n", output.getText());
                    test.assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("-files fileThatDoesntExist.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-files", "fileThatDoesntExist.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("fileThatDoesntExist.txt doesn't exist.\n", output.getText());
                    test.assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("/fileThatExists.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "/fileThatExists.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFile("/fileThatExists.txt");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Deleting file /fileThatExists.txt... Done.\n", output.getText());
                    test.assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("-file /fileThatExists.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-file", "/fileThatExists.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFile("/fileThatExists.txt");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Deleting file /fileThatExists.txt... Done.\n", output.getText());
                    test.assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("-files /fileThatExists.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-files", "/fileThatExists.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFile("/fileThatExists.txt");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Deleting file /fileThatExists.txt... Done.\n", output.getText());
                    test.assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("fileThatExists.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "fileThatExists.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFile("/fileThatExists.txt");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Deleting file /fileThatExists.txt... Done.\n", output.getText());
                    test.assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("-file fileThatExists.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-file", "fileThatExists.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFile("/fileThatExists.txt");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Deleting file /fileThatExists.txt... Done.\n", output.getText());
                    test.assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("-files fileThatExists.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-files", "fileThatExists.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFile("/fileThatExists.txt");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Deleting file /fileThatExists.txt... Done.\n", output.getText());
                    test.assertFalse(fileSystem.fileExists("/fileThatDoesntExist.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("-folder /folderThatDoesntExist"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-folder", "/folderThatDoesntExist" });
                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    new DeleteAction().run(console);

                    test.assertEqual("/folderThatDoesntExist doesn't exist.\n", output.getText());
                    test.assertFalse(fileSystem.folderExists("/folderThatDoesntExist"));
                });

                runner.test("with " + Strings.escapeAndQuote("-folder folderThatDoesntExist"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-folder", "folderThatDoesntExist" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("folderThatDoesntExist doesn't exist.\n", output.getText());
                    test.assertFalse(fileSystem.folderExists("/folderThatDoesntExist"));
                });

                runner.test("with " + Strings.escapeAndQuote("/folderThatExists"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "/folderThatExists" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFolder("/folderThatExists");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Deleting folder /folderThatExists... Done.\n", output.getText());
                    test.assertFalse(fileSystem.folderExists("/folderThatExists"));
                });

                runner.test("with " + Strings.escapeAndQuote("-folder /folderThatExists"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-folder", "/folderThatExists" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFolder("/folderThatExists");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Deleting folder /folderThatExists... Done.\n", output.getText());
                    test.assertFalse(fileSystem.folderExists("/folderThatExists"));
                });

                runner.test("with " + Strings.escapeAndQuote("-folders /folderThatExists"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-folders", "/folderThatExists" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFolder("/folderThatExists");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Deleting folder /folderThatExists... Done.\n", output.getText());
                    test.assertFalse(fileSystem.folderExists("/folderThatExists"));
                });

                runner.test("with " + Strings.escapeAndQuote("folderThatExists"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "folderThatExists" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFolder("/folderThatExists");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Deleting folder /folderThatExists... Done.\n", output.getText());
                    test.assertFalse(fileSystem.folderExists("/folderThatExists"));
                });

                runner.test("with " + Strings.escapeAndQuote("-folder folderThatExists"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-folder", "folderThatExists" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFolder("/folderThatExists");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Deleting folder /folderThatExists... Done.\n", output.getText());
                    test.assertFalse(fileSystem.folderExists("/folderThatExists"));
                });

                runner.test("with " + Strings.escapeAndQuote("-folders folderThatExists"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-folders", "folderThatExists" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFolder("/folderThatExists");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Deleting folder /folderThatExists... Done.\n", output.getText());
                    test.assertFalse(fileSystem.folderExists("/folderThatExists"));
                });

                runner.test("with " + Strings.escapeAndQuote("existingFileAndFolder.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "existingFileAndFolder.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFolder("/existingFileAndFolder.txt");
                    fileSystem.createFile("/existingFileAndFolder.txt");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Can't delete \"existingFileAndFolder.txt\" because it is a path to both a file and folder. Please specify whether you want to delete the file (-file) or the folder (-folder), or both at the same time.\n", output.getText());
                    test.assertTrue(fileSystem.folderExists("/existingFileAndFolder.txt"));
                    test.assertTrue(fileSystem.fileExists("/existingFileAndFolder.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("-file existingFileAndFolder.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-file", "existingFileAndFolder.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFolder("/existingFileAndFolder.txt");
                    fileSystem.createFile("/existingFileAndFolder.txt");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Deleting file /existingFileAndFolder.txt... Done.\n", output.getText());
                    test.assertTrue(fileSystem.folderExists("/existingFileAndFolder.txt"));
                    test.assertFalse(fileSystem.fileExists("/existingFileAndFolder.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("-folder existingFileAndFolder.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-folder", "existingFileAndFolder.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFolder("/existingFileAndFolder.txt");
                    fileSystem.createFile("/existingFileAndFolder.txt");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    test.assertEqual("Deleting folder /existingFileAndFolder.txt... Done.\n", output.getText());
                    test.assertFalse(fileSystem.folderExists("/existingFileAndFolder.txt"));
                    test.assertTrue(fileSystem.fileExists("/existingFileAndFolder.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("-file -folder existingFile.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-file", "-folder", "existingFile.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFile("/existingFile.txt");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    final String expectedOutput =
                        "Deleting file /existingFile.txt... Done.\n";
                    test.assertEqual(expectedOutput, output.getText());
                    test.assertFalse(fileSystem.folderExists("/existingFileAndFolder.txt"));
                    test.assertFalse(fileSystem.fileExists("/existingFileAndFolder.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("-file -folder existingFileAndFolder.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-file", "-folder", "existingFileAndFolder.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFolder("/existingFileAndFolder.txt");
                    fileSystem.createFile("/existingFileAndFolder.txt");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    final String expectedOutput =
                        "Deleting folder /existingFileAndFolder.txt... Done.\n" +
                            "Deleting file /existingFileAndFolder.txt... Done.\n";
                    test.assertEqual(expectedOutput, output.getText());
                    test.assertFalse(fileSystem.folderExists("/existingFileAndFolder.txt"));
                    test.assertFalse(fileSystem.fileExists("/existingFileAndFolder.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("-file -folder nonExistingFileAndFolder.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "-file", "-folder", "nonExistingFileAndFolder.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    final String expectedOutput = "nonExistingFileAndFolder.txt doesn't exist.\n";
                    test.assertEqual(expectedOutput, output.getText());
                    test.assertFalse(fileSystem.folderExists("/nonExistingFileAndFolder.txt"));
                    test.assertFalse(fileSystem.fileExists("/nonExistingFileAndFolder.txt"));
                });

                runner.test("with " + Strings.escapeAndQuote("undeletableExistingFile.txt"), test ->
                {
                    final Console console = createConsole(new String[] { "delete", "undeletableExistingFile.txt" });

                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    fileSystem.createRoot("/");
                    fileSystem.createFile("/undeletableExistingFile.txt");
                    fileSystem.setFileCanDelete("/undeletableExistingFile.txt", false);
                    console.setFileSystem(fileSystem);
                    console.setCurrentFolderPathString("/");

                    final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                    console.setOutput(output);

                    new DeleteAction().run(console);

                    final String expectedOutput = "Deleting file /undeletableExistingFile.txt... Failed.\n";
                    test.assertEqual(expectedOutput, output.getText());
                    test.assertTrue(fileSystem.fileExists("/undeletableExistingFile.txt"));
                });
            });
        });
    }
}
