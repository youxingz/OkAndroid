window.ok = {
    debug: false,
    listenerFunc: null,
    registerListener(listenerFunc) {
        this.listenerFunc = listenerFunc
    },
    onReceive(request) {
        if (debug) {
            console.warn("[OKAndroid] ReceiveID: " + request.requestId)
        }
        let resp = undefined
        if (request.type === 1) { // by ok-android
            resp = this.dispatchSystemReq(request)
        } else {
            // exec by user
            if (this.listenerFunc) {
                resp = this.listenerFunc(request.data)
                if (debug) {
                    console.warn("[OKAndroid] Return: ", resp)
                }
            }
        }
        return JSON.stringify({
            requestId: request.requestId,
            timestamp: new Date().getTime(),
            data: resp
        })
    },
    dispatchSystemReq(request) {
        return "SUCCESS"
    }
}

// ok.debug = true
// ok.registerListener((req) => { return req })
// listening...