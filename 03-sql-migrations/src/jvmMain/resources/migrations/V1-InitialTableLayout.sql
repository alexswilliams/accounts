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
    'MANAGED_INVESTMENT',
    'PENSION',
    'AGREEMENT_BETWEEN_FRIENDS',
    'FINANCE_AGREEMENT',
    'PAYPAL',
    'TRAVEL_CARD'
    );
CREATE TYPE transaction_direction AS ENUM ('CREDIT', 'DEBIT');
CREATE TYPE card_type AS ENUM ('CREDIT', 'DEBIT', 'PREPAID');
CREATE TYPE card_network AS ENUM ('VISA', 'MASTERCARD', 'AMEX', 'MAESTRO');

CREATE TABLE account
(
    account_id          UUID PRIMARY KEY,
    account_type        account_type   NOT NULL,
    friendly_name       VARCHAR UNIQUE NOT NULL,
    held_with           VARCHAR        NOT NULL,
    identifier_in_sheet VARCHAR UNIQUE,
    sort_code           VARCHAR(6),
    account_number      VARCHAR
);

CREATE TABLE card
(
    card_id             UUID PRIMARY KEY,
    account_id          UUID    NOT NULL REFERENCES account (account_id),
    card_number         VARCHAR NOT NULL,
    network             card_network,
    identifier_in_sheet VARCHAR UNIQUE,
    card_comment        TEXT,
    start_month         DATE,
    expiry_month        DATE    NOT NULL,
    activated_date      DATE,
    deactivated_date    DATE,
    UNIQUE (card_number, expiry_month),
    CHECK (start_month IS NULL OR extract(day from start_month) = 1),
    CHECK (extract(day from expiry_month) = 1),
    CHECK (start_month IS NULL OR start_month < expiry_month),
    CHECK (activated_date IS NULL OR deactivated_date IS NULL OR activated_date < deactivated_date),
    CHECK (activated_date IS NULL OR start_month IS NULL OR activated_date > start_month),
    CHECK (deactivated_date IS NULL OR deactivated_date < (expiry_month + interval '1 month'))
);

CREATE TABLE txn
(
    transaction_id         UUID PRIMARY KEY,
    account_id             UUID                  NOT NULL REFERENCES account (account_id),
    card_id                UUID REFERENCES card (card_id),
    amount_minor_units     BIGINT                NOT NULL,
    direction              transaction_direction NOT NULL,
    currency               currency_code         NOT NULL,
    transaction_date       DATE                  NOT NULL,
    transaction_time       TIME,
    account_in_sheet       VARCHAR               NOT NULL,
    category_in_sheet      VARCHAR,
    description_in_sheet   VARCHAR               NOT NULL,
    type_code_in_sheet     VARCHAR               NOT NULL,
    type_in_sheet          VARCHAR               NOT NULL,
    hash_in_sheet          VARCHAR UNIQUE        NOT NULL,
    opposing_hash_in_sheet VARCHAR UNIQUE,
    running_balance_hint   BIGINT,
    row_in_sheet           INTEGER               NOT NULL
);

CREATE TABLE transaction_pair
(
    left_transaction_id  UUID REFERENCES txn (transaction_id),
    right_transaction_id UUID REFERENCES txn (transaction_id),
    CHECK ( left_transaction_id < right_transaction_id )
);
