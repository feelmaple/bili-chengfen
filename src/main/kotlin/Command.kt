package com.feelmaple.mirai.plugin

import kotlinx.coroutines.*
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand


object Command: CompositeCommand(
    owner = BiliChengfen,
    primaryName = "ccf",

    description = "查成分重载配置"
) {
    @SubCommand
    @Description("重载插件配置文件")
    suspend fun CommandSender.reload() {
        BiliChengfen.reloadConfig()
        sendMessage(Config.msgReload)
    }

    @SubCommand
    @Description("手动更新vtb列表")
    suspend fun CommandSender.update() {
        val scopee = CoroutineScope(Dispatchers.Default)
        val jobb = scopee.launch {
            try {
                val status = updateVtbList()
                if(status == "ture"){
                    sendMessage("更新成功")
                }else{
                    sendMessage("更新失败 $status")}
            } catch (e: Exception){
                sendMessage("更新失败 $e")
            }
        }
        runBlocking {
            jobb.join()
        }
        scopee.cancel()
    }
}