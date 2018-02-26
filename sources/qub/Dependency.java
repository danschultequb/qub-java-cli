package qub;

public class Dependency
{
    private final String publisher;
    private final String project;
    private final String version;

    public Dependency(String publisher, String project, String version)
    {
        this.publisher = publisher;
        this.project = project;
        this.version = version;
    }

    public String getPublisher()
    {
        return publisher;
    }

    public String getProject()
    {
        return project;
    }

    public String getVersion()
    {
        return version;
    }

    @Override
    public String toString()
    {
        return publisher + "/" + project + "/" + version + "/" + project + ".jar";
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
            version.equals(rhs.getVersion());
    }
}
