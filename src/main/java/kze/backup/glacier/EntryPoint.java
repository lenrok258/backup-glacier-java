package kze.backup.glacier;

import java.util.Arrays;

public class EntryPoint {

    public static void main(String[] args) {

        System.out.println("\n\nStarted\n\n");

        EntryPointArgumentParser arguments = new EntryPointArgumentParser(args);


        System.out.println(Arrays.asList(args));

        System.out.println("\n\nFinished\n\n");

    }
}
