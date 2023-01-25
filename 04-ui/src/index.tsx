import { useState } from 'react'
import { createRoot } from 'react-dom/client'
import { GlobalContext } from './context'
import { SideBar } from './sidebar'


const container = document.getElementById('root')!
const root = createRoot(container)

root.render(<MainPageRoot />)

function MainPageRoot() {
    const [page, setPage] = useState<string | undefined>()

    return <div id='pagecontainer'>
        <GlobalContext>
            <div id='sidebar'>
                <SideBar onClick={(nextPage) => setPage(nextPage)} />
            </div>
            <div id='mainpage'>
                {page && (<div className='mainpagecontent'>
                    <p>{page}</p>
                </div>
                )}
            </div>
        </GlobalContext>
    </div >
}
