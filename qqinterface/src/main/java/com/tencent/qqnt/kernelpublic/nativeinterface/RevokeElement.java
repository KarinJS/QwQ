package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.io.Serializable;

public final class RevokeElement implements Serializable {
    boolean isSelfOperate;
    String operatorMemRemark;
    String operatorNick;
    String operatorRemark;
    long operatorRole;
    long operatorTinyId;
    String operatorUid;
    String origMsgSenderUid;
    long serialVersionUID;
    String wording;

    public RevokeElement() {
        this.serialVersionUID = 1L;
        this.operatorUid = "";
        this.origMsgSenderUid = "";
        this.wording = "";
    }

    public boolean getIsSelfOperate() {
        return this.isSelfOperate;
    }

    public String getOperatorMemRemark() {
        return this.operatorMemRemark;
    }

    public String getOperatorNick() {
        return this.operatorNick;
    }

    public String getOperatorRemark() {
        return this.operatorRemark;
    }

    public long getOperatorRole() {
        return this.operatorRole;
    }

    public long getOperatorTinyId() {
        return this.operatorTinyId;
    }

    public String getOperatorUid() {
        return this.operatorUid;
    }

    public String getOrigMsgSenderUid() {
        return this.origMsgSenderUid;
    }

    public String getWording() {
        return this.wording;
    }

    public String toString() {
        return "RevokeElement{operatorTinyId=" + this.operatorTinyId + ",operatorRole=" + this.operatorRole + ",operatorUid=" + this.operatorUid + ",operatorNick=" + this.operatorNick + ",operatorRemark=" + this.operatorRemark + ",operatorMemRemark=" + this.operatorMemRemark + ",origMsgSenderUid=" + this.origMsgSenderUid + ",isSelfOperate=" + this.isSelfOperate + ",wording=" + this.wording + ",}";
    }

    public RevokeElement(long tinyId, long role, String operatorUid, String operatorNick, String operatorRemark, String operatorMemRemark, String origMsgSenderUid, boolean isSelfOperate, String wording) {
        this.serialVersionUID = 1L;
        this.operatorTinyId = tinyId;
        this.operatorRole = role;
        this.operatorUid = operatorUid;
        this.operatorNick = operatorNick;
        this.operatorRemark = operatorRemark;
        this.operatorMemRemark = operatorMemRemark;
        this.origMsgSenderUid = origMsgSenderUid;
        this.isSelfOperate = isSelfOperate;
        this.wording = wording;
    }
}
