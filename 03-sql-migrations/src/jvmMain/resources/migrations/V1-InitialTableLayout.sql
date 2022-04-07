CREATE TYPE currency_code AS ENUM ('GBP', 'EUR');
CREATE TYPE account_type AS ENUM (
    'CURRENT_ACCOUNT',
    'CREDIT_CARD',
    'PREPAID_DEBIT_CARD',
    'FIXED_TERM_LOAN',
    'FIXED_SAVINGS_ACCOUNT',
    'EASY_ACCESS_SAVINGS_ACCOUNT',
    'CASH_ISA',
    'LISA',
    'S&S_INVESTMENT',
    'PENSION',
    'AGREEMENT_BETWEEN_FRIENDS',
    'FINANCE_AGREEMENT',
    'PAYPAL',
    'TRAVEL_CARD'
    );
CREATE TYPE transaction_direction AS ENUM ('CREDIT', 'DEBIT');
CREATE TYPE card_type AS ENUM ('CREDIT', 'DEBIT', 'PREPAID');
CREATE TYPE card_network AS ENUM ('VISA', 'MASTERCARD', 'AMEX');

CREATE TABLE accounts
(
    account_id            UUID PRIMARY KEY,
    account_type          account_type   NOT NULL,
    friendly_name         VARCHAR UNIQUE NOT NULL,
    held_with             VARCHAR        NOT NULL,
    primary_currency_code currency_code  NOT NULL,
    identifier_in_sheet   VARCHAR UNIQUE,
    sort_code             VARCHAR(6),
    account_number        VARCHAR
);

CREATE TABLE cards
(
    card_id             UUID PRIMARY KEY,
    account_id          UUID         NOT NULL REFERENCES accounts (account_id),
    card_number         VARCHAR      NOT NULL,
    network             card_network NOT NULL,
    identifier_in_sheet VARCHAR UNIQUE,
    start_date          DATE,
    end_date            DATE,
    activated           DATE,
    deactivated         DATE,
    UNIQUE (card_number, end_date)
);

CREATE TABLE transactions
(
    transaction_id          UUID PRIMARY KEY,
    opposing_transaction_id UUID REFERENCES transactions (transaction_id) DEFERRABLE INITIALLY DEFERRED,
    account_id              UUID                  NOT NULL REFERENCES accounts (account_id),
    card_id                 UUID REFERENCES cards (card_id),
    amount_minor_units      BIGINT                NOT NULL,
    direction               transaction_direction NOT NULL,
    currency                currency_code         NOT NULL,
    transaction_date        DATE                  NOT NULL,
    transaction_time        TIME,
    account_in_sheet        VARCHAR               NOT NULL,
    category_in_sheet       VARCHAR,
    description_in_sheet    VARCHAR               NOT NULL,
    type_code_in_sheet      VARCHAR               NOT NULL,
    type_in_sheet           VARCHAR               NOT NULL,
    hash_in_sheet           VARCHAR UNIQUE        NOT NULL,
    opposing_hash_in_sheet  VARCHAR UNIQUE
);
