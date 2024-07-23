package moe.qwq.miko.ext

import com.google.protobuf.UnknownFieldSet


fun UnknownFieldSet.getUnknownObject(number: Int): UnknownFieldSet {
    return getField(number).groupList.firstOrNull() ?: getField(number).lengthDelimitedList.firstOrNull()?.let {
        UnknownFieldSet.parseFrom(it)
    } ?: throw RuntimeException("failed to fetch object")
}

fun UnknownFieldSet.getUnknownObjects(number: Int): List<UnknownFieldSet> {
    return getField(number).groupList.ifEmpty {
        getField(number).lengthDelimitedList.map {
            UnknownFieldSet.parseFrom(it)
        }
    }
}