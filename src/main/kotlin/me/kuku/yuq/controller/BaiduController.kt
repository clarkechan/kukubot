package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.mif
import kotlinx.coroutines.delay
import me.kuku.yuq.entity.BaiduEntity
import me.kuku.yuq.entity.BaiduService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.BaiduLogic
import me.kuku.yuq.utils.openOrClose
import org.springframework.stereotype.Service
import java.util.Base64

@GroupController
@PrivateController
@Service
class BaiduController (
    private val baiduService: BaiduService,
    private val baiduLogic: BaiduLogic
) {

    @Action("百度登录")
    suspend fun baiduLogin(qqEntity: QqEntity, context: BotActionContext) {
        val qrcode = baiduLogic.getQrcode()
        context.source.sendMessage(
            mif.at(qqEntity.qq)
                .plus(mif.imageByByteArray(Base64.getDecoder().decode(qrcode.imageBase)).plus("请使用qq扫码登录百度"))
        )
        while (true) {
            delay(3000)
            val result = baiduLogic.checkQrcode(qrcode)
            when (result.code) {
                0 -> continue
                200 -> {
                    val newBaiduEntity = result.data
                    val baiduEntity = baiduService.findByQqEntity(qqEntity) ?: BaiduEntity().also {
                        it.qqEntity = qqEntity
                    }
                    baiduEntity.cookie = newBaiduEntity.cookie
                    baiduEntity.sToken = newBaiduEntity.sToken
                    baiduEntity.bdUss = newBaiduEntity.bdUss
                    baiduService.save(baiduEntity)
                    context.source.sendMessage(mif.at(qqEntity.qq).plus("绑定百度成功"))
                    break
                }
                else -> {
                    context.source.sendMessage(mif.at(qqEntity.qq).plus(result.message))
                    break
                }
            }
        }
    }

    @Before(except = ["baiduLogin"])
    fun before(qqEntity: QqEntity): BaiduEntity {
        return baiduService.findByQqEntity(qqEntity) ?: throw mif.at(qqEntity.qq).plus("您没有绑定百度，操作失败").toThrowable()
    }

    @Action("百度贴吧签到")
    suspend fun tieBaSign(baiduEntity: BaiduEntity): String {
        val result = baiduLogic.tieBaSign(baiduEntity)
        return if (result.isSuccess) "百度贴吧签到成功"
        else "百度贴吧签到失败，${result.message}"
    }

    @Action("百度自动签到 {status}")
    fun baiduAutoSign(baiduEntity: BaiduEntity, status: Boolean): String {
        baiduEntity.config.sign = status.toStatus()
        baiduService.save(baiduEntity)
        return "百度自动签到${status.openOrClose()}成功"
    }

    @Action("游帮帮看广告")
    suspend fun watchAd(baiduEntity: BaiduEntity): String {
        val result = baiduLogic.ybbWatchAd(baiduEntity)
        return if (result.isSuccess) "游帮帮观看广告成功"
        else "游帮帮观看广告失败，${result.message}"
    }

    @Action("游帮帮签到")
    suspend fun ybbSign(baiduEntity: BaiduEntity): String {
        val result = baiduLogic.ybbSign(baiduEntity)
        return if (result.isSuccess) "游帮帮签到成功"
        else "游帮帮签到失败，${result.message}"
    }



}