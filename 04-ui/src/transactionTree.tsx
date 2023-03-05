import { useState } from "react"
import { signedAmount, TransactionModel } from "./context"

type TransactionTreeProps = {
    transactions: TransactionModel[]
}
export function TransactionTree(props: TransactionTreeProps) {
    if (props.transactions.length === 0) return <div>No transactions on this account</div>

    const txnYears = [...new Set(props.transactions.map(it => +it.transactionInstant.split('-')[0]))].sort()

    const earliestYear = txnYears[0]
    const latestYear = txnYears[txnYears.length - 1]

    const years = Array.from({ length: latestYear - earliestYear + 1 }, (x, i) => earliestYear + i)
    const txnsPerYear = Object.fromEntries(years.map(year => [year, props.transactions.filter(it => it.transactionInstant.startsWith('' + year))]))
    const openingBalances: Record<number, number> = { [earliestYear]: 0 }
    const closingBalances: Record<number, number> = {}
    for (const year of years) {
        const opening = openingBalances[year]
        const closing = txnsPerYear[year].map(it => it.direction === 'CREDIT' ? it.amountMinorUnits : -it.amountMinorUnits).reduce((acc, it) => acc + it, opening)
        closingBalances[year] = closing
        openingBalances[year + 1] = closing
    }

    return (<div>{years.map(year => <YearView year={year} transactions={txnsPerYear[year]} opening={openingBalances[year]} closing={closingBalances[year]} />)}</div>)
}

type YearViewProps = {
    year: number
    transactions: TransactionModel[]
    opening: number
    closing: number
}
function YearView({ year, transactions, opening, closing }: YearViewProps) {
    const [unfurled, setUnfurled] = useState<boolean>(false)
    const totalCredits = transactions.filter(it => it.direction === 'CREDIT').map(it => it.amountMinorUnits).reduce((acc, it) => acc + it, 0)
    const totalDebits = transactions.filter(it => it.direction === 'DEBIT').map(it => it.amountMinorUnits).reduce((acc, it) => acc + it, 0)
    const unfurlableClass = (transactions.length > 0) ? 'unfurlable' : undefined
    return <div className={['yearCard', unfurlableClass].join(' ')} key={year}>
        <header onClick={() => setUnfurled(!unfurled)}>
            <div className='year' style={{ flex: '0 0 4em' }}>{year}</div>
            <div style={{ flex: '1 1 5em', display: 'flex', flexDirection: 'column' }}>
                <div>Net: <Money minorUnits={closing - opening} signDisplay={'exceptZero'} /></div>
            </div>
            <div style={{ flex: '1 1 5em', display: 'flex', flexDirection: 'column' }}>
                <div>Opening: <Money minorUnits={opening} /></div>
                <div>Closing: <Money minorUnits={closing} /></div>
            </div>
            {(transactions.length == 0) ? (<div style={{ flex: '1 1 5em' }}>No activity</div>) : (
                <div style={{ flex: '1 1 5em', display: 'flex', flexDirection: 'column' }}>
                    <div>CR: <Money minorUnits={totalCredits} /></div>
                    <div>DR: <Money minorUnits={totalDebits} /></div>
                </div>
            )}
        </header>

        {unfurled && (transactions.length > 0) && <YearViewBody year={year} transactions={transactions} yearOpening={opening} />
        }
    </div >
}


type YearViewBodyProps = {
    year: number
    transactions: TransactionModel[]
    yearOpening: number
}
function YearViewBody({ year, transactions, yearOpening }: YearViewBodyProps) {
    const months = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
    const txnsPerMonth = Object.fromEntries(months.map(month =>
        [month, transactions.filter(it => it.transactionInstant.startsWith(`${year}-${String(month).padStart(2, '0')}`))]))
    const openingBalances: Record<number, number> = { 1: yearOpening }
    const closingBalances: Record<number, number> = {}
    for (const month of months) {
        const opening = openingBalances[month]
        const closing = txnsPerMonth[month].map(it =>
            it.direction === 'CREDIT' ? it.amountMinorUnits : -it.amountMinorUnits).reduce((acc, it) => acc + it, opening)
        closingBalances[month] = closing
        openingBalances[month + 1] = closing
    }
    return <div className='yearCardBody'>
        {months.map(month => {
            return <MonthView month={+month} year={year} transactions={txnsPerMonth[month]} opening={openingBalances[month]} closing={closingBalances[month]} />
        })}
    </div>
}

