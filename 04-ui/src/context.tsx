import { createContext, useState } from "react";

type ServiceStoreProps = {
    children?: React.ReactNode;
}

type Account = {
    id: string;
    alias: string;
    institution?: string;
    primaryCurrency: string;
    sortCode?: string;
    accountNumber?: string;
    cards: Record<string, any>[]
}

type AppContext = {
    accounts?: Account[];
    fetchAccounts?: () => Promise<void>;
}
export const context = createContext<AppContext>({});

export function GlobalContext(props: ServiceStoreProps) {
    const [accounts, setAccounts] = useState()

    const fetchAccounts = async () => {
        const res = await fetch('http://localhost:8080/accounts')
        if (res.ok) {
            setAccounts(await res.json())
        }
    }

    return <context.Provider value={{ accounts, fetchAccounts }} >
        {props.children}
    </context.Provider>
}
