import { useContext } from "react"
import { AccountModel, context, TransactionModel } from "./context"
import { TransactionTree } from "./transactionTree"

type AccountProps = {
    id?: string
}
export function Account({ id }: AccountProps) {
    if (id === undefined) return <></>
    const { accounts, fetchAccounts, transactions, fetchTransactions } = useContext(context)
    if (accounts === undefined) fetchAccounts?.()
    if (transactions === undefined) fetchTransactions?.()

    const loadingAccounts = (accounts === undefined || accounts.inFlight === true)
    const loadingTransactions = (transactions === undefined || transactions.inFlight === true)
    if (loadingAccounts || loadingTransactions) {
        return <div>Loading...</div>
    }
    const account = (accounts.data ?? []).find(it => it.id == id)
    if (account === undefined) {
        return <h1>Could not find account</h1>
    }
    const transactionList = (transactions?.data ?? {})[account.id] ?? []

    return <AccountLayout account={account} transactionList={transactionList} />
}


type AccountLayoutProps = {
    account: AccountModel
    transactionList: TransactionModel[]
}
function AccountLayout({ account, transactionList }: AccountLayoutProps) {
    return <div key={account.id}>
        <div className='mainPageContent'>
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

        <div className='mainPageContent'>
            <header>Transactions</header>
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
