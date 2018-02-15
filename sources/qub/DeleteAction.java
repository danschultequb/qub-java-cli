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

        final boolean fileFlag = (commandLine.get("file") != null || commandLine.get("files") != null);
        final boolean folderFlag = (commandLine.get("folder") != null || commandLine.get("folders") != null);

        final Action2<String, FileSystemEntry> deleteEntry = new Action2<String, FileSystemEntry>()
        {
            @Override
            public void run(String entryType, FileSystemEntry entry)
            {
                console.write("Deleting " + entryType + " " + entry.getPath().toString() + "...");
                if (entry.delete())
                {
                    console.writeLine(" Done.");
                }
                else
                {
                    console.writeLine(" Failed.");
                }
            }
        };
        final Action1<File> deleteFile = new Action1<File>()
        {
            @Override
            public void run(File file)
            {
                deleteEntry.run("file", file);
            }
        };
        final Action1<Folder> deleteFolder = new Action1<Folder>()
        {
            @Override
            public void run(Folder folder)
            {
                deleteEntry.run("folder", folder);
            }
        };

        final Iterable<Path> pathsToDelete = commandLine.getArguments()
            .skip(1) // Skip the "delete" action.
            .where(new Function1<CommandLineArgument, Boolean>()
            {
                @Override
                public Boolean run(CommandLineArgument argument)
                {
                    return argument.getName() == null;
                }
            })
            .map(new Function1<CommandLineArgument, Path>()
            {
                @Override
                public Path run(CommandLineArgument argument)
                {
                    return Path.parse(argument.toString());
                }
            });

        final FileSystem fileSystem = console.getFileSystem();
        final Folder currentFolder = console.getCurrentFolder();
        for (final Path pathToDelete : pathsToDelete)
        {
            File fileToDelete = null;
            Folder folderToDelete = null;
            final boolean pathIsRooted = pathToDelete.isRooted();

            boolean fileExists = false;
            if (fileFlag || !folderFlag)
            {
                fileToDelete = pathIsRooted ? fileSystem.getFile(pathToDelete) : currentFolder.getFile(pathToDelete);
                fileExists = fileToDelete.exists();
            }

            boolean folderExists = false;
            if (folderFlag || !fileFlag)
            {
                folderToDelete = pathIsRooted ? fileSystem.getFolder(pathToDelete) : currentFolder.getFolder(pathToDelete);
                folderExists = folderToDelete.exists();
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