type MonthViewProps = {
    month: number
    year: number
    transactions: TransactionModel[]
    opening: number
    closing: number
}
function MonthView({ month, year, transactions, opening, closing }: MonthViewProps) {
    const [unfurled, setUnfurled] = useState<boolean>(false)
    const totalCredits = transactions.filter(it => it.direction === 'CREDIT').map(it => it.amountMinorUnits).reduce((acc, it) => acc + it, 0)
    const totalDebits = transactions.filter(it => it.direction === 'DEBIT').map(it => it.amountMinorUnits).reduce((acc, it) => acc + it, 0)
    const unfurlableClass = (transactions.length > 0) ? 'unfurlable' : undefined
    return <div className={['monthCard', unfurlableClass].join(' ')} key={month}>
        <header onClick={() => setUnfurled(!unfurled)}>
            <div className='month' style={{ flex: '0 0 8em' }}>{monthNames[month - 1]} {`${year}`.slice(2)}</div>
            <div style={{ flex: '1 1 5em', display: 'flex', flexDirection: 'column' }}>
                <div>Net: <Money minorUnits={closing - opening} signDisplay={'exceptZero'} /></div>
            </div>
            {(transactions.length == 0) ? (<div style={{ flex: '1 1 10em' }}>No activity</div>) : (
                <div style={{ flex: '1 1 10em', display: 'flex', flexDirection: 'column' }}>
                    <div><Money minorUnits={opening} />
                        &rarr; (<Money minorUnits={totalCredits} signDisplay={'always'} />,<Money minorUnits={-totalDebits} signDisplay={'always'} />)
                        &rarr; <Money minorUnits={closing} /></div>
                </div>
            )}
        </header>
        {unfurled && (transactions.length > 0) && (
            <div className='monthCardBody'><TransactionTable transactions={transactions} opening={opening} /></div>
        )}
    </div>
}

type TransactionTableProps = {
    transactions: TransactionModel[]
    opening: number
}
function TransactionTable({ transactions, opening }: TransactionTableProps) {
    const days = [...new Set(transactions.map(it => it.transactionInstant.split('T')[0]))].sort()
    let startBalance = opening
    const dayElements = days.map(date => {
        const txnsForDay = [...transactions.filter(it => it.transactionInstant.startsWith(date))].sort(transactionComparator)
        const totalCredits = txnsForDay.filter(it => it.direction === 'CREDIT').map(it => it.amountMinorUnits).reduce((acc, it) => acc + it, 0)
        const totalDebits = txnsForDay.filter(it => it.direction === 'DEBIT').map(it => it.amountMinorUnits).reduce((acc, it) => acc + it, 0)
        startBalance = txnsForDay.reduce((acc, txn) => acc + signedAmount(txn), startBalance)
        return (<div className='daysTransactions' key={date}>
            <div className='date'>{date} (+<Money minorUnits={totalCredits} /> -<Money minorUnits={totalDebits} />)</div>
            {txnsForDay.map(txn => <Transaction transaction={txn} balanceHighlighted={startBalance === txn.runningBalanceHintMinorUnits || -startBalance === txn.runningBalanceHintMinorUnits} key={txn.id} />)}
            <div className='dailyClose'><Money minorUnits={startBalance} /></div>
        </div>
        )
    })
    return <div>{dayElements}</div>
}

