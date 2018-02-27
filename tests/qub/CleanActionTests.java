package qub;

public class CleanActionTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(CleanAction.class, () ->
        {
            runner.test("with no project.json file", (Test test) ->
            {
                final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                fileSystem.createRoot("/");
                fileSystem.createFile("/outputs/qub/TestFile.class");

                final InMemoryLineWriteStream output = new InMemoryLineWriteStream();

                final Console console = new Console();
                console.setLineSeparator("\n");
                console.setFileSystem(fileSystem);
                console.setCurrentFolderPathString("/");
                console.setOutput(output);

                final CleanAction clean = new CleanAction();
                clean.run(console);

                test.assertEqual("project.json file doesn't exist in the current folder.\n", output.getText());
                test.assertTrue(fileSystem.fileExists("/outputs/qub/TestFile.class"));
            });

            runner.test("with null outputs folder", (Test test) ->
            {
                final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                fileSystem.createRoot("/");
                fileSystem.createFile("/outputs/qub/TestFile.class");
                fileSystem.createFile("/project.json", "{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"outputs\":false}}");

                final InMemoryLineWriteStream output = new InMemoryLineWriteStream();

                final Console console = new Console();
                console.setLineSeparator("\n");
                console.setFileSystem(fileSystem);
                console.setCurrentFolderPathString("/");
                console.setOutput(output);

                final CleanAction clean = new CleanAction();
                clean.run(console);

                test.assertEqual("Expected \"outputs\" property in \"java\" section to be a non-empty quoted-string.\n", output.getText());
                test.assertTrue(fileSystem.fileExists("/outputs/qub/TestFile.class"));
            });

            runner.test("with default outputs folder", (Test test) ->
            {
                final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                fileSystem.createRoot("/");
                fileSystem.createFile("/outputs/qub/TestFile.class");
                fileSystem.createFile("/project.json", "{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{}}");

                final InMemoryLineWriteStream output = new InMemoryLineWriteStream();

                final Console console = new Console();
                console.setLineSeparator("\n");
                console.setFileSystem(fileSystem);
                console.setCurrentFolderPathString("/");
                console.setOutput(output);

                final CleanAction clean = new CleanAction();
                clean.run(console);

                test.assertEqual("Deleting folder /outputs... Done.\n", output.getText());
                test.assertFalse(fileSystem.folderExists("/outputs"));
            });

            runner.test("with non-default outputs folder", (Test test) ->
            {
                final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                fileSystem.createRoot("/");
                fileSystem.createFile("/outputs/qub/TestFile.class");
                fileSystem.createFile("/spam/qub/TestFile.class");
                fileSystem.createFile("/project.json", "{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"outputs\":\"spam\"}}");

                final InMemoryLineWriteStream output = new InMemoryLineWriteStream();

                final Console console = new Console();
                console.setLineSeparator("\n");
                console.setFileSystem(fileSystem);
                console.setCurrentFolderPathString("/");
                console.setOutput(output);

                final CleanAction clean = new CleanAction();
                clean.run(console);

                test.assertEqual("Deleting folder /spam... Done.\n", output.getText());
                test.assertFalse(fileSystem.folderExists("/spam"));
                test.assertTrue(fileSystem.fileExists("/outputs/qub/TestFile.class"));
            });

            runner.test("with locked outputs folder", (Test test) ->
            {
                final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                fileSystem.createRoot("/");
                fileSystem.createFile("/outputs/qub/TestFile.class");
                fileSystem.setFileCanDelete("/outputs/qub/TestFile.class", false);
                fileSystem.createFile("/project.json", "{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{}}");

                final InMemoryLineWriteStream output = new InMemoryLineWriteStream();

                final Console console = new Console();
                console.setLineSeparator("\n");
                console.setFileSystem(fileSystem);
                console.setCurrentFolderPathString("/");
                console.setOutput(output);

                final CleanAction clean = new CleanAction();
                clean.run(console);

                test.assertEqual("Deleting folder /outputs... Failed.\n", output.getText());
                test.assertTrue(fileSystem.fileExists("/outputs/qub/TestFile.class"));
            });
        });
    }
}
