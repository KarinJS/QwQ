package com.tencent.qqnt.kernel.nativeinterface;

import com.tencent.qqnt.kernelpublic.nativeinterface.IGetTransferableMemberCallback;
import com.tencent.qqnt.kernelpublic.nativeinterface.IKernelGroupListener;

public interface IKernelGroupService {
    void getTransferableMemberInfo(long uin, IGetTransferableMemberCallback cb);

    long addKernelGroupListener(IKernelGroupListener ln);
}
