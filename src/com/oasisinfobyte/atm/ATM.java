package com.oasisinfobyte.atm;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * ATM — Console interface layer.
 * Step 3: Banking operations — withdraw, deposit, transfer, history.
 */
public class ATM {

    private static final int MAX_ATTEMPTS = 3;

    private final Bank bank;
    private final Scanner scanner;

    public ATM(Bank bank) {
        this.bank    = bank;
        this.scanner = new Scanner(System.in);
    }

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

    private void showMenu(Account account) {
        boolean sessionActive = true;

        while (sessionActive) {
            printMenu();
            System.out.print("  Your choice: ");
            String input = scanner.nextLine().trim();
            System.out.println();

            switch (input) {
                case "1":
                    showHistory(account);
                    break;
                case "2":
                    handleWithdraw(account);
                    break;
                case "3":
                    handleDeposit(account);
                    break;
                case "4":
                    handleTransfer(account);
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

    private void showHistory(Account account) {
        List<Transaction> history = account.getTransactionHistory();
        System.out.println("  " + "=".repeat(70));
        System.out.println("  TRANSACTION HISTORY — " + account.getUserId());
        System.out.println("  " + "=".repeat(70));

        if (history.isEmpty()) {
            System.out.println("  No transactions found for this account.");
        } else {
            for (Transaction t : history) {
                System.out.println("  " + t);
            }
        }

        System.out.println("  " + "=".repeat(70));
        System.out.printf("  Current balance: $ %.2f%n", account.getBalance());
        System.out.println("  " + "=".repeat(70));
    }

    private void handleWithdraw(Account account) {
        System.out.println("  --- WITHDRAWAL ---");
        System.out.printf("  Available balance: $ %.2f%n", account.getBalance());
        System.out.print("  Amount to withdraw: $ ");
        String input = scanner.nextLine().trim();

        BigDecimal amount;
        try {
            amount = new BigDecimal(input);
        } catch (NumberFormatException e) {
            System.out.println("  [!] Invalid amount.");
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("  [!] Amount must be greater than zero.");
            return;
        }

        if (account.getBalance().compareTo(amount) < 0) {
            System.out.println("  [!] Insufficient Funds.");
            return;
        }

        account.withdraw(amount, "ATM Withdrawal");
        System.out.printf("  [✓] $ %.2f withdrawn successfully.%n", amount);
        System.out.printf("  Remaining balance: $ %.2f%n", account.getBalance());
    }

    private void handleDeposit(Account account) {
        System.out.println("  --- DEPOSIT ---");
        System.out.print("  Amount to deposit: $ ");
        String input = scanner.nextLine().trim();

        BigDecimal amount;
        try {
            amount = new BigDecimal(input);
        } catch (NumberFormatException e) {
            System.out.println("  [!] Invalid amount.");
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("  [!] Amount must be greater than zero.");
            return;
        }

        account.deposit(amount, "ATM Deposit");
        System.out.printf("  [✓] $ %.2f deposited successfully.%n", amount);
        System.out.printf("  New balance: $ %.2f%n", account.getBalance());
    }

    private void handleTransfer(Account account) {
        System.out.println("  --- TRANSFER ---");
        System.out.printf("  Available balance: $ %.2f%n", account.getBalance());
        System.out.print("  Recipient User ID: ");
        String recipientId = scanner.nextLine().trim();

        if (recipientId.isEmpty()) {
            System.out.println("  [!] Recipient ID cannot be empty.");
            return;
        }

        System.out.print("  Amount to transfer: $ ");
        String input = scanner.nextLine().trim();

        BigDecimal amount;
        try {
            amount = new BigDecimal(input);
        } catch (NumberFormatException e) {
            System.out.println("  [!] Invalid amount.");
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("  [!] Amount must be greater than zero.");
            return;
        }

        Bank.TransferResult result = bank.transfer(account, recipientId, amount);

        switch (result) {
            case SUCCESS:
                System.out.printf("  [✓] $ %.2f transferred to %s successfully.%n", amount, recipientId);
                System.out.printf("  Remaining balance: $ %.2f%n", account.getBalance());
                break;
            case RECIPIENT_NOT_FOUND:
                System.out.println("  [!] Account " + recipientId + " not found.");
                break;
            case INSUFFICIENT_FUNDS:
                System.out.println("  [!] Insufficient Funds.");
                break;
            case SELF_TRANSFER:
                System.out.println("  [!] You cannot transfer money to your own account.");
                break;
            default:
                System.out.println("  [!] Transfer failed. Please try again.");
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
