package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.io.Serializable;

/* loaded from: classes2.dex */
public final class GProMemberInfo implements Serializable {
    String avatarMeta;
    int gender;
    long serialVersionUID;
    long tinyId;

    public GProMemberInfo() {
        this.serialVersionUID = 1L;
        this.avatarMeta = "";
    }

    public String getAvatarMeta() {
        return this.avatarMeta;
    }

    public int getGender() {
        return this.gender;
    }

    public long getTinyId() {
        return this.tinyId;
    }

    public String toString() {
        return "GProMemberInfo{tinyId=" + this.tinyId + ",gender=" + this.gender + ",avatarMeta=" + this.avatarMeta + ",}";
    }

    public GProMemberInfo(long j2, int i2, String str) {
        this.serialVersionUID = 1L;
        this.avatarMeta = "";
        this.tinyId = j2;
        this.gender = i2;
        this.avatarMeta = str;
    }
}
