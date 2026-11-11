package com.oasisinfobyte.atm;

import java.math.BigDecimal;

/**
 * Entry point of the ATM Interface application.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("ATM Interface — Oasis Infobyte Task 3");

        // Basic account verification
        Account account = new Account("U001", "1234", new BigDecimal("1500.00"));
        System.out.println("Account created : " + account.getUserId());
        System.out.println("Balance         : $" + account.getBalance());
        System.out.println("PIN check (1234): " + account.checkPin("1234"));
        System.out.println("PIN check (0000): " + account.checkPin("0000"));
    }
}
