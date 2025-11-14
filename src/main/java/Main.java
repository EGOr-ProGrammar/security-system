import controllers.InteractiveModeController;
import controllers.SecuritySystemController;
import controllers.CommandLineController;
import models.TextFileParser;
import models.CSVLogger;
import views.ConsoleView;

public class Main {
    public static void main(String[] args) {
        String defaultFile = "../src/main/java/security_systems.txt";

        TextFileParser textFileParser = new TextFileParser();
        CSVLogger csvLogger = new CSVLogger();
        ConsoleView view = new ConsoleView();

        var systemController = new SecuritySystemController(textFileParser, csvLogger, defaultFile);

        if (args.length > 0) {
            CommandLineController cmdController = new CommandLineController(systemController, view);
            boolean exitAfterProcessing = cmdController.processArgs(args);

            if (exitAfterProcessing) {
                // Команда выполнена, выход из программы
                return;
            }
        }

        // Если processArgs вернул false, запускаем интерактивный режим
        // Интерактивный режим по умолчанию или по флагу -i
        InteractiveModeController interactiveController = new InteractiveModeController(
                systemController,
                view
        );
        interactiveController.run();
    }
}
