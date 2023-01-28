import { useState } from "react"
import { TransactionModel } from "./context"

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
function YearView(props: YearViewProps) {
    const [visible, setVisible] = useState<boolean>(false)
    const totalCredits = props.transactions.filter(it => it.direction === 'CREDIT').map(it => it.amountMinorUnits).reduce((acc, it) => acc + it, 0)
    const totalDebits = props.transactions.filter(it => it.direction === 'DEBIT').map(it => it.amountMinorUnits).reduce((acc, it) => acc + it, 0)
    const txns = [...props.transactions].sort(transactionComparator)
    return <div>
        <div className='yearCardHeader'>
            <h3>{props.year}</h3>
            <div>Opening Balance: {formatMinorUnits(props.opening)}; Closing Balance: {formatMinorUnits(props.closing)}</div>
            <div>Total Credits: {formatMinorUnits(totalCredits)}; Total Debits: {formatMinorUnits(totalDebits)}</div>
            <button onClick={() => setVisible(!visible)}>Toggle</button></div>
        {visible && <div>
            <table>
                {txns.map(it =>
                    <tr>
                        <td>{it.transactionInstant}</td>
                        <td>{it.typeCodeInSheet}</td>
                        <td><pre>{it.descriptionInSheet}</pre></td>
                        <td>{formatMinorUnits(it.amountMinorUnits)}</td>
                        <td>{it.direction}</td>
                        <td>{formatMinorUnits(it.runningBalanceHintMinorUnits)}</td>
                    </tr>
                )}
            </table>
        </div>}
    </div>
}

const transactionComparator = (a: TransactionModel, b: TransactionModel) =>
    a.transactionInstant.localeCompare(b.transactionInstant) || a.direction.localeCompare(b.direction) || Number(b.amountMinorUnits) - Number(a.amountMinorUnits)

const GBP = Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP' })
function formatMinorUnits(minorUnits: number): string {
    return GBP.format(minorUnits / 100)
    const sign = minorUnits < 0 ? '-' : ''
    const asString = String(Math.abs(minorUnits))
    if (asString.length == 1) return `${sign}0.0${asString}`
    if (asString.length == 2) return `${sign}0.${asString}`
    const major = asString.slice(0, -2)
    const minor = asString.slice(-2)
    return sign + major + '.' + minor
}
