CREATE TYPE currency_code AS ENUM ('GBP', 'EUR');
CREATE TYPE account_type AS ENUM (
    'CURRENT_ACCOUNT',
    'CREDIT_CARD',
    'PREPAID_DEBIT_CARD',
    'LOAN',
    'SAVINGS_ACCOUNT',
    'AGREEMENT_BETWEEN_FRIENDS',
    'FINANCIAL_AGREEMENT',
    'PAYPAL'
    );
CREATE TYPE transaction_direction AS ENUM ('CREDIT', 'DEBIT');

CREATE TABLE accounts
(
    account_id            UUID PRIMARY KEY,
    account_type          account_type         NOT NULL,
    friendly_name         VARCHAR UNIQUE       NOT NULL,
    held_with             VARCHAR              NOT NULL,
    primary_currency_code currency_code UNIQUE NOT NULL,
    identifier_in_sheet   VARCHAR UNIQUE       NOT NULL,
    sort_code             VARCHAR(6),
    account_number        VARCHAR,
    CHECK (account_type != 'CURRENT_ACCOUNT' or (sort_code IS NOT NULL and account_number IS NOT NULL))
);

CREATE TABLE transactions
(
    transaction_id          UUID PRIMARY KEY,
    opposing_transaction_id UUID,
    account_id              UUID                  NOT NULL REFERENCES accounts (account_id),
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
