package com.tencent.mobileqq.webview.swift;

import org.json.JSONObject;

public abstract class WebViewPlugin {
    public static String toJsScript(String str, JSONObject jSONObject, JSONObject jSONObject2) {
        //return "window.mqq && mqq.execEventCallback && mqq.execEventCallback(" + com.tencent.mobileqq.webview.util.s.j(str) + "," + jSONObject + "," + jSONObject2 + ");";
        return "";
    }

    public void callJs(String jsFunction, String... args) {
    }
}
