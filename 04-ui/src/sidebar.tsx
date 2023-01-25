import { useState } from "react"
import { AccountsSideBarMenu } from "./accounts-sidebar";

type SideBarProps = {
    onClick: (nextPage: string, nextSubPage?: string) => void;
}
export function SideBar(props: SideBarProps) {
    const [selectedItem, setSelectedItem] = useState<string | undefined>(undefined)

    const doItemClick = (name: string) => {
        setSelectedItem(name)
        props.onClick(name)
    }

    const doSubItemClick = (subItem: string) => {
        props.onClick(selectedItem!, subItem)
    }

    return <ul className="sidebarmenu">
        <SideBarItem onClick={() => doItemClick('accounts')} selected={selectedItem == 'accounts'} displayString='All Accounts' />
        {selectedItem == 'accounts' && (<AccountsSideBarMenu onClick={doSubItemClick} />)}
    </ul >
}


type SideBarItemProps = {
    onClick: () => void;
    selected: boolean;
    displayString: string;
}

function SideBarItem(props: SideBarItemProps) {
    return <li
        onClick={props.onClick}
        className={props.selected ? 'selected' : 'unselected'}
    >{props.displayString}</li>
}
