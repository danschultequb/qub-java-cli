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
}
