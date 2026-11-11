package com.oasisinfobyte.atm;

import java.math.BigDecimal;

/**
 * Entry point of the ATM Interface application.
 *
 * Responsibilities:
 *  - Create the Bank instance.
 *  - Register demo accounts with initial balances.
 *  - Create the ATM and start the session.
 *
 * All data lives in memory — no database, no files.
 *
 * Demo accounts:
 *   U001 / PIN 1234 — balance $1,500.00
 *   U002 / PIN 5678 — balance $3,200.50
 *   U003 / PIN 0000 — balance $800.00
 */
public class Main {

    public static void main(String[] args) {

        // 1. Create the bank
        Bank bank = new Bank("Oasis National Bank");

        // 2. Register demo accounts
        bank.addAccount(new Account("U001", "1234", new BigDecimal("1500.00")));
        bank.addAccount(new Account("U002", "5678", new BigDecimal("3200.50")));
        bank.addAccount(new Account("U003", "0000", new BigDecimal("800.00")));

        // 3. Create the ATM connected to the bank and start the session
        ATM atm = new ATM(bank);
        atm.start();
    }
}
