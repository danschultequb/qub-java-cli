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
                    final InMemoryFileSystem fileSystem = new InMemoryFileSystem(test.getMainAsyncRunner());
                    final Result<Folder> qubFolder = fileSystem.getFolder("/my/qub/folder");
                    test.assertSuccess(qubFolder);
                    test.assertEqual("/my/qub/folder/1/2/3/2.jar", dependency.toString(qubFolder.getValue()));
                });
            });

            runner.testGroup("equals(Object)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final Dependency dependency = new Dependency("1", "2", "3");
                    test.assertFalse(dependency.equals((Object)null));
                });

                runner.test("with different type", (Test test) ->
                {
                    final Dependency dependency = new Dependency("1", "2", "3");
                    test.assertFalse(dependency.equals((Object)1235));
                });

                runner.test("with different Dependency", (Test test) ->
                {
                    final Dependency dependency = new Dependency("1", "2", "3");
                    test.assertFalse(dependency.equals((Object)new Dependency("4", "5", "6")));
                });

                runner.test("with equal Dependency", (Test test) ->
                {
                    final Dependency dependency = new Dependency("1", "2", "3");
                    test.assertTrue(dependency.equals((Object)new Dependency("1", "2", "3")));
                });
            });

            runner.testGroup("equals(Dependency)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final Dependency dependency = new Dependency("1", "2", "3");
                    test.assertFalse(dependency.equals((Dependency)null));
                });

                runner.test("with different publisher", (Test test) ->
                {
                    final Dependency dependency = new Dependency("1", "2", "3");
                    test.assertFalse(dependency.equals(new Dependency("a", "2", "3")));
                });

                runner.test("with different project", (Test test) ->
                {
                    final Dependency dependency = new Dependency("1", "2", "3");
                    test.assertFalse(dependency.equals(new Dependency("1", "b", "3")));
                });

                runner.test("with different version", (Test test) ->
                {
                    final Dependency dependency = new Dependency("1", "2", "3");
                    test.assertFalse(dependency.equals(new Dependency("1", "2", "c")));
                });

                runner.test("with equal Dependency", (Test test) ->
                {
                    final Dependency dependency = new Dependency("1", "2", "3");
                    test.assertTrue(dependency.equals(new Dependency("1", "2", "3")));
                });
            });
        });
    }
}
