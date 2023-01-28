import { useContext, useState } from "react"
import { AccountModel, context } from "./context"

type AccountsSideBarMenuProps = {
    onClick: (subItem: string) => void
}
export function AccountsSideBarMenu(props: AccountsSideBarMenuProps) {
    const { accounts, fetchAccounts } = useContext(context)
    const [selectedSubItem, setSelectedSubItem] = useState<string | undefined>()

    if (accounts === undefined || accounts.inFlight === true) {
        fetchAccounts?.()
        return <div className='loading'>Loading...</div>
    }
    if (accounts.error !== undefined) {
        return <div className='apiError'>Error while fetching: {accounts.error}</div>
    }

    const doClick = (id: string) => {
        setSelectedSubItem(id)
        props.onClick(id)
    }

    return <AccountsSideBarMenuLayout accounts={accounts.data ?? []} doClick={doClick} selectedId={selectedSubItem} />
}


type AccountsSideBarMenuLayoutProps = {
    accounts: AccountModel[]
    doClick: (id: string) => void
    selectedId?: string
}
function AccountsSideBarMenuLayout(props: AccountsSideBarMenuLayoutProps) {
    const accountElements = props.accounts.map(it =>
        <li
            key={it.id}
            onClick={() => props.doClick(it.id)}
            className={props.selectedId === it.id ? 'selected' : 'unselected'}
        >{it.alias}</li>
    )
    return <ul className='accountsSubMenu'>
        {accountElements}
    </ul>
}
