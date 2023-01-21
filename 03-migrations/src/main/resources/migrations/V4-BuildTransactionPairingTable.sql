INSERT INTO transaction_pair(left_transaction_id, right_transaction_id)
SELECT txn_left.transaction_id  AS left_transaction_id,
       txn_right.transaction_id AS right_transaction_id
FROM txn txn_left
         INNER JOIN txn txn_right ON txn_left.opposing_hash_in_sheet = txn_right.hash_in_sheet AND
                                     txn_right.opposing_hash_in_sheet = txn_left.hash_in_sheet
WHERE txn_left.transaction_id < txn_right.transaction_id;
