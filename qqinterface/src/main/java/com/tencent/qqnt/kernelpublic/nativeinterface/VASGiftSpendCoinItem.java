package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.io.Serializable;

/* compiled from: P */
/* loaded from: classes.dex */
public final class VASGiftSpendCoinItem implements Serializable {
    int amt;
    int coinType;
    long serialVersionUID = 1;

    public VASGiftSpendCoinItem() {
    }

    public int getAmt() {
        return this.amt;
    }

    public int getCoinType() {
        return this.coinType;
    }

    public String toString() {
        return "VASGiftSpendCoinItem{coinType=" + this.coinType + ",amt=" + this.amt + ",}";
    }

    public VASGiftSpendCoinItem(int i2, int i3) {
        this.coinType = i2;
        this.amt = i3;
    }
}
