package qub;

public class ProjectJsonTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(ProjectJson.class, () ->
        {
            runner.testGroup("parse()", () ->
            {
                runner.test("with null Console", (Test test) ->
                {
                    test.assertNull(ProjectJson.parse(null));
                });

                runner.test("with no project.json file", (Test test) ->
                {
                    try (final Console console = new Console())
                    {
                        console.setLineSeparator("\n");

                        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                        console.setOutput(output);

                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                        fileSystem.createRoot("/");
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        test.assertNull(ProjectJson.parse(console));

                        test.assertEqual(
                            "project.json file doesn't exist in the current folder.\n",
                            output.getText());
                    }
                });

                runner.test("with \"\"", (Test test) ->
                {
                    try (final Console console = new Console())
                    {
                        console.setLineSeparator("\n");

                        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                        console.setOutput(output);

                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                        fileSystem.createRoot("/");
                        fileSystem.createFile("/project.json");
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        final ProjectJson projectJson = ProjectJson.parse(console);
                        test.assertNotNull(projectJson);
                        test.assertNull(projectJson.getRootObject());
                        test.assertNull(projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());

                        test.assertEqual(
                            "project.json root segment must be a JSON object.\n",
                            output.getText());
                    }
                });

                runner.test("with \"[]\"", (Test test) ->
                {
                    try (final Console console = new Console())
                    {
                        console.setLineSeparator("\n");

                        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                        console.setOutput(output);

                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                        fileSystem.createRoot("/");
                        fileSystem.createFile("/project.json", "[]");
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        final ProjectJson projectJson = ProjectJson.parse(console);
                        test.assertNotNull(projectJson);
                        test.assertNull(projectJson.getRootObject());
                        test.assertNull(projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());

                        test.assertEqual(
                            "project.json root segment must be a JSON object.\n",
                            output.getText());
                    }
                });

                runner.test("with \"{}\"", (Test test) ->
                {
                    try (final Console console = new Console())
                    {
                        console.setLineSeparator("\n");

                        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                        console.setOutput(output);

                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                        fileSystem.createRoot("/");
                        fileSystem.createFile("/project.json", "{}");
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        final ProjectJson projectJson = ProjectJson.parse(console);
                        test.assertNotNull(projectJson);
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertNull(projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());

                        test.assertEqual(
                            "A \"publisher\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                            "A \"project\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                            "A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                            "project.json root object must contain a \"java\" property.\n",
                            output.getText());
                    }
                });

                runner.test("with " + runner.escapeAndQuote("{\"publisher\":\"a\""), (Test test) ->
                {
                    try (final Console console = new Console())
                    {
                        console.setLineSeparator("\n");

                        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                        console.setOutput(output);

                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                        fileSystem.createRoot("/");
                        fileSystem.createFile("/project.json", "{\"publisher\":\"a\"}");
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        final ProjectJson projectJson = ProjectJson.parse(console);
                        test.assertNotNull(projectJson);
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());

                        test.assertEqual(
                            "A \"project\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                            "A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                            "project.json root object must contain a \"java\" property.\n",
                            output.getText());
                    }
                });

                runner.test("with " + runner.escapeAndQuote("{\"publisher\":false"), (Test test) ->
                {
                    try (final Console console = new Console())
                    {
                        console.setLineSeparator("\n");

                        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                        console.setOutput(output);

                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                        fileSystem.createRoot("/");
                        fileSystem.createFile("/project.json", "{\"publisher\":false}");
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        final ProjectJson projectJson = ProjectJson.parse(console);
                        test.assertNotNull(projectJson);
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertNull(projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());

                        test.assertEqual(
                            "The \"publisher\" property in the rootObject object of the project.json file must be a non-empty quoted-string.\n" +
                            "A \"project\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                            "A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                            "project.json root object must contain a \"java\" property.\n",
                            output.getText());
                    }
                });

                runner.test("with " + runner.escapeAndQuote("{\"publisher\":\"\""), (Test test) ->
                {
                    try (final Console console = new Console())
                    {
                        console.setLineSeparator("\n");

                        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                        console.setOutput(output);

                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                        fileSystem.createRoot("/");
                        fileSystem.createFile("/project.json", "{\"publisher\":\"\"}");
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        final ProjectJson projectJson = ProjectJson.parse(console);
                        test.assertNotNull(projectJson);
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertNull(projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());

                        test.assertEqual(
                            "The \"publisher\" property in the rootObject object of the project.json file must be a non-empty quoted-string.\n" +
                                "A \"project\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                                "A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                                "project.json root object must contain a \"java\" property.\n",
                            output.getText());
                    }
                });

                runner.test("with " + runner.escapeAndQuote("{\"publisher\":\"a\",\"project\":true"), (Test test) ->
                {
                    try (final Console console = new Console())
                    {
                        console.setLineSeparator("\n");

                        final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                        console.setOutput(output);

                        final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                        fileSystem.createRoot("/");
                        fileSystem.createFile("/project.json", "{\"publisher\":\"a\",\"project\":true}");
                        console.setFileSystem(fileSystem);
                        console.setCurrentFolderPathString("/");

                        final ProjectJson projectJson = ProjectJson.parse(console);
                        test.assertNotNull(projectJson);
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());

                        test.assertEqual(
                            "The \"project\" property in the rootObject object of the project.json file must be a non-empty quoted-string.\n" +
                            "A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                            "project.json root object must contain a \"java\" property.\n",
                            output.getText());
                    }
                });
            });
        });
    }
}
