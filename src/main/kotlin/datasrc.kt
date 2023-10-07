package com.feelmaple.mirai.plugin

import com.feelmaple.mirai.plugin.BiliChengfen.configFolderPath
import com.google.gson.JsonParser
import java.io.File
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.features.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat


var vtbFilePathStr = "${configFolderPath.toAbsolutePath()}\\vtb_list.json"

fun convertTimestampToDateString(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp * 1000)
}

suspend fun updateVtbList(): String {
    val url = Config.url

    val client = HttpClient() {
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
        }
    }

    try{
        val response = client.get<String>(url) {}
        dumpVtbList(response)
        return "ture"
    } catch (e: Throwable){
        BiliChengfen.logger.error(e)
        dumpVtbList("[]")
        return "$e"
    }
}



fun dumpVtbList(vtbList: String) {
    if (!(File(vtbFilePathStr).exists())) {
        File(vtbFilePathStr).createNewFile()
    }
    File(vtbFilePathStr).writeText(vtbList)
}

fun loadVtbList(): String {
    if (File(vtbFilePathStr).exists()) {
        try {
            return File(vtbFilePathStr).readText()
        } catch (ex: Exception) {
            BiliChengfen.logger.error(ex)
            BiliChengfen.logger.error("vtb列表解析错误")

        }
    }
    return "[]"
}

suspend fun getVtbList(): String {
    if (!(File(vtbFilePathStr).exists())) {
        File(vtbFilePathStr).createNewFile()
    }
    val vtbList = loadVtbList()
    if (vtbList.isEmpty()) {
        updateVtbList()
    }
    return loadVtbList()
}

suspend fun getUidByName(name: String): Long {
    val url = "http://api.bilibili.com/x/web-interface/search/type"
    val params = mapOf("search_type" to "bili_user", "keyword" to name)
    val client = HttpClient() {
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
        }
    }
    try {
        val response = client.get<String>(url) {
            parameter("search_type", "bili_user")
            parameter("keyword", name)
            headers {
                append("cookie", Config.cookie)
                append("user-agent", Config.UA)
            }
        }

        val root = JsonParser.parseString(response)
        val data = root.asJsonObject.getAsJsonObject("data")
        val result = data.asJsonObject.getAsJsonArray("result")
        val uname = result.get(0).asJsonObject.get("uname").asString
        if (uname != null) {
            if (uname.equals(name, ignoreCase = true)) {
                return result.get(0).asJsonObject.get("mid").asLong
            } else return 0
        } else return 0
    } catch (e: Throwable) {
        BiliChengfen.logger.error("在getUidByName($name)中出现错误: $e")

        return 0
    } finally {
        client.close()
    }
}

suspend fun getCard(uid: Long) : String {
    val url = "https://account.bilibili.com/api/member/getCardByMid"
    val params = mapOf("mid" to uid)
    val client = HttpClient() {
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
        }
    }
    try {
        val resp = client.get<String>(url) {
            parameter("mid", uid)
            headers {
                append("cookie", Config.cookie)
                append("user-agent", Config.UA)
            }
        }

        return resp
    } catch (e: Throwable) {
        BiliChengfen.logger.error("在getAttentions($uid)中出现错误: $e")
        return "{}"
    } finally {
        client.close()
    }
}


suspend fun getAttentionsInfo(uids: String): Any {
    val url = "https://api.vc.bilibili.com/account/v1/user/cards"
    val params = mapOf("uids" to uids)
    val client = HttpClient() {
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
        }
    }
    try {
        val resp = client.get<String>(url) {
            parameter("uids", uids)
            headers {
                append("cookie", Config.cookie)
                append("user-agent", Config.UA)
            }
        }
        return resp
    } catch (e:Exception){
        println("在getAttentionsInfo中出现错误: $e")
        return 0
    } finally {
        client.close()
    }
}

