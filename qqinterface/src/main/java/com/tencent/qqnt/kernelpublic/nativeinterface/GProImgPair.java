package com.tencent.qqnt.kernelpublic.nativeinterface;

/* loaded from: classes2.dex */
public final class GProImgPair {
    String imgDayMode;
    String imgNightMode;

    public GProImgPair() {
        this.imgNightMode = "";
        this.imgDayMode = "";
    }

    public String getImgDayMode() {
        return this.imgDayMode;
    }

    public String getImgNightMode() {
        return this.imgNightMode;
    }

    public String toString() {
        return "GProImgPair{imgNightMode=" + this.imgNightMode + ",imgDayMode=" + this.imgDayMode + ",}";
    }

    public GProImgPair(String str, String str2) {
        this.imgNightMode = "";
        this.imgDayMode = "";
        this.imgNightMode = str;
        this.imgDayMode = str2;
    }
}
