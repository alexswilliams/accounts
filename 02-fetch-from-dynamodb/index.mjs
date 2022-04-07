import * as Client from '@aws-sdk/client-dynamodb'
import * as fs from 'fs'
import { default as creds } from '../secrets/02-credentials.mjs'

if (!fs.existsSync('../secrets/02-data/accounts')) fs.mkdirSync('../secrets/02-data/accounts', { recursive: true })
if (!fs.existsSync('../secrets/02-data/transactions')) fs.mkdirSync('../secrets/02-data/transactions', { recursive: true })

const client = new Client.DynamoDB({
    region: 'eu-west-2',
    credentials: creds
})

const accounts = await client.scan({ TableName: 'accounts' })
fs.writeFileSync('../secrets/02-data/accounts/all.json', JSON.stringify(accounts.Items, null, 2), { encoding: 'utf8' })

const uploadedRecsRaw = await client.scan({ TableName: 'recs' })
const uploadedRecs = Object.fromEntries(uploadedRecsRaw.Items.map(ddbItem => ([ddbItem.yearmonth.S, {
    transactionCount: Number(ddbItem.transactionCount.N),
    totalCredits: Number(ddbItem.totalCredits.N),
    totalDebits: Number(ddbItem.totalDebits.N)
}])))
fs.writeFileSync('../secrets/02-data/recs-from-sheet.json', JSON.stringify(Object.entries(uploadedRecs), null, 2), { encoding: 'utf8' })

const replacer = RegExp('/', 'g')
const perMonth = {}
let lastKey = undefined
do {
    const transactions = await client.scan({ TableName: 'transactions', ExclusiveStartKey: lastKey })
    lastKey = transactions.LastEvaluatedKey
    console.log(`Fetched ${transactions.Count} of ${transactions.ScannedCount}`)
    transactions.Items.forEach(item => {
        const idFileSafe = item.id.S.replace(replacer, '_')
        const date = item.date.S
        const yearMonth = (/[0-9]{4}-[0-9]{2}-[0-9]{2}/.test(date)) ? date.substring(0, 7) : 'invalid-date'
        if (!fs.existsSync('../secrets/02-data/transactions/' + yearMonth)) fs.mkdirSync('../secrets/02-data/transactions/' + yearMonth, { recursive: true })
        fs.writeFileSync(`../secrets/02-data/transactions/${yearMonth}/${date}--${idFileSafe}.json`, JSON.stringify(item, null, 2))


        const creditAmount = item.creditAmount?.N
        const debitAmount = item.debitAmount?.N

        perMonth[yearMonth] = (perMonth[yearMonth] !== undefined) ? {
            transactionCount: perMonth[yearMonth].transactionCount + 1,
            totalCredits: Number(perMonth[yearMonth].totalCredits) + Number(creditAmount ?? 0),
            totalDebits: Number(perMonth[yearMonth].totalDebits) + Number((debitAmount ?? 0))
        } : {
            transactionCount: 1,
            totalCredits: Number((creditAmount ?? 0)),
            totalDebits: Number((debitAmount ?? 0))
        }
    })
} while (lastKey !== undefined)

console.log(perMonth)

fs.writeFileSync('../secrets/02-data/recs-from-fetched.json', JSON.stringify(Object.entries(perMonth), null, 2), { encoding: 'utf8' })

Object.entries(perMonth).forEach(([month, recData]) => {
    const fromSheet = uploadedRecs[month]
    if (fromSheet === undefined) {
        console.error(`Year/Month ${month} found in fetched data but not found in sheet`)
        return
    }
    if (fromSheet.transactionCount !== recData.transactionCount)
        console.error(`Txn count for ${month} does not match - ${fromSheet.transactionCount} in sheet, ${recData.transactionCount} fetched`)
    if (fromSheet.totalCredits !== recData.totalCredits)
        console.error(`Sum of credits for ${month} does not match - ${fromSheet.totalCredits} in sheet, ${recData.totalCredits} fetched`)
    if (fromSheet.totalDebits !== recData.totalDebits)
        console.error(`Sum of debits for ${month} does not match - ${fromSheet.totalDebits} in sheet, ${recData.totalDebits} fetched`)
})
Object.keys(uploadedRecs).forEach(month => {
    if (perMonth[month] === undefined)
        console.error(`Year/Month ${month} was found in sheet but not found in fetched data`)
})

