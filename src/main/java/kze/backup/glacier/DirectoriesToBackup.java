package kze.backup.glacier;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
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
        List<Integer> monthsNumbers = expandMonthsRangeToList(monthsRange);
        Logger.info("[%s] Given months numbers to backup: %s", monthsNumbers.size(), monthsNumbers);

        List<Path> monthsPaths = listMonthsDirectories(monthsNumbers);
        Logger.info("[%s] Computed months paths to backup: %s", monthsPaths.size(), monthsPaths.toString());

        return monthsPaths;
    }

    private List<Integer> expandMonthsRangeToList(String monthsRange) {
        String[] split = monthsRange.split("-");
        String rangeStart = split[0];
        String rangeEnd = split[1];
        return IntStream
                .rangeClosed(parseInt(rangeStart), parseInt(rangeEnd))
                .boxed()
                .collect(toList());
    }

    private List<Path> listMonthsDirectories(List<Integer> monthsNumbers) {
        File[] directories = inputDirectoryPath.toFile().listFiles(File::isDirectory);
        String monthsRegexp = prepareMonthsRegexp(monthsNumbers);

        return Stream.of(directories)
                .filter(d -> isMonthDirectory(d, monthsRegexp))
                .filter(this::notYetBackedUp)
                .map(d -> Paths.get(d.toURI()))
                .collect(Collectors.toList());
    }

    private String prepareMonthsRegexp(List<Integer> monthsNumbers) {
        String regexp = monthsNumbers.stream()
                .map(m -> String.format("_%02d", m))
                .reduce((m, n) -> m + "|" + n)
                .map(m -> ".*(" + m + ").*")
                .get();
        Logger.info("Computed months directories regexp %s", regexp);
        return regexp;
    }

    private boolean isMonthDirectory(File dir, String monthsRegexp) {
        return dir.getName().matches(monthsRegexp);
    }

    private boolean notYetBackedUp(File dir) {
        Path backupInfoFilePath = Paths.get(dir.getAbsolutePath(), filenameAwsArchiveInfo);
        boolean notYetBackedUp = Files.notExists(backupInfoFilePath);

        if (!notYetBackedUp) {
            Logger.info("Directory %s was already backed up. Skipping.", dir);
        }
        return notYetBackedUp;
    }
}
