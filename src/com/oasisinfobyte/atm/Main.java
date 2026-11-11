package com.oasisinfobyte.atm;

import java.math.BigDecimal;

/**
 * Entry point of the ATM Interface application.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("ATM Interface — Oasis Infobyte Task 3");

        // Transaction history test
        Account account = new Account("U001", "1234", new BigDecimal("1500.00"));
        account.deposit(new BigDecimal("500.00"), "ATM Deposit");
        account.withdraw(new BigDecimal("200.00"), "ATM Withdrawal");

        System.out.println("--- Transaction History ---");
        for (Transaction t : account.getTransactionHistory()) {
            System.out.println(t);
        }
        System.out.printf("Balance: $%.2f%n", account.getBalance());
    }
}
