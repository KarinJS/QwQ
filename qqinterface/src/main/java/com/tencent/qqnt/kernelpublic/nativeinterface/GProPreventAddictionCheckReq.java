package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.io.Serializable;

/* loaded from: classes2.dex */
public final class GProPreventAddictionCheckReq implements Serializable {
    String context;
    long serialVersionUID;

    public GProPreventAddictionCheckReq() {
        this.serialVersionUID = 1L;
        this.context = "";
    }

    public String getContext() {
        return this.context;
    }

    public void setContext(String str) {
        this.context = str;
    }

    public String toString() {
        return "GProPreventAddictionCheckReq{context=" + this.context + ",}";
    }

    public GProPreventAddictionCheckReq(String str) {
        this.serialVersionUID = 1L;
        this.context = "";
        this.context = str;
    }
}
