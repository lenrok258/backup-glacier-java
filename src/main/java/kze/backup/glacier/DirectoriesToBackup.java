package kze.backup.glacier;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DirectoriesToBackup {

    private Path inputDirectoryPath;
    private String monthsRange;
    private String filenameAwsArchiveInfo;

    public DirectoriesToBackup(Path inputDirectoryPath, String monthsRange, String filenameAwsArchiveInfo) {
        this.inputDirectoryPath = inputDirectoryPath;
        this.monthsRange = monthsRange;
        this.filenameAwsArchiveInfo = filenameAwsArchiveInfo;
    }

    public List<Path> getPathsList() {
        List<Integer> monthsList = expandMonthsRange(monthsRange);
        Logger.info("Months list to backup: %s", monthsList.toString());



        return null;
    }

    private List<Integer> expandMonthsRange(String monthsRange) {
        String[] split = monthsRange.split("-");
        String rangeStart = split[0];
        String rangeEnd = split[1];
        return IntStream
                .rangeClosed(parseInt(rangeStart), parseInt(rangeEnd))
                .boxed()
                .collect(toList());
    }

    /*private Path listMonthsDirectories(List<Integer> monthsList) {
        File[] directories = inputDirectoryPath.toFile().listFiles(File::isDirectory);
        Stream.of(directories)
                .
    }*/

}
