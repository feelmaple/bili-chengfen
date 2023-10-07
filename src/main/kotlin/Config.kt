package com.feelmaple.mirai.plugin

import net.mamoe.mirai.console.data.*


object Config: ReadOnlyPluginConfig("BiliConfig") {
    @ValueDescription("cookie")
    val cookie by value<String>()

    @ValueDescription("UA")
    val UA by value<String>()

    @ValueDescription("msg-reload")
    val msgReload by value("配置文件已重载")

    @ValueDescription("最长等待时间ms")
    val time by value(5)

    @ValueDescription("chromium绝对位置")
    val chromium by value<String>()

    @ValueDescription("chromdriver绝对位置")
    val chromedriver by value<String>()

    @ValueDescription("font")
    val font by value<String>("font.ttf")

    @ValueDescription("vtb获取地址")
    val url by value<String>("https://api.vtbs.moe/v1/short")

    @ValueDescription("模板文件名称")
    val template by value<String>("info.html")

}
