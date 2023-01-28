import { useContext } from "react"
import { context } from "./context"

type AccountProps = {
    id?: string
}
export function Account(props: AccountProps) {
    if (props.id === undefined) return <></>

    const { accounts, fetchAccounts, transactions, fetchTransactions } = useContext(context)
    if (accounts === undefined || accounts.inFlight === true) {
        fetchAccounts?.()
        return <div>Loading accounts...</div>
    }
    const account = (accounts.data ?? []).find(it => it.id == props.id)
    if (account === undefined) {
        return <h1>Could not find account</h1>
    }

    if (transactions === undefined || transactions.inFlight === true) {
        fetchTransactions?.()
        return <div>Loading transactions...</div>
    }
    const transactionList = (transactions?.data ?? {})[account.id] ?? []

    return <div className='mainpagecontent'>
        <dl>
            <dt>Alias</dt><dd>{account.alias}</dd>
            <dt>Account Type</dt><dd>{account.accountType}</dd>
            <dt>Currency</dt><dd>{account.primaryCurrency}</dd>
            <dt>With</dt><dd>{account.institution}</dd>
            {(account.accountNumber !== undefined) && (<dt>Account Number</dt>)}
            {(account.accountNumber !== undefined) && (<dd>{formatAccountNumber(account.accountNumber, account.sortCode)}</dd>)}
        </dl>
        <h1>Transactions</h1>
        <table>
            {transactionList.map(it =>
                <tr><td>{it.transactionInstant}</td><td>{it.descriptionInSheet}</td><td>{it.amount}</td><td>{it.direction}</td></tr>
            )}
        </table>
    </div>
}

function formatAccountNumber(num: string, sortCode?: string) {
    if (sortCode === undefined) return num
    return `${sortCode} ${num}`
}