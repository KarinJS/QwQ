package com.tencent.qqnt.kernelpublic.nativeinterface;

/* loaded from: classes2.dex */
public final class GProPlayAddress {
    Integer bitRate;
    String formats;
    String url;

    public GProPlayAddress() {
    }

    public Integer getBitRate() {
        return this.bitRate;
    }

    public String getFormats() {
        return this.formats;
    }

    public String getUrl() {
        return this.url;
    }

    public String toString() {
        return "GProPlayAddress{url=" + this.url + ",bitRate=" + this.bitRate + ",formats=" + this.formats + ",}";
    }

    public GProPlayAddress(String str, Integer num, String str2) {
        this.url = str;
        this.bitRate = num;
        this.formats = str2;
    }
}
