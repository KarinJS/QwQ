package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.io.Serializable;

/* compiled from: P */
/* loaded from: classes2.dex */
public final class LocalGrayTipRobot implements Serializable {
    boolean isBlackRobot;
    long robotTid;
    long serialVersionUID = 1;

    public LocalGrayTipRobot() {
    }

    public boolean getIsBlackRobot() {
        return this.isBlackRobot;
    }

    public long getRobotTid() {
        return this.robotTid;
    }

    public String toString() {
        return "LocalGrayTipRobot{robotTid=" + this.robotTid + ",isBlackRobot=" + this.isBlackRobot + ",}";
    }

    public LocalGrayTipRobot(long j2, boolean z) {
        this.robotTid = j2;
        this.isBlackRobot = z;
    }
}
