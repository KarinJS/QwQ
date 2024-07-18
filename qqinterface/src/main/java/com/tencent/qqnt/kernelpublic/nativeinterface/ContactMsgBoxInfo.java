package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.util.ArrayList;

/* compiled from: P */
/* loaded from: classes2.dex */
public final class ContactMsgBoxInfo {
    Contact contact;
    MsgBoxNecessaryMsgInfo firstUnreadMsgInfo;
    ArrayList<SpecificEventTypeInfoInMsgBox> listOfSpecificEventTypeInfosInMsgBox;
    Long unreadCnt;

    public ContactMsgBoxInfo() {
        this.contact = new Contact();
        this.listOfSpecificEventTypeInfosInMsgBox = new ArrayList<>();
    }

    public Contact getContact() {
        return this.contact;
    }

    public MsgBoxNecessaryMsgInfo getFirstUnreadMsgInfo() {
        return this.firstUnreadMsgInfo;
    }

    public ArrayList<SpecificEventTypeInfoInMsgBox> getListOfSpecificEventTypeInfosInMsgBox() {
        return this.listOfSpecificEventTypeInfosInMsgBox;
    }

    public Long getUnreadCnt() {
        return this.unreadCnt;
    }

    public String toString() {
        return "ContactMsgBoxInfo{contact=" + this.contact + ",firstUnreadMsgInfo=" + this.firstUnreadMsgInfo + ",unreadCnt=" + this.unreadCnt + ",listOfSpecificEventTypeInfosInMsgBox=" + this.listOfSpecificEventTypeInfosInMsgBox + ",}";
    }

    public ContactMsgBoxInfo(Contact contact, MsgBoxNecessaryMsgInfo msgBoxNecessaryMsgInfo, Long l2, ArrayList<SpecificEventTypeInfoInMsgBox> arrayList) {
        this.contact = new Contact();
        this.listOfSpecificEventTypeInfosInMsgBox = new ArrayList<>();
        this.contact = contact;
        this.firstUnreadMsgInfo = msgBoxNecessaryMsgInfo;
        this.unreadCnt = l2;
        this.listOfSpecificEventTypeInfosInMsgBox = arrayList;
    }
}
