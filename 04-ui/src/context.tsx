import { createContext, useState } from "react"

type RequestState<T> = {
    inFlight: boolean
    data?: T
    error?: string
}

export type CardModel = {
    id: string
    cardNumber: string
    network?: string
    comment?: string
    startMonth?: string
    expiryMonth?: string
}
export type AccountModel = {
    id: string
    accountType: string
    alias: string
    institution?: string
    primaryCurrency: string
    sortCode?: string
    accountNumber?: string
    cards: Record<string, CardModel>[]
}

export type TransactionModel = {
    id: string
    opposingId: string
    amountMinorUnits: number
    currency: string
    direction: string
    transactionInstant: string
    descriptionInSheet: string
    typeInSheet: string
    typeCodeInSheet: string
    runningBalanceHintMinorUnits: number
}
export function signedAmount(txn: TransactionModel): number {
    return txn.direction === 'CREDIT' ? txn.amountMinorUnits : -txn.amountMinorUnits
}

type AppContext = {
    accounts?: RequestState<AccountModel[]>
    fetchAccounts?: () => Promise<void>
    transactions?: RequestState<Record<string, TransactionModel[]>>
    fetchTransactions?: () => Promise<void>
}
export const context = createContext<AppContext>({})

type ServiceStoreProps = {
    children?: React.ReactNode
}
export function GlobalContext(props: ServiceStoreProps) {
    const [accounts, setAccounts] = useState<RequestState<AccountModel[]>>()
    const fetchAccounts = async () => {
        if (accounts?.inFlight === true) return
        setAccounts({ inFlight: true, data: undefined, error: undefined })
        try {
            const res = await fetch('http://localhost:8080/accounts', { signal: AbortSignal.timeout(2000) })
            if (res.ok) {
                setAccounts({ inFlight: false, data: await res.json(), error: undefined })
            } else {
                console.log(res)
                setAccounts({ inFlight: false, data: undefined, error: res.statusText })
            }
        } catch (e: any) {
            console.log(e)
            setAccounts({ inFlight: false, data: undefined, error: String(e) })
        }
    }

    const [transactions, setTransactions] = useState<RequestState<Record<string, TransactionModel[]>>>()
    const fetchTransactions = async () => {
        if (transactions?.inFlight === true) return
        setTransactions({ inFlight: true, data: undefined, error: undefined })
        try {
            const res = await fetch('http://localhost:8080/transactions', { signal: AbortSignal.timeout(2000) })
            if (res.ok) {
                setTransactions({ inFlight: false, data: toTransactionModel(await res.json()), error: undefined })
            } else {
                console.log(res)
                setTransactions({ inFlight: false, data: undefined, error: res.statusText })
            }
        } catch (e: any) {
            console.log(e)
            setTransactions({ inFlight: false, data: undefined, error: String(e) })
        }
    }

    return <context.Provider value={{ accounts, fetchAccounts, transactions, fetchTransactions }} >
        {props.children}
    </context.Provider>
}

function toTransactionModel(data: Record<string, Record<string, unknown>[]>): Record<string, TransactionModel[]> {
    const result: Record<string, TransactionModel[]> = {}
    Object.entries(data).forEach(([accountId, txnList]) => {
        result[accountId] = txnList.map(it => {
            return Object.assign({}, it, {
                amountMinorUnits: stringToMinorUnits(it.amount as string),
                runningBalanceHintMinorUnits: stringToMinorUnits(it.runningBalanceHint as string)
            }) as TransactionModel
        })
    })
    return result
}

function stringToMinorUnits(str: string): number {
    const parts = str.split('.')
    const major = +parts[0] * 100
    const minor = (parts.length > 0) ? +parts[1] : 0
    return major + minor
}