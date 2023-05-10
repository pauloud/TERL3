public class ParseArguments {

    public static String parseRulesPath(String[] args) {
        return args.length > 0 ? args[0] : null;
    }

    public static String parseToForgetPath(String[] args) {
        String toForgetPath = args.length == 2 ? args[1] : null;
        for (int i = 1; i < args.length; i++) {
            if (args[i] == "-f" && args.length > i + 1) {
                toForgetPath = args[i + 1];
                break;


            }
        }
        return toForgetPath;
    }
    public static String parseOutputPath(String [] args) {
        String outputPath = args.length == 3 ? args[2] : null;
        for (int i = 1; i < args.length; i++) {
            if (args[i] == "-o" && args.length > i + 1) {
                outputPath = args[i + 1];
                break;

            }
        }
        return outputPath;
    }
}
