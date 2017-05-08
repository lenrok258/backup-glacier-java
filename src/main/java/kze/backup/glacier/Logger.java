package kze.backup.glacier;

import java.util.Date;

public class Logger {

    private static final String PROGRESS_TILE = ".";

    public static void info(Object message, Object... args) {
        String messageFormatted = formatMessage("INFO", message, args);
        System.out.println(messageFormatted);
    }

    public static void error(Object message, Object... args) {
        String messageFormatted = formatMessage("ERROR", message, args);
        System.err.println(messageFormatted);
    }

    public static void error(Object message, Throwable e, Object... args) {
        error(message, args);
        e.printStackTrace();
    }

    public static void progress(String message, long current, long total, Object... args) {
        float percentage = (float) current / (float) total * 100;
        String messageFormatted = formatMessage("PROGRESS", message, args);
        messageFormatted += String.format(" (%.2f%% :: %s of %s)", percentage, current, total);
        System.out.print("\r" + messageFormatted);
    }

    public static void progressComplete() {
        System.out.println("");
    }

    private static String formatMessage(String level, Object message, Object... ars) {
        String levelPrefixAndTime = String.format("[%1$tF %1$tT] [%2$s] ", new Date(), level);
        return String.format(levelPrefixAndTime + message, ars);
    }

/*    public static void main(String[] args) throws InterruptedException {
        Logger.info("Start");
        for (int i = 0; i < 10; i++) {
            Logger.progress("Doing stuff:", 12 + i, 21);
            Thread.sleep(500);
        }
        progressComplete();
        Logger.info("stop");
    }*/

}
