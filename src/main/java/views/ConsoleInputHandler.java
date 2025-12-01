package views;

import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ConsoleInputHandler {
    private static Scanner scanner = new Scanner(System.in);
    private static volatile boolean isRunning = true;

    public static void shutdown() {
        isRunning = false;
        if (scanner != null) {
            try {
                scanner.close();
            } catch (Exception ignored) {

            }
        }
    }

    public static int getIntInput(String prompt, int min, int max) {
        if (!isRunning) return 0;

        while (isRunning) {
            try {
                System.out.print(prompt);
                if (!scanner.hasNextLine()) {
                    return 0;
                }

                int value = scanner.nextInt();
                scanner.nextLine(); // очистка буфера

                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.println("Введите число от " + min + " до " + max);
                }
            } catch (InputMismatchException e) {
                System.out.println("Ошибка: введите корректное число");
                scanner.nextLine(); // очистка буфера
            } catch (NoSuchElementException e) {
                // Ввод закрыт, выход
                return 0;
            } catch (IllegalStateException e) {
                // Scanner закрыт
                return 0;
            }
        }
        return 0;
    }

    public static String getStringInput(String prompt) {
        if (!isRunning) return "";

        try {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return "";
            }
            return scanner.nextLine().trim();
        } catch (NoSuchElementException | IllegalStateException e) {
            return "";
        }
    }

    public static boolean getConfirmation(String prompt) {
        if (!isRunning) return false;

        try {
            System.out.print(prompt + " (y/n): ");
            if (!scanner.hasNextLine()) {
                return false;
            }
            String input = scanner.nextLine().trim().toLowerCase();
            return input.equals("y") || input.equals("yes") || input.equals("д") || input.equals("да");
        } catch (NoSuchElementException | IllegalStateException e) {
            return false;
        }
    }

    public static int getChoiceFromList(String prompt, List<String> options) {
        if (!isRunning) return 0;

        System.out.println(prompt);
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
        return getIntInput("Выберите вариант: ", 1, options.size()) - 1;
    }

    public static void waitForEnter() {
        if (!isRunning) return;

        try {
            System.out.print("Нажмите Enter для продолжения...");
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        } catch (NoSuchElementException | IllegalStateException ignored) {

        }
    }
}
