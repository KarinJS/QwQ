package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.util.HashMap;

public interface IForwardOperateCallback {
    void onResult(int i2, String str, HashMap<Long, Integer> hashMap);
}