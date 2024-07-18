package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.io.Serializable;

/* loaded from: classes2.dex */
public final class GProKVPair implements Serializable {
    String key;
    long serialVersionUID;
    byte[] value;

    public GProKVPair() {
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
        return "GProKVPair{key=" + this.key + ",value=" + this.value + ",}";
    }

    public GProKVPair(String str, byte[] bArr) {
        this.serialVersionUID = 1L;
        this.key = "";
        this.value = new byte[0];
        this.key = str;
        this.value = bArr;
    }
}
