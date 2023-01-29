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
            return <MonthView month={+month} transactions={txnsPerMonth[month]} opening={openingBalances[month]} closing={closingBalances[month]} />
        })}
    </div>
}

type MonthViewProps = {
    month: number
    transactions: TransactionModel[]
    opening: number
    closing: number
}
function MonthView({ month, transactions, opening, closing }: MonthViewProps) {
    const [unfurled, setUnfurled] = useState<boolean>(false)
    const totalCredits = transactions.filter(it => it.direction === 'CREDIT').map(it => it.amountMinorUnits).reduce((acc, it) => acc + it, 0)
    const totalDebits = transactions.filter(it => it.direction === 'DEBIT').map(it => it.amountMinorUnits).reduce((acc, it) => acc + it, 0)
    const unfurlableClass = (transactions.length > 0) ? 'unfurlable' : undefined
    return <div className={['monthCard', unfurlableClass].join(' ')} key={month}>
        <header onClick={() => setUnfurled(!unfurled)}>
            <div className='month' style={{ flex: '0 0 8em' }}>{monthNames[month - 1]}</div>
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
            {txnsForDay.map(txn => {
                return <div className='transaction'>
                    <div style={{ flex: '0 0 3em' }}>{txn.typeCodeInSheet}</div>
                    <div style={{ flex: '0 0 13em' }}>{txn.typeInSheet}</div>
                    <div style={{ flex: '1 0 auto' }}>{txn.descriptionInSheet}</div>
                    <div style={{ flex: '0 0 10em', textAlign: 'right' }}><Money minorUnits={signedAmount(txn)} signDisplay={'always'} /></div>
                </div>
            })}
            <div className='dailyClose'><Money minorUnits={startBalance} /></div>
        </div>
        )
    })
    return <div>{dayElements}</div>
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
