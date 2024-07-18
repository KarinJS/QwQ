package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.io.Serializable;

/* loaded from: classes2.dex */
public final class GProEnterAVChannelPermissionInfo implements Serializable {
    int roleType;
    long serialVersionUID = 1;

    public GProEnterAVChannelPermissionInfo() {
    }

    public int getRoleType() {
        return this.roleType;
    }

    public String toString() {
        return "GProEnterAVChannelPermissionInfo{roleType=" + this.roleType + ",}";
    }

    public GProEnterAVChannelPermissionInfo(int i2) {
        this.roleType = i2;
    }
}
