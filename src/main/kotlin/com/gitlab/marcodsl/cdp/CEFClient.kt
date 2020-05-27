package com.gitlab.marcodsl.cdp

import pl.wendigo.chrome.Browser
import pl.wendigo.chrome.api.runtime.EvaluateRequest
import pl.wendigo.chrome.targets.Target

object CEFClient {

    private val chrome: Browser = Browser.builder().withAddress("127.0.0.1:13172").build()
    private val target: Target

    init {
        val avaliableTargets = chrome.targets().filter { it.title.contains("nui") }
        target = chrome.attach(avaliableTargets.random())

        executeJavascript("const fetchAsBlob = url => fetch(url).then(response => response.blob());")
        executeJavascript("const convertBlobToBase64=e=>new Promise((a,o)=>{const r=new FileReader;r.onerror=o,r.onload=(()=>{a(r.result.replace(/^data:.+;base64,/, ''))}),r.readAsDataURL(e)});")
    }

    fun executeJavascript(code: String) {
        val expression = EvaluateRequest(code)
        target.Runtime.evaluate(expression).blockingGet()
    }

}