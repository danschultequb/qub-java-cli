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

                final Action3<String,Action2<Test,ProjectJson>,String> parseTest = (String projectJsonText, Action2<Test,ProjectJson> projectJsonAssertions, String expectedOutput) ->
                {
                    runner.test("with " + runner.escapeAndQuote(projectJsonText), (Test test) ->
                    {
                        try (final Console console = new Console())
                        {
                            console.setLineSeparator("\n");

                            final InMemoryLineWriteStream output = new InMemoryLineWriteStream();
                            console.setOutput(output);

                            final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                            fileSystem.createRoot("/");
                            fileSystem.createFile("/project.json", projectJsonText);
                            console.setFileSystem(fileSystem);
                            console.setCurrentFolderPathString("/");

                            final ProjectJson projectJson = ProjectJson.parse(console);
                            test.assertNotNull(projectJson);
                            projectJsonAssertions.run(test, projectJson);

                            test.assertEqual(expectedOutput, output.getText());
                        }
                    });
                };

                parseTest.run("",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNull(projectJson.getRootObject());
                        test.assertNull(projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "project.json root segment must be a JSON object.\n");

                parseTest.run("[]",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNull(projectJson.getRootObject());
                        test.assertNull(projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "project.json root segment must be a JSON object.\n");

                parseTest.run("{}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertNull(projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "A \"publisher\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                    "A \"project\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                    "A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                    "project.json root object must contain a \"java\" property.\n");

                parseTest.run("{\"publisher\":\"a\"}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "A \"project\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                    "A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                    "project.json root object must contain a \"java\" property.\n");

                parseTest.run("{\"publisher\":false}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertNull(projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "The \"publisher\" property in the rootObject object of the project.json file must be a non-empty quoted-string.\n" +
                    "A \"project\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                    "A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                    "project.json root object must contain a \"java\" property.\n");

                parseTest.run("{\"publisher\":\"\"}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertNull(projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "The \"publisher\" property in the rootObject object of the project.json file must be a non-empty quoted-string.\n" +
                    "A \"project\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                    "A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                    "project.json root object must contain a \"java\" property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":true}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "The \"project\" property in the rootObject object of the project.json file must be a non-empty quoted-string.\n" +
                    "A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                    "project.json root object must contain a \"java\" property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"\"}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "The \"project\" property in the rootObject object of the project.json file must be a non-empty quoted-string.\n" +
                    "A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                    "project.json root object must contain a \"java\" property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":true}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "The \"project\" property in the rootObject object of the project.json file must be a non-empty quoted-string.\n" +
                    "A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                    "project.json root object must contain a \"java\" property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"\"}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertNull(projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "The \"project\" property in the rootObject object of the project.json file must be a non-empty quoted-string.\n" +
                    "A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                    "project.json root object must contain a \"java\" property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\"}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "A \"version\" quoted-string property must be specified in the rootObject object of the project.json file.\n" +
                    "project.json root object must contain a \"java\" property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":1}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "The \"version\" property in the rootObject object of the project.json file must be a non-empty quoted-string.\n" +
                    "project.json root object must contain a \"java\" property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"\"}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertNull(projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "The \"version\" property in the rootObject object of the project.json file must be a non-empty quoted-string.\n" +
                    "project.json root object must contain a \"java\" property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\"}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "project.json root object must contain a \"java\" property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":false}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "\"java\" property must be a JSON object.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"mainClass\":false}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "The \"mainClass\" property in the java object of the project.json file must be a non-empty quoted-string.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"mainClass\":\"\"}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "The \"mainClass\" property in the java object of the project.json file must be a non-empty quoted-string.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"mainClass\":\"qub.Test\"}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertEqual("qub.Test", projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"sources\":false}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "Expected \"sources\" to not exist, be a non-empty quoted-string property, or be an object property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"sources\":\"\"}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "Expected \"sources\" to not exist, be a non-empty quoted-string property, or be an object property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"sources\":\"spam\"}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/spam", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"sources\":{}}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNotNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"sources\":{\"folder\":false}}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNotNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "Expected \"folder\" property in the \"sources\" section to be a non-empty quoted-string property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"sources\":{\"folder\":\"\"}}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNotNull(projectJson.getJavaSourcesObject());
                        test.assertNull(projectJson.getJavaSourcesFolder());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "Expected \"folder\" property in \"sources\" section to not exist, be a non-empty quoted-string property, or be an object property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"sources\":{\"folder\":\"cats\"}}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNotNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/cats", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"sources\":{\"version\":7}}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNotNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "Expected \"version\" property in \"sources\" section to be a non-empty quoted-string property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"sources\":{\"version\":\"\"}}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNotNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "Expected \"version\" property in \"sources\" section to be a non-empty quoted-string property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"sources\":{\"version\":\"801\"}}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNotNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertEqual("801", projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"tests\":false}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "Expected \"tests\" to not exist, be a non-empty quoted-string property, or be an object property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"tests\":\"\"}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "Expected \"tests\" to not exist, be a non-empty quoted-string property, or be an object property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"tests\":\"fish\"}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/fish", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"tests\":{}}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNotNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"tests\":{\"folder\":59}}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNotNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "Expected \"folder\" property in the \"tests\" section to be a non-empty quoted-string property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"tests\":{\"folder\":\"\"}}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNotNull(projectJson.getJavaTestsObject());
                        test.assertNull(projectJson.getJavaTestsFolder());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "Expected \"folder\" property in \"tests\" section to be a non-empty quoted-string property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"tests\":{\"folder\":\"ugh\"}}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNotNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/ugh", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"tests\":{\"version\":[]}}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNotNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "Expected \"version\" property in \"tests\" section to be a non-empty quoted-string property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"tests\":{\"version\":\"\"}}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNotNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "Expected \"version\" property in \"tests\" section to be a non-empty quoted-string property.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"tests\":{\"version\":\"blah\"}}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNotNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertEqual("blah", projectJson.getJavaTestsVersion());
                        test.assertEqual("/outputs", projectJson.getJavaOutputsFolder().toString());
                    },
                    "");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"outputs\":false}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "Expected \"outputs\" property in \"java\" section to be a non-empty quoted-string.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"outputs\":\"\"}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertNull(projectJson.getJavaOutputsFolder());
                    },
                    "Expected \"outputs\" property in \"java\" section to be a non-empty quoted-string.\n");

                parseTest.run("{\"publisher\":\"a\",\"project\":\"b\",\"version\":\"c\",\"java\":{\"outputs\":\"out\"}}",
                    (Test test, ProjectJson projectJson) ->
                    {
                        test.assertNotNull(projectJson.getRootObject());
                        test.assertEqual("a", projectJson.getPublisher());
                        test.assertEqual("b", projectJson.getProject());
                        test.assertEqual("c", projectJson.getVersion());
                        test.assertNotNull(projectJson.getJavaObject());
                        test.assertNull(projectJson.getMainClass());
                        test.assertNull(projectJson.getJavaSourcesObject());
                        test.assertEqual("/sources", projectJson.getJavaSourcesFolder().toString());
                        test.assertNull(projectJson.getJavaSourcesVersion());
                        test.assertNull(projectJson.getJavaTestsObject());
                        test.assertEqual("/tests", projectJson.getJavaTestsFolder().toString());
                        test.assertNull(projectJson.getJavaTestsVersion());
                        test.assertEqual("/out", projectJson.getJavaOutputsFolder().toString());
                    },
                    "");
            });
        });
    }
}
