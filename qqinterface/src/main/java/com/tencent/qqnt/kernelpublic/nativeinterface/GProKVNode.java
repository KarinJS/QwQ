package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.io.Serializable;

/* loaded from: classes2.dex */
public final class GProKVNode implements Serializable {
    String key;
    long serialVersionUID;
    byte[] value;

    public GProKVNode() {
        this.serialVersionUID = 1L;
        this.key = "";
        this.value = new byte[0];
    }

    public String getKey() {
        return this.key;
    }

    public byte[] getValue() {
        return this.value;
    }

    public String toString() {
        return "GProKVNode{key=" + this.key + ",value=" + this.value + ",}";
    }

    public GProKVNode(String str, byte[] bArr) {
        this.serialVersionUID = 1L;
        this.key = "";
        this.value = new byte[0];
        this.key = str;
        this.value = bArr;
    }
}
