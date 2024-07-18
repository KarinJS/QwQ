package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.util.ArrayList;

public interface IOperateTransferInfoCallback {
    void onResult(int i2, String str, ArrayList<Long> arrayList);
}