suspend fun getFanMedal(uid: Long): String {
    val url = "https://api.live.bilibili.com/xlive/web-ucenter/user/MedalWall"
    val params = mapOf("target_id" to uid)
    val client = HttpClient() {
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
        }
    }
    try {
        val resp = client.get<String>(url) {
            parameter("target_id", uid)
            headers {
                append("cookie", Config.cookie)
                append("user-agent", Config.UA)
            }
        }
        return resp
    } catch (e: Exception){
        BiliChengfen.logger.error("在getFanMedal中出现错误: $e")
        return ""
    } finally {
        client.close()
    }
}

suspend fun formatAttentions(attentions: List<Long>): List<HashMap<String, Any>> {

    val gzf = mutableListOf<HashMap<String, Any>>()

    if (attentions.size < 50) {
        val uids = attentions.joinToString(",")
        val aaa = getAttentionsInfo(uids)
        val root = JsonParser.parseString(aaa as String?)
        val data = root.asJsonObject.getAsJsonArray("data")

        for (i in 0 until data.size()) {
            val namevalue = data[i].asJsonObject.get("name").asString
            val uidvalue = data[i].asJsonObject.get("mid").asString
            val dict = hashMapOf<String, Any>(
                "name" to namevalue,
                "uid" to uidvalue
            )
            gzf.add(dict)
        }
    } else {
        var listall = attentions.chunked(49)

        for (i in 0 until listall.size) {

            val uid = listall[i]
            val uids = uid.joinToString(",")
            val aaa = getAttentionsInfo(uids)
            delay(10)
            val root = JsonParser.parseString(aaa as String?)
            val data = root.asJsonObject.getAsJsonArray("data")
            for (j in 0 until data.size()) {
                val namevalue = data[j].asJsonObject.get("name").asString
                val uidvalue = data[j].asJsonObject.get("mid").asString
                val dict = hashMapOf<String, Any>(
                    "name" to namevalue,
                    "uid" to uidvalue
                )
                gzf.add(dict)
            }
        }
    }
    return gzf
}

suspend fun formatFanMedal(fanMedalResp: String): MutableList<HashMap<String, Any>> {
    var medalf = mutableListOf<HashMap<String, Any>>()
    val root = JsonParser.parseString(fanMedalResp)
    val data = root.asJsonObject.getAsJsonObject("data")
    val list = data.asJsonObject.getAsJsonArray("list")
    for(i in 0 until list.size()){
        val uidvalue = list[i].asJsonObject.get("medal_info").asJsonObject.get("target_id").asString
        val medalnamevalue = list[i].asJsonObject.get("medal_info").asJsonObject.get("medal_name").asString
        val levelvalue = list[i].asJsonObject.get("medal_info").asJsonObject.get("level").asString
        val namelvalue = list[i].asJsonObject.get("target_name").asString
        val colorbordervalue = formatColor(
            list[i].asJsonObject.get("medal_info").asJsonObject.get("medal_color_border").asInt)
        val colorstartvalue = formatColor(
            list[i].asJsonObject.get("medal_info").asJsonObject.get("medal_color_start").asInt)
        val colorendvalue = formatColor(
            list[i].asJsonObject.get("medal_info").asJsonObject.get("medal_color_end").asInt)
        val dict = hashMapOf<String, Any>(
            "name" to namelvalue,
            "medal_name" to medalnamevalue,
            "uid" to uidvalue,
            "level" to levelvalue,
            "colorborder" to colorbordervalue,
            "colorstart" to colorstartvalue,
            "colorend" to colorendvalue,

            )
        medalf.add(dict)
    }
    return medalf
}

fun formatColor(intColor:Int): String {
    return String.format("#%06X", 0xFFFFFF and intColor)
}

fun formatVtb(vtbString: String): MutableList<Map<String, Any>> {
    var vtbf = mutableListOf<Map<String, Any>>()
    val root = JsonParser.parseString(vtbString)
    val list = root.asJsonArray
    for(i in 0 until list.size()){
        val midvalue = list[i].asJsonObject.get("mid").asString
        val unamevalue = list[i].asJsonObject.get("uname").asString
        val dict = mapOf(
            "uid" to midvalue,
            "name" to unamevalue,
        )
        vtbf.add(dict)
    }
    return vtbf
}












