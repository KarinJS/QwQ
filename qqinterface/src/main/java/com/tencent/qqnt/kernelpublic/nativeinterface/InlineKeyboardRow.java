package com.tencent.qqnt.kernelpublic.nativeinterface;

import java.util.ArrayList;

/* compiled from: P */
/* loaded from: classes2.dex */
public final class InlineKeyboardRow {
    ArrayList<InlineKeyboardButton> buttons;

    public InlineKeyboardRow() {
        this.buttons = new ArrayList<>();
    }

    public ArrayList<InlineKeyboardButton> getButtons() {
        return this.buttons;
    }

    public String toString() {
        return "InlineKeyboardRow{buttons=" + this.buttons + ",}";
    }

    public InlineKeyboardRow(ArrayList<InlineKeyboardButton> arrayList) {
        this.buttons = new ArrayList<>();
        this.buttons = arrayList;
    }
}
