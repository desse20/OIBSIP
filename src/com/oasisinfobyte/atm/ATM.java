package com.oasisinfobyte.atm;

import java.util.Scanner;

/**
 * ATM — Console interface layer.
 * Step 2: Main menu with switch-case dispatch.
 */
public class ATM {

    private static final int MAX_ATTEMPTS = 3;

    private final Bank bank;
    private final Scanner scanner;

    public ATM(Bank bank) {
        this.bank    = bank;
        this.scanner = new Scanner(System.in);
    }

    /** Starts the ATM session loop. */
    public void start() {
        printBanner();

        boolean running = true;
        while (running) {
            Account account = authenticate();

            if (account != null) {
                showMenu(account);
            }

            System.out.println();
            System.out.print("  New session? (yes / no): ");
            String answer = scanner.nextLine().trim().toLowerCase();
            if (!answer.equals("yes") && !answer.equals("y")) {
                running = false;
            }
        }

        System.out.println();
        System.out.println("  Thank you for using " + bank.getName() + ". Goodbye!");
        System.out.println("  " + "=".repeat(50));
        scanner.close();
    }

    /** Displays the main menu and dispatches options 1–5. */
    private void showMenu(Account account) {
        boolean sessionActive = true;

        while (sessionActive) {
            printMenu();
            System.out.print("  Your choice: ");
            String input = scanner.nextLine().trim();
            System.out.println();

            switch (input) {
                case "1":
                    System.out.println("  [Transaction History — to be implemented]");
                    break;
                case "2":
                    System.out.println("  [Withdraw — to be implemented]");
                    break;
                case "3":
                    System.out.println("  [Deposit — to be implemented]");
                    break;
                case "4":
                    System.out.println("  [Transfer — to be implemented]");
                    break;
                case "5":
                    sessionActive = false;
                    System.out.println("  Session ended. Card ejected.");
                    break;
                default:
                    System.out.println("  [!] Invalid option. Please enter a number from 1 to 5.");
            }
        }
    }

    private Account authenticate() {
        System.out.println();
        System.out.println("  " + "-".repeat(50));
        System.out.println("  Please insert your card (enter credentials)");
        System.out.println("  " + "-".repeat(50));

        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            if (attempts > 0) {
                int remaining = MAX_ATTEMPTS - attempts;
                System.out.println("  [!] Invalid credentials. "
                        + remaining + " attempt(s) remaining.");
            }

            System.out.print("  User ID : ");
            String userId = scanner.nextLine().trim();

            System.out.print("  PIN     : ");
            String pin = scanner.nextLine().trim();

            Account account = bank.authenticate(userId, pin);

            if (account != null) {
                System.out.println();
                System.out.println("  Welcome, " + account.getUserId() + "!");
                System.out.printf("  Current balance: $ %.2f%n", account.getBalance());
                return account;
            }

            attempts++;
        }

        System.out.println();
        System.out.println("  [X] Card blocked after " + MAX_ATTEMPTS
                + " failed attempts. Please contact your bank.");
        return null;
    }

    private void printBanner() {
        System.out.println();
        System.out.println("  " + "=".repeat(50));
        System.out.println("  " + bank.getName().toUpperCase() + " — ATM Interface");
        System.out.println("  Oasis Infobyte — Java Development Task 3");
        System.out.println("  " + "=".repeat(50));
    }

    private void printMenu() {
        System.out.println();
        System.out.println("  " + "-".repeat(30));
        System.out.println("  MAIN MENU");
        System.out.println("  " + "-".repeat(30));
        System.out.println("  1. Transaction History");
        System.out.println("  2. Withdraw");
        System.out.println("  3. Deposit");
        System.out.println("  4. Transfer");
        System.out.println("  5. Quit");
        System.out.println("  " + "-".repeat(30));
    }
}
