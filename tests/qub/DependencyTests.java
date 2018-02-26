package qub;

public class DependencyTests
{
    public static void test(TestRunner runner)
    {
        runner.testGroup(Dependency.class, () ->
        {
            runner.test("toString()", (Test test) ->
            {
                final Dependency dependency = new Dependency("1", "2", "3");
                test.assertEqual("1/2/3/2.jar", dependency.toString());
            });

            runner.testGroup("toString(Folder)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final Dependency dependency = new Dependency("1", "2", "3");
                    test.assertEqual("1/2/3/2.jar", dependency.toString(null));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final Dependency dependency = new Dependency("1", "2", "3");
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem();
                    final Folder qubFolder = fileSystem.getFolder("/my/qub/folder");
                    test.assertEqual("/my/qub/folder/1/2/3/2.jar", dependency.toString(qubFolder));
                });
            });
        });
    }
}
