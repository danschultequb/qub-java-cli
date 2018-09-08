package qub;

public class Dependency
{
    private final String publisher;
    private final String project;
    private final String versionRange;

    public Dependency(String publisher, String project, String versionRange)
    {
        PreCondition.assertNotNullAndNotEmpty(publisher, "publisher");
        PreCondition.assertNotNullAndNotEmpty(project, "project");
        PreCondition.assertNotNullAndNotEmpty(versionRange, "versionRange");

        this.publisher = publisher;
        this.project = project;
        this.versionRange = versionRange;
    }

    public String getPublisher()
    {
        return publisher;
    }

    public String getProject()
    {
        return project;
    }

    public String getVersionRange()
    {
        return versionRange;
    }

    public Folder getPublisherFolder(Folder qubFolder)
    {
        PreCondition.assertNotNull(qubFolder, "qubFolder");

        final Folder result = qubFolder.getFolder(publisher).getValue();

        PostCondition.assertNotNull(result, "result");
        PostCondition.assertEqual(publisher, result.getName(), "result.getName()");

        return result;
    }

    public Folder getProjectFolder(Folder qubFolder)
    {
        PreCondition.assertNotNull(qubFolder, "qubFolder");

        final Folder publisherFolder = getPublisherFolder(qubFolder);
        final Folder result = publisherFolder.getFolder(project).getValue();

        PostCondition.assertNotNull(result, "result");
        PostCondition.assertEqual(project, result.getName(), "result.getName()");

        return result;
    }

    @Override
    public String toString()
    {
        return publisher + "/" + project + "/" + versionRange + "/" + project + ".jar";
    }

    public String toString(Folder qubFolder)
    {
        String result = toString();
        if (qubFolder != null)
        {
            result = qubFolder.getPath().concatenateSegment(result).toString();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Dependency && equals((Dependency)obj);
    }

    public boolean equals(Dependency rhs)
    {
        return rhs != null &&
            publisher.equals(rhs.getPublisher()) &&
            project.equals(rhs.getProject()) &&
            versionRange.equals(rhs.getVersionRange());
    }

    @Override
    public int hashCode()
    {
        return publisher.hashCode() ^ project.hashCode() ^ versionRange.hashCode();
    }
}
