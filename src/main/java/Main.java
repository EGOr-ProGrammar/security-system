import config.ConfigManager;
import controllers.InteractiveModeController;
import controllers.SecuritySystemController;
import controllers.CommandLineController;
import models.TextFileParser;
import models.CSVLogger;
import views.ConsoleView;

public class Main {
    public static void main(String[] args) {
        ConfigManager config = ConfigManager.getInstance();

        String defaultFile = config.getString("file.default.path");
        TextFileParser textFileParser = new TextFileParser();
        CSVLogger csvLogger = new CSVLogger();
        ConsoleView view = new ConsoleView();

        var systemController = new SecuritySystemController(textFileParser, csvLogger, defaultFile);

        if (args.length > 0) {
            CommandLineController cmdController = new CommandLineController(systemController, view);
            boolean exitAfterProcessing = cmdController.processArgs(args);
            if (exitAfterProcessing) {
                return;
            }
        }

        InteractiveModeController interactiveController = new InteractiveModeController(systemController, view);
        interactiveController.run();
    }
}
