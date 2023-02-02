import { useState, StrictMode, ReactElement } from 'react'
import { createRoot } from 'react-dom/client'
import { GlobalContext } from './context'
import { SideBar } from './sidebar'
import { Account } from "./account"


const container = document.getElementById('root')!
const root = createRoot(container)

root.render(<StrictMode><MainPageRoot /></StrictMode>)

function MainPageRoot() {
    const [page, setPage] = useState<string | undefined>()
    const [subPage, setSubPage] = useState<string | undefined>()

    return <div id='pagecontainer'>
        <GlobalContext>
            <div id='sidebar'>
                <SideBar onClick={(nextPage, nextSubPage) => {
                    setPage(nextPage)
                    setSubPage(nextSubPage)
                }} />
            </div>
            <div id='mainpage'>
                {page && <Router page={page} subPage={subPage} />}
            </div>
        </GlobalContext>
    </div >
}


type RouterProps = {
    page: string
    subPage?: string
}
function Router({ page, subPage }: RouterProps) {
    const pages: Record<string, () => ReactElement<any, any>> = {
        'accounts': () => <Account id={subPage} />
    }
    if (!(page in pages)) {
        return <h1>Page not found: {page}</h1>
    }
    return pages[page]()
}
