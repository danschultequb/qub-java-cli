package qub;

public class CleanActionTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(CleanAction.class, () ->
        {
            runner.test("with no project.json file", (Test test) ->
            {
                final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getMainAsyncRunner());
                fileSystem.createRoot("/");
                fileSystem.createFile("/outputs/qub/TestFile.class");

                final InMemoryLineStream output = new InMemoryLineStream();

                final Console console = new Console();
                console.setLineSeparator("\n");
                console.setFileSystem(fileSystem);
                console.setCurrentFolderPathString("/");
                console.setOutput(output);

                final CleanAction clean = new CleanAction();
                clean.run(console);

                test.assertSuccess("project.json file doesn't exist in the current folder.\n", output.getText());
                test.assertSuccess(true, fileSystem.fileExists("/outputs/qub/TestFile.class"));
            });

            runner.test("with null outputs folder", (Test test) ->
            {
                final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getMainAsyncRunner());
                fileSystem.createRoot("/");
                fileSystem.createFile("/outputs/qub/TestFile.class");
                fileSystem.setFileContent("/project.json", CharacterEncoding.UTF_8.encode("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"outputs\":null}}").getValue());

                final InMemoryLineStream output = new InMemoryLineStream();

                final Console console = new Console();
                console.setLineSeparator("\n");
                console.setFileSystem(fileSystem);
                console.setCurrentFolderPathString("/");
                console.setOutput(output);

                final CleanAction clean = new CleanAction();
                clean.run(console);

                test.assertSuccess("Expected \"outputs\" property in \"java\" section to be a non-empty quoted-string.\n", output.getText());
                test.assertSuccess(true, fileSystem.fileExists("/outputs/qub/TestFile.class"));
            });

            runner.test("with default outputs folder", (Test test) ->
            {
                final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getMainAsyncRunner());
                fileSystem.createRoot("/");
                fileSystem.createFile("/outputs/qub/TestFile.class");
                fileSystem.setFileContent("/project.json", CharacterEncoding.UTF_8.encode("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{}}").getValue());

                final InMemoryLineStream output = new InMemoryLineStream();

                final Console console = new Console();
                console.setLineSeparator("\n");
                console.setFileSystem(fileSystem);
                console.setCurrentFolderPathString("/");
                console.setOutput(output);

                final CleanAction clean = new CleanAction();
                clean.run(console);

                test.assertSuccess("Deleting folder /outputs... Done.\n", output.getText());
                test.assertSuccess(false, fileSystem.folderExists("/outputs"));
            });

            runner.test("with non-default outputs folder", (Test test) ->
            {
                final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getMainAsyncRunner());
                fileSystem.createRoot("/");
                fileSystem.createFile("/outputs/qub/TestFile.class");
                fileSystem.createFile("/spam/qub/TestFile.class");
                fileSystem.setFileContent("/project.json", CharacterEncoding.UTF_8.encode("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"outputs\":\"spam\"}}").getValue());

                final InMemoryLineStream output = new InMemoryLineStream();

                final Console console = new Console();
                console.setLineSeparator("\n");
                console.setFileSystem(fileSystem);
                console.setCurrentFolderPathString("/");
                console.setOutput(output);

                final CleanAction clean = new CleanAction();
                clean.run(console);

                test.assertSuccess("Deleting folder /spam... Done.\n", output.getText());
                test.assertSuccess(false, fileSystem.folderExists("/spam"));
                test.assertSuccess(true, fileSystem.fileExists("/outputs/qub/TestFile.class"));
            });

            runner.test("with locked outputs folder", (Test test) ->
            {
                final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getMainAsyncRunner());
                fileSystem.createRoot("/");
                fileSystem.createFile("/outputs/qub/TestFile.class");
                fileSystem.setFileCanDelete("/outputs/qub/TestFile.class", false);
                fileSystem.setFileContent("/project.json", CharacterEncoding.UTF_8.encode("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{}}").getValue());

                final InMemoryLineStream output = new InMemoryLineStream();

                final Console console = new Console();
                console.setLineSeparator("\n");
                console.setFileSystem(fileSystem);
                console.setCurrentFolderPathString("/");
                console.setOutput(output);

                final CleanAction clean = new CleanAction();
                clean.run(console);

                test.assertSuccess("Deleting folder /outputs... Failed.\n", output.getText());
                test.assertSuccess(true, fileSystem.fileExists("/outputs/qub/TestFile.class"));
            });
        });
    }
}
