# ATM Interface — Java Console Application

**Oasis Infobyte Java Development Internship — Task 3**

A fully functional ATM (Automated Teller Machine) simulator built as a Java console application, demonstrating Object-Oriented Programming principles.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Class Architecture](#class-architecture)
- [Demo Accounts](#demo-accounts)
- [How to Run](#how-to-run)
- [Usage Guide](#usage-guide)
- [Technical Highlights](#technical-highlights)

---

## Overview

This application simulates the core operations of an ATM machine:  
authentication with PIN verification, cash withdrawal, deposit, fund transfer between accounts, and a full transaction history — all running in a Java console with data stored in memory.

**Stack:** Java 17

---

## Features

| Feature | Details |
|---|---|
| Authentication | User ID + PIN verification, blocked after 3 failed attempts |
| Transaction History | Full ordered list with type, amount, date and description |
| Withdrawal | Amount validation + insufficient funds check |
| Deposit | Positive-amount validation + instant balance update |
| Transfer | Recipient lookup, self-transfer prevention, dual-history recording |
| Quit | Clean session end, Scanner properly closed |

---

## Project Structure

```text
JavaDevelopment-Task2-ATMInterface/
├── README.md
└── src/
    └── com/
        └── oasisinfobyte/
            └── atm/
                ├── Main.java         ← Entry point
                ├── Account.java      ← Bank account model
                ├── Transaction.java  ← Immutable transaction record
                ├── Bank.java         ← Account management & transfers
                └── ATM.java          ← Console interface & menu logic
```

---

## Class Architecture

```text
Main
 └── creates Bank + ATM
        │
        Bank ──────────────────────────────────────────┐
         ├── ArrayList<Account>                        │
         │     └── Account (userId, pin, balance)      │
         │            └── ArrayList<Transaction>       │
         │                   └── Transaction           │
         │                     (type, amount,          │
         │                      description, dateTime) │
         └── TransferResult enum                       │
                                                       │
        ATM ────────────────────────────────────────────┘
         ├── authenticate()   → 3 attempts max
         ├── showMenu()       → switch-case options 1–5
         ├── showHistory()
         ├── handleWithdraw()
         ├── handleDeposit()
         └── handleTransfer()
```

### Responsibilities

| Class | Role |
|---|---|
| `Main` | Bootstrap: creates Bank, demo accounts, ATM |
| `Account` | Data model: balance, PIN check, deposit/withdraw, history |
| `Transaction` | Immutable record: type, amount, description, timestamp |
| `Bank` | Service layer: account lookup, authentication, transfer |
| `ATM` | UI layer: console I/O, input validation, menu dispatch |

---

## Demo Accounts

Three accounts are pre-loaded at startup for testing:

| User ID | PIN | Initial Balance |
|---|---|---|
| `U001` | `1234` | $1,500.00 |
| `U002` | `5678` | $3,200.50 |
| `U003` | `0000` | $800.00 |

---

## How to Run

### Prerequisites

- Java 17 or higher installed (`java -version` to check)
- No build tool required

### Compile

```bash
# From the project root directory
javac -d out src/com/oasisinfobyte/atm/Transaction.java \
            src/com/oasisinfobyte/atm/Account.java \
            src/com/oasisinfobyte/atm/Bank.java \
            src/com/oasisinfobyte/atm/ATM.java \
            src/com/oasisinfobyte/atm/Main.java
```

### Run

```bash
java -cp out com.oasisinfobyte.atm.Main
```

---

## Usage Guide

```text
==================================================
OASIS NATIONAL BANK — ATM Interface
Oasis Infobyte — Java Development Task 3
==================================================

--------------------------------------------------
Please insert your card (enter credentials)
--------------------------------------------------
User ID : U001
PIN     : 1234

Welcome, U001!
Current balance: $ 1500.00

------------------------------
MAIN MENU
------------------------------
1. Transaction History
2. Withdraw
3. Deposit
4. Transfer
5. Quit
------------------------------
Your choice:
```

### Menu Options

**1 — Transaction History**  
Displays all operations performed during the session, including type, amount, timestamp, and description.

**2 — Withdraw**  
Enter an amount. The ATM checks it is positive and that the balance is sufficient before proceeding.

**3 — Deposit**  
Enter a positive amount to add to the account balance.

**4 — Transfer**  
Enter the recipient's User ID and an amount. The ATM prevents transfers to non-existent accounts, to the same account, or beyond the available balance.

**5 — Quit**  
Ends the current session cleanly. Optionally start a new session for another user.

---

## Technical Highlights

### `BigDecimal` for monetary amounts

Avoids the floating-point imprecision of `double` (e.g. `0.1 + 0.2 ≠ 0.3`).  
All amounts are parsed as `new BigDecimal(String)` for exact representation.

### Immutable `Transaction` objects

All fields in `Transaction` are `final`. Once a transaction is recorded it can never be altered, ensuring history integrity.

### `TransferResult` enum

Instead of returning a plain `boolean` or `String`, `Bank.transfer()` returns a typed enum value (`SUCCESS`, `INSUFFICIENT_FUNDS`, `RECIPIENT_NOT_FOUND`, `SELF_TRANSFER`), making the ATM's switch-case exhaustive and safe from typos.

### Encapsulation

All class fields are `private`. The PIN is never returned by any getter — only `checkPin(String)` is exposed, returning a `boolean`.

### Unmodifiable transaction list

`Account.getTransactionHistory()` wraps the internal `ArrayList` with `Collections.unmodifiableList()`, preventing external code from tampering with the history.

---

*Developed as part of the Oasis Infobyte Java Development Internship — Task 3.*