type TransactionProps = {
    transaction: TransactionModel
    balanceHighlighted: boolean
}
function Transaction({ transaction, balanceHighlighted }: TransactionProps): JSX.Element {
    const [unfurled, setUnfurled] = useState<boolean>(false)
    return (<div className='transaction unfurlable'>
        <header onClick={() => setUnfurled(!unfurled)}>
            <div style={{ flex: '0 0 3em' }}>{transaction.typeCodeInSheet}</div>
            <div style={{ flex: '0 0 13em' }}>{transaction.typeInSheet}</div>
            <div style={{ flex: '1 0 auto' }}>{transaction.descriptionInSheet}</div>
            <div style={{ flex: '0 0 10em', textAlign: 'right' }}><Money minorUnits={signedAmount(transaction)} signDisplay={'always'} /></div>
            <div className={balanceHighlighted ? 'highlighted' : ''} style={{ flex: '0 0 10em', textAlign: 'right' }}>{transaction.runningBalanceHintMinorUnits !== undefined
                ? <Money minorUnits={transaction.runningBalanceHintMinorUnits} signDisplay={'auto'} />
                : <></>}</div>
        </header>
        {unfurled && (<TransactionDetailsTable transaction={transaction} />)}
    </div>)
}

type TransactionDetailsTableProps = {
    transaction: TransactionModel
}
function TransactionDetailsTable({ transaction }: TransactionDetailsTableProps): JSX.Element {
    return <div className='transactionDetails'>
        <table>
            <tbody>
                <tr><td>Transaction ID</td><td>{transaction.id}</td></tr>
                <tr><td>Account ID</td><td>{transaction.accountId}</td></tr>
                <tr><td>Opposing ID</td><td>{transaction.opposingId}</td></tr>
                <tr><td>Amount</td><td>{transaction.amountMinorUnits} (<Money minorUnits={signedAmount(transaction)} currency={transaction.currency} signDisplay={'always'} />)</td></tr>
                <tr><td>Currency</td><td>{transaction.currency}</td></tr>
                <tr><td>Direction</td><td>{transaction.direction}</td></tr>
                <tr><td>Transaction Instant</td><td>{transaction.transactionInstant}</td></tr>
                <tr><td>Description in Sheet</td><td>{transaction.descriptionInSheet}</td></tr>
                <tr><td>typeInSheet</td><td>{transaction.typeInSheet}</td></tr>
                <tr><td>typeCodeInSheet</td><td>{transaction.typeCodeInSheet}</td></tr>
                <tr><td>runningBalanceHint</td><td>{transaction.runningBalanceHintMinorUnits} {transaction.runningBalanceHintMinorUnits !== undefined ?
                    <>(<Money minorUnits={transaction.runningBalanceHintMinorUnits} signDisplay={'always'} />)</>
                    : <></>}</td></tr>
                <tr><td>hashInSheet</td><td>{transaction.hashInSheet}</td></tr>
                <tr><td>opposingHashInSheet</td><td>{transaction.opposingHashInSheet}</td></tr>
                <tr><td>rowInSheet</td><td>{transaction.rowInSheet}</td></tr>
                <tr><td>Card ID</td><td>{transaction.cardId}</td></tr>
            </tbody></table>
    </div>
}


type MoneyProps = {
    minorUnits: number
    currency?: string
    signDisplay?: "auto" | "never" | "always" | "exceptZero"
}
function Money({ minorUnits, currency = 'GBP', signDisplay = 'auto' }: MoneyProps) {
    const number = Intl.NumberFormat('en-GB', { style: 'currency', currency, signDisplay }).format(minorUnits / 100)
    if (minorUnits < 0) return <div className='negativeMoney' style={{ display: 'inline-block' }}>{number}</div>
    return <div style={{ display: 'inline-block' }}>{number}</div>
}

const transactionComparator = (a: TransactionModel, b: TransactionModel) =>
    a.transactionInstant.localeCompare(b.transactionInstant) || a.direction.localeCompare(b.direction) || Number(b.amountMinorUnits) - Number(a.amountMinorUnits)

const monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']
