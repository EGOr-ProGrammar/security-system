package views;

import java.util.List;
import java.util.Scanner;

/**
 * Обрабатывает пользовательский ввод.
 */
public class ConsoleInputHandler {
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Получение целого числа с проверкой диапазона
     */
    public static int getIntInput(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Ошибка: Ввод не может быть пустым. Пожалуйста, введите число.");
                    continue;
                }

                int value = Integer.parseInt(input);

                if (value < min || value > max) {
                    System.out.printf("Ошибка: Число должно быть в диапазоне от %d до %d. Попробуйте снова.\n", min, max);
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите корректное целое число. Попробуйте снова.");
            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage() + ". Попробуйте снова.");
            }
        }
    }

    /**
     * Получение целого числа без ограничения диапазона
     */
    public static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Ошибка: Ввод не может быть пустым. Пожалуйста, введите число.");
                    continue;
                }

                return Integer.parseInt(input);

            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите корректное целое число. Попробуйте снова.");
            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage() + ". Попробуйте снова.");
            }
        }
    }

    /**
     * Получение строки с проверкой на пустоту
     */
    public static String getStringInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Ошибка: Ввод не может быть пустым. Пожалуйста, введите текст.");
                    continue;
                }

                return input;

            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage() + ". Попробуйте снова.");
            }
        }
    }

    /**
     * Получение строки с допустимыми значениями
     */
    public static String getStringInput(String prompt, List<String> allowedValues) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Ошибка: Ввод не может быть пустым. Пожалуйста, введите одно из значений: " + allowedValues);
                    continue;
                }

                if (!allowedValues.contains(input)) {
                    System.out.println("Ошибка: Допустимые значения: " + String.join(", ", allowedValues) + ". Попробуйте снова.");
                    continue;
                }

                return input;

            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage() + ". Попробуйте снова.");
            }
        }
    }

    /**
     * Получение булевого значения (да/нет)
     */
    public static boolean getBooleanInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt + " (да/нет): ");
                String input = scanner.nextLine().trim().toLowerCase();

                if (input.isEmpty()) {
                    System.out.println("Ошибка: Ввод не может быть пустым. Пожалуйста, введите 'да' или 'нет'.");
                    continue;
                }

                if (input.equals("да") || input.equals("д") || input.equals("yes") || input.equals("y")) {
                    return true;
                } else if (input.equals("нет") || input.equals("н") || input.equals("no") || input.equals("n")) {
                    return false;
                } else {
                    System.out.println("Ошибка: Пожалуйста, введите 'да' или 'нет'.");
                }

            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage() + ". Попробуйте снова.");
            }
        }
    }

    /**
     * Получение числа с плавающей точкой
     */
    public static double getDoubleInput(String prompt, double min, double max) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Ошибка: Ввод не может быть пустым. Пожалуйста, введите число.");
                    continue;
                }

                double value = Double.parseDouble(input.replace(',', '.'));

                if (value < min || value > max) {
                    System.out.printf("Ошибка: Число должно быть в диапазоне от %.2f до %.2f. Попробуйте снова.\n", min, max);
                    continue;
                }

                return value;

            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите корректное число. Попробуйте снова.");
            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage() + ". Попробуйте снова.");
            }
        }
    }

    /**
     * Подтверждение действия
     */
    public static boolean getConfirmation(String prompt) {
        while (true) {
            String input = getStringInput(prompt + " (y/n): ").toLowerCase();
            if (input.equals("y") || input.equals("yes") || input.equals("да") || input.equals("д")) {
                return true;
            } else if (input.equals("n") || input.equals("no") || input.equals("нет") || input.equals("н")) {
                return false;
            } else {
                System.out.println("Ошибка: Пожалуйста, введите 'y' для подтверждения или 'n' для отмены.");
            }
        }
    }

    /**
     * Выбор из списка вариантов
     */
    public static int getChoiceFromList(String prompt, List<String> options) {
        System.out.println(prompt);
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, options.get(i));
        }

        return getIntInput("Выберите вариант: ", 1, options.size()) - 1;
    }

    /**
     * Пауза с ожиданием ввода пользователя
     */
    public static void waitForEnter() {
        System.out.print("Нажмите Enter для продолжения...");
        scanner.nextLine();
    }

    /**
     * Очистка сканера (на случай проблем с вводом)
     */
    public static void clearScanner() {
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }

    /**
     * Закрытие сканера
     */
    public static void closeScanner() {
        scanner.close();
    }
}