package com.feelmaple.mirai.plugin

import net.mamoe.mirai.console.data.ReadOnlyPluginData
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

object Data: ReadOnlyPluginData("BiliConfig") {
    @ValueDescription("生成目录用")
    val cookie by value<String>()
}