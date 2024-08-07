package osrs.dev.util;

import lombok.Getter;

/**
 * cli argument parsing service
 */
public class OptionsParser {
    @Getter
    private static String rsDumpPath = null;
    @Getter
    private static int world = 6;

    public static void parse(String[] args) {
        for(int i = 0; i < args.length; ++i) {
            switch (args[i]) {
                case "-rsdump":
                    if (++i == args.length) {
                        return;
                    }
                    rsDumpPath = args[i];
                    break;
                case "-world":
                    if (++i == args.length) {
                        return;
                    }
                    world = Integer.parseInt(args[i]) - 300;
                    break;
            }
        }
    }
}