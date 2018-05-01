package qub;

/**
 * A QubCLI action that deletes files or folders.
 */
public class DeleteAction implements Action
{
    @Override
    public String getName()
    {
        return "Delete";
    }

    @Override
    public String getDescription()
    {
        return "Delete a provided file or folder.";
    }

    @Override
    public String getArgumentUsage()
    {
        return "[-file] [-folder] <file-folder-or-filter-to-delete> [<additional-file-folder-or-filter-to-delete> ...]";
    }

    @Override
    public void run(final Console console)
    {
        final CommandLine commandLine = console.getCommandLine();

        commandLine.removeAt(0); // Remove the "delete" command line argument.

        final boolean fileFlag = (commandLine.remove("file") != null | commandLine.remove("files") != null);
        final boolean folderFlag = (commandLine.remove("folder") != null | commandLine.remove("folders") != null);

        final Action2<String, FileSystemEntry> deleteEntry = (String entryType, FileSystemEntry entry) ->
        {
            console.write("Deleting " + entryType + " " + entry.getPath().toString() + "...");
            if (entry.delete().getValue())
            {
                console.writeLine(" Done.");
            }
            else
            {
                console.writeLine(" Failed.");
            }
        };
        final Action1<File> deleteFile = (File file) -> deleteEntry.run("file", file);
        final Action1<Folder> deleteFolder = (Folder folder) -> deleteEntry.run("folder", folder);

        final Iterable<Path> pathsToDelete = commandLine.getArguments()
            .map((CommandLineArgument argument) -> Path.parse(argument.toString()));

        final FileSystem fileSystem = console.getFileSystem();
        final Folder currentFolder = console.getCurrentFolder().getValue();
        for (final Path pathToDelete : pathsToDelete)
        {
            File fileToDelete = null;
            Folder folderToDelete = null;
            final boolean pathIsRooted = pathToDelete.isRooted();

            boolean fileExists = false;
            if (fileFlag || !folderFlag)
            {
                fileToDelete = pathIsRooted ? fileSystem.getFile(pathToDelete).getValue() : currentFolder.getFile(pathToDelete).getValue();
                fileExists = fileToDelete.exists().getValue();
            }

            boolean folderExists = false;
            if (folderFlag || !fileFlag)
            {
                folderToDelete = pathIsRooted ? fileSystem.getFolder(pathToDelete).getValue() : currentFolder.getFolder(pathToDelete).getValue();
                folderExists = folderToDelete.exists().getValue();
            }

            if (fileFlag && folderFlag)
            {
                if (folderExists)
                {
                    deleteFolder.run(folderToDelete);
                }

                if (fileExists)
                {
                    deleteFile.run(fileToDelete);
                }

                if (!folderExists && !fileExists)
                {
                    console.writeLine(pathToDelete.toString() + " doesn't exist.");
                }
            }
            else if (fileFlag)
            {
                if (fileExists)
                {
                    deleteFile.run(fileToDelete);
                }
                else
                {
                    console.writeLine(pathToDelete.toString() + " doesn't exist.");
                }
            }
            else if (folderFlag)
            {
                if (folderExists)
                {
                    deleteFolder.run(folderToDelete);
                }
                else
                {
                    console.writeLine(pathToDelete.toString() + " doesn't exist.");
                }
            }
            else
            {
                if (folderExists && fileExists)
                {
                    console.writeLine("Can't delete \"" + pathToDelete + "\" because it is a path to both a file and folder. Please specify whether you want to delete the file (-file) or the folder (-folder), or both at the same time.");
                }
                else if (folderExists)
                {
                    deleteFolder.run(folderToDelete);
                }
                else if (fileExists)
                {
                    deleteFile.run(fileToDelete);
                }
                else
                {
                    console.writeLine(pathToDelete.toString() + " doesn't exist.");
                }
            }
        }
    }
}
