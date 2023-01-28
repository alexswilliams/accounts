import { useContext } from "react"
import { context } from "./context"
import { TransactionTree } from "./transactionTree"

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

    return <div key={props.id}>
        <div className='mainpagecontent'>
            <header>{account.alias}</header>
            <div style={{ display: 'flex' }}>
                <table>
                    {(account.accountNumber !== undefined) && (<tr><td>Account Number</td><td>{formatAccountNumber(account.accountNumber, account.sortCode)}</td></tr>)}
                    <tr><td>Currency</td><td>{account.primaryCurrency}</td></tr>
                    <tr><td>Account Type</td><td>{formatAccountType(account.accountType)}</td></tr>
                    <tr><td>With</td><td>{account.institution}</td></tr>
                    <tr><td>Status</td><td>Open</td></tr>
                </table>
            </div>
        </div>

        <div className='mainpagecontent'>
            <h1>Transactions</h1>
            <TransactionTree transactions={transactionList} />
        </div >
    </div>
}

function formatAccountNumber(num: string, sortCode?: string): string {
    if (sortCode === undefined) return num
    return `${sortCode} ${num}`
}

function formatAccountType(accType: string): string {
    return {
        'CURRENT_ACCOUNT': 'Current Account',
        'AGREEMENT_BETWEEN_FRIENDS': 'Agreement Between Friends',
        'CASH_ISA': 'Cash ISA',
        'FINANCE_AGREEMENT': 'Finance Agreement',
        'FIXED_TERM_LOAN': 'Fixed Term Loan',
        'PAYPAL': 'PayPal',
        'CREDIT_CARD': 'Credit Card',
        'PREPAID_DEBIT_CARD': 'Prepaid Debit Card',
        'FIXED_SAVINGS_ACCOUNT': 'Fixed Term Saver',
    }[accType] ?? accType
}
