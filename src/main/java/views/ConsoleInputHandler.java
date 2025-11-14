package views;

import config.ConfigManager;

import java.util.List;
import java.util.Scanner;

public class ConsoleInputHandler {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ConfigManager config = ConfigManager.getInstance();

    public static int getIntInput(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println(config.getString("error.prefix") + " " +
                            config.getString("error.input.empty") + " " +
                            config.getString("error.input.empty.number"));
                    continue;
                }

                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.printf(config.getString("error.number.range") + "\n", min, max);
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                System.out.println(config.getString("error.prefix") + " " +
                        config.getString("error.number.format"));
            } catch (Exception e) {
                System.out.println(config.getString("error.unexpected") + " " +
                        e.getMessage() + config.getString("error.try.again"));
            }
        }
    }

    public static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println(config.getString("error.prefix") + " " +
                            config.getString("error.input.empty") + " " +
                            config.getString("error.input.empty.number"));
                    continue;
                }

                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(config.getString("error.prefix") + " " +
                        config.getString("error.number.format"));
            } catch (Exception e) {
                System.out.println(config.getString("error.unexpected") + " " +
                        e.getMessage() + config.getString("error.try.again"));
            }
        }
    }

    public static String getStringInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println(config.getString("error.prefix") + " " +
                            config.getString("error.input.empty") + " " +
                            config.getString("error.input.empty.text"));
                    continue;
                }

                return input;
            } catch (Exception e) {
                System.out.println(config.getString("error.unexpected") + " " +
                        e.getMessage() + config.getString("error.try.again"));
            }
        }
    }

    public static String getStringInput(String prompt, List<String> allowedValues) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println(config.getString("error.prefix") + " " +
                            config.getString("error.input.empty") + " " +
                            config.getString("prompt.enter.values") + " " + allowedValues);
                    continue;
                }

                if (!allowedValues.contains(input)) {
                    System.out.println(config.getString("error.prefix") + " " +
                            config.getString("error.allowed.values") + " " +
                            String.join(", ", allowedValues) + config.getString("error.try.again"));
                    continue;
                }

                return input;
            } catch (Exception e) {
                System.out.println(config.getString("error.unexpected") + " " +
                        e.getMessage() + config.getString("error.try.again"));
            }
        }
    }

    public static boolean getBooleanInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + " " + config.getString("prompt.yes.no") + " ");
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.isEmpty()) {
                    System.out.println(config.getString("error.prefix") + " " +
                            config.getString("error.boolean.format"));
                    continue;
                }

                if (input.equals("да") || input.equals("д") || input.equals("yes") || input.equals("y")) {
                    return true;
                } else if (input.equals("нет") || input.equals("н") || input.equals("no") || input.equals("n")) {
                    return false;
                } else {
                    System.out.println(config.getString("error.prefix") + " " +
                            config.getString("error.boolean.format"));
                }

            } catch (Exception e) {
                System.out.println(config.getString("error.unexpected") + " " +
                        e.getMessage() + config.getString("error.try.again"));
            }
        }
    }

    public static double getDoubleInput(String prompt, double min, double max) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println(config.getString("error.prefix") + " " +
                            config.getString("error.input.empty") + " " +
                            config.getString("error.input.empty.number"));
                    continue;
                }

                double value = Double.parseDouble(input.replace(',', '.'));
                if (value < min || value > max) {
                    System.out.printf(config.getString("error.double.range") + "\n", min, max);
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                System.out.println(config.getString("error.prefix") + " " +
                        config.getString("error.double.format"));
            } catch (Exception e) {
                System.out.println(config.getString("error.unexpected") + " " +
                        e.getMessage() + config.getString("error.try.again"));
            }
        }
    }

    public static boolean getConfirmation(String prompt) {
        while (true) {
            String input = getStringInput(prompt + " " + config.getString("prompt.confirm") + " ").toLowerCase();
            if (input.equals("y") || input.equals("yes") || input.equals("да") || input.equals("д")) {
                return true;
            } else if (input.equals("n") || input.equals("no") || input.equals("нет") || input.equals("н")) {
                return false;
            } else {
                System.out.println(config.getString("error.prefix") + " " +
                        config.getString("error.confirmation.format"));
            }
        }
    }

    public static int getChoiceFromList(String prompt, List<String> options) {
        System.out.println(prompt);
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, options.get(i));
        }

        return getIntInput(config.getString("prompt.choose") + " ", 1, options.size()) - 1;
    }

    public static void waitForEnter() {
        System.out.print(config.getString("prompt.enter.to.continue"));
        scanner.nextLine();
    }

    public static void clearScanner() {
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }

    public static void closeScanner() {
        scanner.close();
    }
}
