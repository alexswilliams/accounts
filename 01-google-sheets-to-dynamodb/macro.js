// include ../secrets/01-sensitive-info.js
// include ./dynamodb.js

const types = {
  'VIS': 'VISA Card Payment',
  'MAE': 'MasterCard Card Payment',
  'MAS': 'MasterCard Card Payment',
  ')))': 'Contactless Payment',
  'ATM': 'Cash Machine Withdrawal',
  'CR': 'Deposit',
  'TFR': 'Transfer',
  'DD': 'Direct Debit',
  'SO': 'Standing Order',
  'CC': 'Transfer (Paying off credit card)',
  'BP': 'Bill Payment',
  'CHQ': 'Cheque',
  'PP': 'PayPal Debit',
  'XCH': 'Currency Exchange',
  'PYM': 'Paym',
  'XX': 'Unknown Type'
}

function DryRunAllSheets() {
  const sheets = SpreadsheetApp.getActiveSpreadsheet().getSheets()
  sheets.forEach(sheet => {
    if (sheet.getSheetName() === 'Overview') return
    DryRun(sheet)
  })
}

function PerformUploadAllSheets() {
  const sheets = SpreadsheetApp.getActiveSpreadsheet().getSheets()
  sheets.forEach(sheet => {
    if (sheet.getSheetName() === 'Overview') return
    PerformUpload(sheet)
  })
}

function DryRun(inputSheet) {
  const sheet = inputSheet ?? SpreadsheetApp.getActiveSheet()
  const cells = sheet.getDataRange().getValues()

  const firstOfMonthFromTabName = sheet.getSheetName() === '2006 - May 07' ? '1 Oct 2006' : `1 ${sheet.getSheetName().split(' ')[0]} 20${sheet.getSheetName().split(' ')[1]}`
  const firstOfMonth = Utilities.formatDate(new Date(firstOfMonthFromTabName), 'UTC', 'yyyy-MM-dd')


  const headers = {}
  cells[0].forEach((value, col) => { if (value.toString().trim() !== '') headers[value.toString().trim()] = col })
  const allAccounts = findAllAccounts(cells, headers)
  const allTransactions = cells.map((row, rowNum) => extractTransactionFromRow(row, rowNum, allAccounts, headers, firstOfMonth)).filter(it => it != null)

  findMatchingTransfers(allTransactions, allAccounts)
  applyFixups(allTransactions)

  const sheetYearMonth = firstOfMonth.substring(0, 7)
  allTransactions.forEach(txn => {
    if (txn.date <= '2007-06') return
    if (txn.date.substring(0, 7) !== sheetYearMonth) throw new Error(`Date does not belong on sheet: ${txn.date} on sheet ${sheet.getSheetName()}`)
  })

  const occupiedRowCount = cells.map(row => row[headers['Description']] !== '' || (row[headers['Date']] !== '' && row[headers['Type']] !== '')).filter(it => it).length - 1
  const transactionCount = allTransactions.length
  const totalCredits = allTransactions.map(txn => txn.creditAmount).reduce(((it, next) => next === undefined ? it : it + next), 0)
  const totalDebits = allTransactions.map(txn => txn.debitAmount).reduce(((it, next) => next === undefined ? it : it + next), 0)
  const allRecs = {
    occupiedRowCount,
    transactionCount,
    totalCredits,
    totalDebits,
    yearMonth: firstOfMonth.substring(0, 7)
  }
  console.log(allRecs)

  if (occupiedRowCount !== transactionCount) throw new Error(`Occupied row count and transaction count do not match (${occupiedRowCount}, ${transactionCount})`)

  const distinctIdCount = new Set(allTransactions.map(it => it.id)).size
  if (distinctIdCount !== allTransactions.length) throw new Error(`Some IDs were duplicated`)

  return { allAccounts, allTransactions, allRecs }
}

function PerformUpload(sheet) {
  const { allAccounts, allTransactions, allRecs } = DryRun(sheet)

  sendToDynamoDb('DynamoDB_20120810.PutItem', {
    'TableName': 'recs',
    'Item': {
      'yearmonth': { 'S': String(allRecs.yearMonth) },
      'occupiedRowCount': { 'N': String(allRecs.occupiedRowCount) },
      'transactionCount': { 'N': String(allRecs.transactionCount) },
      'totalCredits': { 'N': String(allRecs.totalCredits) },
      'totalDebits': { 'N': String(allRecs.totalDebits) },
    }
  })

  allAccounts.forEach(acct => {
    sendToDynamoDb('DynamoDB_20120810.PutItem', {
      'TableName': 'accounts',
      'Item': {
        'id': { 'S': acct.id },
        'alias': { 'S': acct.alias },
        'currency': { 'S': 'GBP' }
      }
    })
  })

  sendBatches = inChunks(allTransactions, 25)
  sendBatches.forEach(batch => {
    const txnsToSend = batch.map(txn => ({
      'PutRequest': {
        'Item': cleanedTransaction(txn)
      }
    }))
    sendToDynamoDb('DynamoDB_20120810.BatchWriteItem', {
      'RequestItems': {
        'transactions': txnsToSend
      }
    })
  })
}

