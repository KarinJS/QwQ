package moe.qwq.miko.internals.locators

import java.lang.reflect.Field

fun interface FieldLocator {
    operator fun invoke(): Pair<Class<*>, Field>?
}
