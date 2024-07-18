package com.tencent.qqnt.kernelpublic.nativeinterface;

/* loaded from: classes2.dex */
public final class GProGuidePageInfo {
    String desc;
    GProImgPair images;
    String title;

    public GProGuidePageInfo() {
        this.title = "";
        this.desc = "";
        this.images = new GProImgPair();
    }

    public String getDesc() {
        return this.desc;
    }

    public GProImgPair getImages() {
        return this.images;
    }

    public String getTitle() {
        return this.title;
    }

    public String toString() {
        return "GProGuidePageInfo{title=" + this.title + ",desc=" + this.desc + ",images=" + this.images + ",}";
    }

    public GProGuidePageInfo(String str, String str2, GProImgPair gProImgPair) {
        this.title = "";
        this.desc = "";
        this.images = new GProImgPair();
        this.title = str;
        this.desc = str2;
        this.images = gProImgPair;
    }
}
