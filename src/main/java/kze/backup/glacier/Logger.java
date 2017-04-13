package kze.backup.glacier;

public class Logger {

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

    private static String formatMessage(String level, Object message, Object... ars) {
        String levelPrefix = String.format("[%s] ", level);
        return String.format(levelPrefix + message, ars);
    }

}
