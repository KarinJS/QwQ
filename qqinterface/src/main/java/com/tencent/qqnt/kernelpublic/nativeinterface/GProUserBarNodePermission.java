package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.io.Serializable;

/* loaded from: classes2.dex */
public final class GProUserBarNodePermission implements Serializable {
    int nodeType;
    long serialVersionUID = 1;
    int visibleType;

    public GProUserBarNodePermission() {
    }

    public int getNodeType() {
        return this.nodeType;
    }

    public int getVisibleType() {
        return this.visibleType;
    }

    public String toString() {
        return "GProUserBarNodePermission{nodeType=" + this.nodeType + ",visibleType=" + this.visibleType + ",}";
    }

    public GProUserBarNodePermission(int i2, int i3) {
        this.nodeType = i2;
        this.visibleType = i3;
    }
}
