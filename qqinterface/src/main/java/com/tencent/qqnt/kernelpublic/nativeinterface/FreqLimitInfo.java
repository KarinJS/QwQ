package com.tencent.qqnt.kernelpublic.nativeinterface;

/* compiled from: P */
/* loaded from: classes2.dex */
public final class FreqLimitInfo {
    int isLimited;
    int leftCount;
    long limitTimestamp;

    public FreqLimitInfo() {
    }

    public int getIsLimited() {
        return this.isLimited;
    }

    public int getLeftCount() {
        return this.leftCount;
    }

    public long getLimitTimestamp() {
        return this.limitTimestamp;
    }

    public String toString() {
        return "FreqLimitInfo{isLimited=" + this.isLimited + ",leftCount=" + this.leftCount + ",limitTimestamp=" + this.limitTimestamp + ",}";
    }

    public FreqLimitInfo(int i2, int i3, long j2) {
        this.isLimited = i2;
        this.leftCount = i3;
        this.limitTimestamp = j2;
    }
}