function cleanedTransaction(txn) {
  const copy = { ...txn }
  copy.id = { 'S': copy.id }
  if (!copy.opposingTransactionId) delete copy.opposingTransactionId; else copy.opposingTransactionId = { 'S': copy.opposingTransactionId }
  copy.rowNum = { 'N': String(copy.rowNum) }
  copy.accountId = { 'S': copy.accountId }
  if (!copy.date) delete copy.date; else copy.date = { 'S': copy.date }
  if (!copy.time) delete copy.time; else copy.time = { 'S': copy.time }
  copy.timestamp = { 'S': copy.timestamp }
  if (!copy.type) delete copy.type; else copy.type = { 'S': copy.type }
  if (!copy.typeCode) delete copy.typeCode; else copy.typeCode = { 'S': copy.typeCode }
  if (!copy.description) delete copy.description; else copy.description = { 'S': copy.description }
  if (!copy.category) delete copy.category; else copy.category = { 'S': copy.category }
  copy.currency = { 'S': copy.currency }
  if (!copy.debitAmount) delete copy.debitAmount; else copy.debitAmount = { 'N': String(copy.debitAmount) }
  if (!copy.creditAmount) delete copy.creditAmount; else copy.creditAmount = { 'N': String(copy.creditAmount) }
  if (!copy.runningBalanceHint) delete copy.runningBalanceHint; else copy.runningBalanceHint = { 'N': String(copy.runningBalanceHint) }
  if (!copy.expenseFor) delete copy.expenseFor; else copy.expenseFor = { 'S': copy.expenseFor }
  return copy
}

function findMatchingTransfers(transactions, accounts) {
  const accountIds = accounts.map(it => it.id)
  transactions.forEach(txn => {
    if (txn.typeCode === 'TFR' && accountIds.includes(txn.description)) {
      // there is probably a transaction elsewhere in the list that has the same value and date, and this account code in its description
      const opposingTxn = transactions.find(it =>
        it.typeCode === 'TFR'
        && it.accountId !== txn.accountId
        && it.description === txn.accountId
        && it.accountId === txn.description
        && it.date === txn.date
        && ((it.debitAmount > 0 && it.debitAmount === txn.creditAmount) || (it.creditAmount > 0 && it.creditAmount === txn.debitAmount))
      )
      if (opposingTxn) {
        if (opposingTxn.opposingTransactionId && opposingTxn.opposingTransactionId != txn.id) {
          Logger.log('Warning: conflicting transaction pairings!')
          throw new Error('Conflicting transaction pairings')
        }
        txn.opposingTransactionId = opposingTxn.id
      }
    }
    if (txn.typeCode === 'CC' && (txn.description.toUpperCase().startsWith('PAYPAL') || txn.accountId.toLowerCase() == 'paypal')) {
      const opposingTxn = transactions.find(it =>
        it.typeCode === 'CC'
        && (it.accountId !== txn.accountId)
        && (it.description.toUpperCase().startsWith('PAYPAL') || it.accountId.toLowerCase() == 'paypal')
        && ((it.debitAmount > 0 && it.debitAmount === txn.creditAmount) || (it.creditAmount > 0 && it.creditAmount === txn.debitAmount))
      )
      if (opposingTxn) {
        if (opposingTxn.opposingTransactionId && opposingTxn.opposingTransactionId != txn.id) {
          Logger.log('Warning: conflicting transaction pairings!')
          throw new Error('Conflicting transaction pairings')
        }
        txn.opposingTransactionId = opposingTxn.id
      }
    }
    if (txn.typeCode === 'CC' && (txn.description.toUpperCase().startsWith('MASTERCARD')
      || txn.description.toUpperCase().startsWith('VISA')
      || txn.description.toUpperCase().includes('PAYMENT')
      || txn.description.toUpperCase().includes('CREDIT CARD'))) {
      const opposingTxn = transactions.find(it =>
        it.typeCode === 'CC'
        && (it.accountId !== txn.accountId)
        && (it.date === txn.date)
        && (it.description.toUpperCase().startsWith('MASTERCARD') || it.description.toUpperCase().startsWith('VISA')
          || it.description.toUpperCase().includes('PAYMENT') || it.description.toUpperCase().includes('CREDIT CARD'))
        && ((it.debitAmount > 0 && it.debitAmount === txn.creditAmount) || (it.creditAmount > 0 && it.creditAmount === txn.debitAmount))
      )
      if (opposingTxn) {
        if (opposingTxn.opposingTransactionId && opposingTxn.opposingTransactionId != txn.id) {
          Logger.log('Warning: conflicting transaction pairings!')
          throw new Error('Conflicting transaction pairings')
        }
        txn.opposingTransactionId = opposingTxn.id
      }
    }
  })
}

