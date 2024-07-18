package com.tencent.qqnt.kernel.api.impl;

import com.tencent.qqnt.kernelpublic.nativeinterface.IKernelMsgListener;
import com.tencent.qqnt.kernelpublic.nativeinterface.IOperateCallback;
import com.tencent.qqnt.kernelpublic.nativeinterface.RichMediaFilePathInfo;
import com.tencent.qqnt.kernelpublic.nativeinterface.TempChatPrepareInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MsgService {
    public void addMsgListener(IKernelMsgListener listener) {
    }

    // 9.0.8 混淆该方法
    //public void addLocalJsonGrayTipMsg(@NotNull Contact contact, @NotNull JsonGrayElement json, boolean needStore, boolean needRecentContact, @Nullable IAddJsonGrayTipMsgCallback iAddJsonGrayTipMsgCallback) {
    //}


    public String getRichMediaFilePathForGuild(@NotNull RichMediaFilePathInfo richMediaFilePathInfo) {
        return null;
    }

    @Nullable
    public String getRichMediaFilePathForMobileQQSend(@NotNull RichMediaFilePathInfo richMediaFilePathInfo) {
        return null;
    }

    public void prepareTempChat(TempChatPrepareInfo tempChatPrepareInfo, IOperateCallback cb) {

    }
}
