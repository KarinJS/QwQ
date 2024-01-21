package moe.qwq.miko.tools

import com.tencent.imcore.message.BaseQQMessageFacade
import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.data.MessageForGrayTips
import com.tencent.mobileqq.data.MessageRecord
import com.tencent.mobileqq.msg.api.IMessageRecordFactory
import com.tencent.mobileqq.qroute.QRoute
import de.robv.android.xposed.XposedBridge
import moe.qwq.miko.internals.helper.AppRuntimeFetcher.appRuntime
import moe.qwq.miko.internals.helper.NTServiceFetcher
import moe.qwq.miko.internals.helper.msgService
import java.lang.reflect.Method

object MessageHelper {

}