import { useState } from "react"
import { AccountsSideBarMenu } from "./accounts-sidebar"

type SideBarProps = {
    onClick: (nextPage: string, nextSubPage?: string) => void
}
export function SideBar({ onClick }: SideBarProps) {
    const [selectedItem, setSelectedItem] = useState<string | undefined>()

    const doItemClick = (name: string) => {
        setSelectedItem(name)
        onClick(name)
    }

    const doSubItemClick = (subItem: string) => {
        onClick(selectedItem!, subItem)
    }

    return <ul className="sidebarmenu">
        <SideBarItem onClick={() => doItemClick('accounts')} selected={selectedItem == 'accounts'} displayString='All Accounts' />
        {selectedItem == 'accounts' && (<AccountsSideBarMenu onClick={doSubItemClick} />)}
    </ul >
}


type SideBarItemProps = {
    onClick: () => void
    selected: boolean
    displayString: string
}
function SideBarItem({ onClick, selected, displayString }: SideBarItemProps) {
    return <li
        onClick={onClick}
        className={selected ? 'selected' : 'unselected'}
    >{displayString}</li>
}
