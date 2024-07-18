package com.tencent.qqnt.kernelpublic.nativeinterface;

/* compiled from: P */
/* loaded from: classes.dex */
public final class SearchHitInfo {
    int end;
    int start;

    public SearchHitInfo() {
    }

    public int getEnd() {
        return this.end;
    }

    public int getStart() {
        return this.start;
    }

    public String toString() {
        return "SearchHitInfo{start=" + this.start + ",end=" + this.end + ",}";
    }

    public SearchHitInfo(int i2, int i3) {
        this.start = i2;
        this.end = i3;
    }
}