function applyFixups(transactions) {
  transactions.forEach(txn => {
    if (txn.typeCode === 'CC' && (txn.description.toUpperCase().startsWith('PAYPAL') || txn.accountId.toLowerCase() === 'paypal')) {
      txn.type = 'Transfer (Loading PayPal)'
    }
    if ((txn.rowNum === 68 && txn.time === '22:10:00.000') || (txn.rowNum === 69 && txn.time === '14:36:00.000')) {
      txn.time = '00:00:00.000'
      txn.timestamp = `${txn.date}T${txn.time}`
    }
  })
}

function findAllAccounts(cells, headers) {
  const accountColumn = headers['Account']
  const allAccounts = []
  for (let r = 1; r < cells.length; r++) {
    if (cells[r][accountColumn] !== '') {
      const id = String(cells[r][accountColumn])
      const alias = aliases[id]
      if (!alias) {
        Logger.log('Warning: could not find account alias for: ' + id)
        throw new Error('Could not find account alias for: ' + id)
      }
      allAccounts.push({
        id: id,
        alias: alias,
        startRow: r,
        endRow: 0
      })
    }
  }
  if (allAccounts.length > 0) {
    for (let i = 0; i < allAccounts.length - 1; i++) {
      allAccounts[i].endRow = allAccounts[i + 1].startRow - 1
    }
    allAccounts[allAccounts.length - 1].endRow = cells.length - 1
  }
  return allAccounts;
}

function getAccountForLine(line, accounts) {
  if (line < accounts[0].startRow) throw new Error('Line out of range: ' + line)
  for (let i = 0; i < accounts.length; i++) {
    if (accounts[i].startRow <= line && line <= accounts[i].endRow) return accounts[i]
  }
  throw new Error('End of sheet reached without determining an account for line ' + line)
}

function extractTransactionFromRow(row, rowNum, accounts, headers, firstOfMonth) {
  if (rowNum === 0) return undefined

  const rawDesc = String(row[headers['Description']])

  const account = getAccountForLine(rowNum, accounts)

  const rawDate = row[headers['Date']]
  const date = (rawDate === '') ? firstOfMonth : Utilities.formatDate(new Date(rawDate), 'Europe/London', 'yyyy-MM-dd')

  const rawTime = row[headers['Time']]
  const time = (rawTime === '') ? '00:00:00.000' : Utilities.formatDate(new Date(rawTime), 'Europe/London', 'HH:mm:ss.SSS')

  const typeCode = row[headers['Type']]
  const lookedUpType = types[typeCode]
  const isNoActivity = rawDesc.toLowerCase().includes("no activity this month")
  const typeToUse = (!lookedUpType)
    ? ((isNoActivity) ? 'Balance Hint' : undefined)
    : lookedUpType

  if (!rawDesc && !typeCode && !rawDate) return undefined

  if (!typeToUse) {
    Logger.log('Warning: could not find type for code ' + typeCode)
    throw new Error('Could not find type for code ' + typeCode)
  }

  const rawCategory = row[headers['Category']]
  const category = (rawCategory === '')
    ? { 'DD': 'Bills', 'BP': 'Bills', 'ATM': 'Cash', 'CC': 'Transfer', 'TFR': 'Transfer' }[typeCode]
    : rawCategory

  const debit = row[headers['Paid out']]
  const debitAmount = (debit === '') ? undefined : Math.round(debit * 100)
  const credit = row[headers['Paid in']]
  const creditAmount = (credit === '') ? undefined : Math.round(credit * 100)
  const balance = row[headers['Balance (£)']]
  const newBalance = (balance === '') ? undefined : Math.round(balance * 100)

  const rawExpense = row[headers['Expense For']]

  return {
    id: Utilities.base64Encode(Utilities.computeDigest(
      Utilities.DigestAlgorithm.SHA_256,
      `${rowNum} ${date ?? 'X'} ${time ?? 'X'} ${account.id} ${typeCode} ${rawDesc} ${debitAmount ?? 'X'} ${creditAmount ?? 'X'} ${newBalance ?? 'X'}`)
    ).replace(/[/]/g, '_'),
    rowNum: rowNum,
    accountId: account.id,
    date: date,
    time: time,
    timestamp: `${date}T${time}`,
    type: lookedUpType,
    typeCode: typeCode,
    description: (rawDesc === '') ? undefined : rawDesc,
    category: category,
    currency: 'GBP',
    debitAmount: debitAmount,
    creditAmount: creditAmount,
    runningBalanceHint: newBalance,
    expenseFor: (rawExpense === '') ? undefined : rawExpense,
  }
}

