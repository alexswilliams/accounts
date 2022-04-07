// Google Sheets macro for accessing dynamodb
// Nothing specific to dynamodb other than the hard-coded service two lines below, and the list of headers.

const region = 'eu-west-2'
const service = 'dynamodb'
const host = `${service}.${region}.amazonaws.com`
const hexits = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f']

function byteArrayToHex(it) {
    return it.map(x => `${hexits[(x >> 4) & 0x0f]}${hexits[x & 0x0f]}`).join('')
}

function payloadHash(payload) {
    const payloadBlob = Utilities.newBlob(payload, Utilities.Charset.UTF_8)
    const payloadHash = Utilities.computeDigest(Utilities.DigestAlgorithm.SHA_256, payloadBlob.getBytes())
    return [payloadBlob, byteArrayToHex(payloadHash)]
}

function createCanonicalHeaderList(headers) {
    const newHeaders = [...headers].sort()
    return newHeaders.join(';')
}

function getInMapCaseInsensitive(map, key) {
    const lowerCaseKey = key.toLowerCase();
    const pair = Object.entries(map).find(([k]) => k.toLowerCase() == lowerCaseKey)
    return (!pair) ? undefined : pair[1]
}

function createCanonicalRequestHash(method, encodedPath, encodedQueryString, signedHeadersList, headers) {
    const sortedHeaders = [...signedHeadersList].sort()
    const canonicalHeaders = sortedHeaders.map(it => `${it}:${getInMapCaseInsensitive(headers, it)}`).join(`\n`)
    const canonicalRequest = `${method}\n` +
        `${encodedPath}\n` +
        `${encodedQueryString}\n` +
        `${canonicalHeaders}\n\n` +
        `${createCanonicalHeaderList(signedHeadersList)}\n` +
        `${headers['x-amz-content-sha256']}`
    const documentBlob = Utilities.newBlob(canonicalRequest, Utilities.Charset.UTF_8)
    const documentHash = Utilities.computeDigest(Utilities.DigestAlgorithm.SHA_256, documentBlob.getBytes())
    return byteArrayToHex(documentHash)
}

function createStringToSign(utcTimestamp, utcDate, region, service, canonicalRequestHash) {
    const scope = `${utcDate}/${region}/${service}/aws4_request`
    const signingString = `AWS4-HMAC-SHA256\n` +
        `${utcTimestamp}\n` +
        `${scope}\n` +
        `${canonicalRequestHash}`
    return Utilities.newBlob(signingString, Utilities.Charset.UTF_8).getBytes()
}

function createSigningKey(secretKey, utcDate, region, service) {
    const dateKey = Utilities.computeHmacSha256Signature(utcDate, `AWS4${secretKey}`)
    const dateRegionKey = Utilities.computeHmacSha256Signature(Utilities.newBlob(region, Utilities.Charset.UTF_8).getBytes(), dateKey)
    const dateRegionServiceKey = Utilities.computeHmacSha256Signature(Utilities.newBlob(service, Utilities.Charset.UTF_8).getBytes(), dateRegionKey)
    const signingKey = Utilities.computeHmacSha256Signature(Utilities.newBlob('aws4_request', Utilities.Charset.UTF_8).getBytes(), dateRegionServiceKey)
    return signingKey
}

function signPayload(payloadBytes, signingKey) {
    return byteArrayToHex(Utilities.computeHmacSha256Signature(payloadBytes, signingKey))
}

function sendToDynamoDb(target = 'DynamoDB_20120810.PutItem', jsPayload = {}) {
    const utcTimestamp = Utilities.formatDate(new Date(), 'Etc/UTC', `yyyyMMdd'T'HHmmss'Z'`)
    const utcDate = Utilities.formatDate(new Date(), 'Etc/UTC', 'yyyyMMdd')

    const [payloadBlob, payloadHashHex] = payloadHash(JSON.stringify(jsPayload))

    const signedHeadersList = ['content-type', 'host', 'x-amz-content-sha256', 'x-amz-date', 'x-amz-target']
    const headers = {
        'content-type': 'application/x-amz-json-1.0; charset=utf-8',
        'host': host,
        'x-amz-date': utcTimestamp,
        'x-amz-content-sha256': payloadHashHex,
        'x-amz-target': target
    }
    const canonicalRequestHash = createCanonicalRequestHash('POST', '/', '', signedHeadersList, headers)

    const signature = signPayload(
        createStringToSign(utcTimestamp, utcDate, region, 'dynamodb', canonicalRequestHash),
        createSigningKey(definitelyNotASecretKey, utcDate, region, 'dynamodb'))

    const authHeader = `AWS4-HMAC-SHA256 Credential=${accessKeyId}/${utcDate}/${region}/${service}/aws4_request,SignedHeaders=${createCanonicalHeaderList(signedHeadersList)},Signature=${signature}`

    const contentType = headers['content-type']
    delete headers['host']
    delete headers['content-type']
    const options = {
        method: 'post',
        contentType: contentType,
        headers: {
            ...headers,
            'Authorization': authHeader
        },
        payload: payloadBlob,
        // muteHttpExceptions: true,
    }
    const url = `https://${host}/`

    const response = UrlFetchApp.fetch(url, options)
    Logger.log(response)
}

function inChunks(array, chunkSize) {
    return Array(Math.ceil(array.length / chunkSize)).fill().map(function (_, i) {
        return array.slice(i * chunkSize, i * chunkSize + chunkSize)
    })
}
