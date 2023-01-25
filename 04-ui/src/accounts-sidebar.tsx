import { useContext, useState } from "react";
import { context } from "./context";

type AccountsSideBarMenuProps = {
    onClick: (subItem: string) => void;
}
export function AccountsSideBarMenu(props: AccountsSideBarMenuProps) {
    const { accounts, fetchAccounts } = useContext(context)
    const [selectedSubItem, setSelectedSubItem] = useState<string | undefined>(undefined)

    if (accounts === undefined) {
        fetchAccounts?.()
        return <div className='loading'>Loading...</div>
    }

    const doClick = (id: string) => {
        setSelectedSubItem(id)
        props.onClick(id)
    }

    const accountElements = accounts?.map(it =>
        <li
            key={it.id}
            onClick={() => doClick(it.id)}
            className={selectedSubItem === it.id ? 'selected' : 'unselected'}
        >{it.alias}</li>
    )
    return <ul className='accountsSubMenu'>
        {accountElements}
    </ul>
}
