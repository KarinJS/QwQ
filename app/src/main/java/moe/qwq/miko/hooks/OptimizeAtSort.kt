package moe.qwq.miko.hooks

import android.content.Context
import de.robv.android.xposed.XposedHelpers
import moe.fuqiuluo.processor.HookAction
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.setting.QwQSetting

@HookAction("群成员at排序优化")
class OptimizeAtSort: IAction {
    override fun onRun(ctx: Context) {
        val atSelectMemberUseCase = LuoClassloader.load("com.tencent.mobileqq.aio.input.at.business.AIOAtSelectMemberUseCase")
            ?: throw RuntimeException("AIOAtSelectMemberUseCase not found")
        for (m in atSelectMemberUseCase.declaredMethods) {
            if (m.parameterTypes.size == 1 && m.returnType == Map::class.java && m.parameterTypes[0] == List::class.java) {
                atSelectMemberUseCase.hookMethod(m.name).after {
                    val backMap = it.result as Map<String, List<Any>>
                    val map = backMap.toMutableMap()
                    map.clear()
                    backMap.forEach { (k, l) ->
                        val list = l.toMutableList()
                        val ob = l.toMutableList()
                        val ab = l.toMutableList()
                        list.clear()
                        ob.clear()
                        ab.clear()
                        l.forEach { v ->
                            var added = false
                            for (vf in v.javaClass.declaredFields) {
                                if (vf.type.name.contains("MemberInfo")) {
                                    val info = XposedHelpers.getObjectField(v, vf.name)
                                    val role = XposedHelpers.getObjectField(info, "role")
                                    if (role.toString().contains("OWNER")) {
                                        // 群主
                                        ob.add(v)
                                        added = true
                                        break
                                    } else if (role.toString().contains("ADMIN")) {
                                        // 管理
                                        ab.add(v)
                                        added = true
                                        break
                                    }
                                }
                            }
                            if (!added) {
                                list.add(v)
                            }
                        }
                        map[k] = list
                        val f = "★"
                        val c = map.getOrDefault(f, arrayListOf()).toMutableList()
                        c.addAll(ob)
                        c.addAll(ab)
                        map[f] = c.toList()
                    }
                    it.result = map.toMap()
                }
            }
        }
    }

    override val name: String = QwQSetting.OPTIMIZE_AT_SORT
}