import * as React from 'react'
import * as Server from 'react-dom/server'
import * as Client from 'react-dom/client'


const container = document.getElementById('root')!
const root = Client.createRoot(container)
root.render(<h1>Hello React!</h1>)
