package com.oasisinfobyte.atm;

import java.math.BigDecimal;

/**
 * Entry point of the ATM Interface application.
 */
public class Main {

    public static void main(String[] args) {
        // Bank setup
        Bank bank = new Bank("Oasis National Bank");
        bank.addAccount(new Account("U001", "1234", new BigDecimal("1500.00")));
        bank.addAccount(new Account("U002", "5678", new BigDecimal("3200.50")));

        // Authentication test
        Account auth = bank.authenticate("U001", "1234");
        System.out.println("Auth U001/1234 : " + (auth != null ? "OK" : "FAIL"));

        Account bad = bank.authenticate("U001", "9999");
        System.out.println("Auth U001/9999 : " + (bad != null ? "OK" : "FAIL"));

        // Transfer test
        Bank.TransferResult result = bank.transfer(auth, "U002", new BigDecimal("300.00"));
        System.out.println("Transfer result: " + result);
        System.out.printf("U001 balance   : $%.2f%n", auth.getBalance());

        Account u002 = bank.findAccount("U002");
        System.out.printf("U002 balance   : $%.2f%n", u002.getBalance());
    }
}
