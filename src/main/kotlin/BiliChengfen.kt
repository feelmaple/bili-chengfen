package com.feelmaple.mirai.plugin



import com.google.gson.JsonParser
import io.pebbletemplates.pebble.PebbleEngine
import kotlinx.coroutines.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import org.openqa.selenium.Dimension
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.OutputType
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

object BiliChengfen : KotlinPlugin(
    JvmPluginDescription(
        id = "com.feelmaple.bili-chengfen",
        name = "bili-chengfen",
        version = "0.3.0",

    ) {
author("feelmaple")
    }
) {
    private val timer = Timer()
    override fun onEnable() {
        reloadConfig()
        Data.reload()
        Command.register()
        logger.info { "Bili-查成分 已加载" }
        timerr()

        globalEventChannel().subscribeMessages {
            // 收到 ???
            """^(?:查成分|/查成分)\s+(.+)""".toRegex() findingReply reply@{ match ->
                val (nc) = match.destructured
                try {
                    var uid : Long
                    var card : String
                    if (nc.toLongOrNull() == null) {
                        // 用户名获取uid
                        uid = getUidByName(nc)
                        if (uid == 0.toLong()) {
                            subject.sendMessage("解析失败 可能昵称不正确或使用uid")
                            return@reply null
                        }

                        card = getCard(uid)
                    } else {
                        // UID获取昵称 关注列表的UID 等字符串json信息 如果获取失败返回空
                        uid = nc.toLong()
                        card = getCard(nc.toLong())
                    }

                    val usercard = JsonParser.parseString(card).asJsonObject.getAsJsonObject("card")
                    if (usercard == null){
                        subject.sendMessage("解析失败 可能uid不正确")
                        return@reply null
                    }
                    val username = usercard.asJsonObject.get("name").asString
                    val useruid = usercard.asJsonObject.get("mid").asLong
                    val userface = usercard.asJsonObject.get("face").asString
                    val userfan = usercard.asJsonObject.get("fans").asString
                    val usercoin = usercard.asJsonObject.get("coins").asLong
                    val userregdate = convertTimestampToDateString(usercard.asJsonObject.get("regtime").asLong)
                    val follownum = usercard.asJsonObject.get("attention").asInt
                    val attrntions = usercard.asJsonObject.getAsJsonArray("attentions")
                    val cardmap = mutableListOf<Long>()
                    for (i in attrntions) {
                        cardmap.add(i.asLong)
                    }
                    if (cardmap.isEmpty()) {
                        subject.sendMessage("获取关注列表失败 可能未公开")
                        return@reply null
                    }
                    // 获取关注详细信息 并格式化
                    val guanzhulist = formatAttentions(cardmap)
                    // 获取粉丝牌信息
                    val FanMedalString = getFanMedal(uid)
                    // 格式化粉丝牌信息
                    val FanMedalList = formatFanMedal(FanMedalString)
                    // 读取vtb列表
                    val vtbString = getVtbList()
                    // 进行格式化
                    var vtbbList = formatVtb(vtbString)

                    val vtbuids = vtbbList.map { it["uid"] }
                    val fanuids = FanMedalList.map { it["uid"] }
                    val attentionuids = guanzhulist.map { it["uid"] }


                    val resultList = mutableListOf<HashMap<String, Any>>()
                    for (f in FanMedalList) {
                        if (f["uid"] in fanuids) {
                            resultList.add(f)
                        }
                    }
                    for (a in guanzhulist) {
                        if (a["uid"] !in fanuids) {
                            resultList.add(a)
                        }
                    }
                    for (item in resultList) {
                        if (item["uid"] in attentionuids && item["uid"] in vtbuids) {
                            item["color"] = "#F52887"
                        } else if (item["uid"] in attentionuids && item["uid"] !in vtbuids) {
                            item["color"] = "#000000"
                        } else if (item["uid"] !in attentionuids && item["uid"] in vtbuids) {
                            item["color"] = "#FAAFBE"
                        } else if (item["uid"] !in attentionuids && item["uid"] !in vtbuids) {
                            item["color"] = "#808080"
                        }
                    }
                    for (i in 0 until resultList.size) {
                        resultList[i]["index"] = i
                    }

                    val vmun = attentionuids.filter { it in vtbuids }
                    val percent = if (follownum != 0) {
                        String.format("%.2f", (vmun.size.toDouble() / follownum.toDouble()) * 100)
                    } else 0.00

                    val guanzhu_num = resultList.size
                    val num_per_col = if (guanzhu_num != 0) {
                        val numCols = Math.ceil(guanzhu_num.toDouble() / 80.0).toInt()
                        Math.ceil(guanzhu_num.toDouble() / numCols.toDouble()).toInt()
                    } else 1

                    val currenttime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                    val info = hashMapOf<String, Any>(
                        "username" to username,
                        "useruid" to useruid,
                        "userface" to userface,
                        "userfan" to userfan,
                        "usercoin" to usercoin,
                        "userregdate" to userregdate,
                        "userfollownum" to follownum,
                        "percent" to "$percent% (${vmun.size}/${follownum})",
                        "time" to currenttime,
                        "gz" to resultList,
                        "num_per_col" to num_per_col,
                        "filepath" to "${configFolderPath.toAbsolutePath()}\\${Config.font}".replace("\\","/")

                    )
                    // 将数据导入模板
                    val data = resolveConfigFile(Config.template).absolutePath
                    val template = PebbleEngine.Builder().build().getTemplate(data)
                    val writer = StringWriter()
                    template.evaluate(writer, info)
                    val content = writer.toString()

                    val tsnow = System.currentTimeMillis()
                    val cachepath = "${dataFolderPath.toAbsolutePath()}\\${tsnow}_${subject.id}.html"
                    File(cachepath).writeText(content)
                    //配置selenium 并截图
                    var chromeiumpath:String
                    var chromedriverpath:String
                    if(Config.chromium == ""){
                        chromeiumpath = "${configFolderPath.toAbsolutePath()}\\chrome-win\\chrome.exe"
                    }else{ chromeiumpath = Config.chromium }

                    if(Config.chromedriver == ""){
                        chromedriverpath = "${configFolderPath.toAbsolutePath()}\\chrome-win\\chromedriver.exe"
                    }else{ chromedriverpath = Config.chromium }

                    System.setProperty("webdriver.chrome.driver",chromedriverpath)
                    System.setProperty("webdriver.chrome.silentOutput", "true")
                    val chromeOptions = ChromeOptions()
                    chromeOptions.addArguments("-headless")
                    chromeOptions.setBinary(chromeiumpath)

                    val driver = ChromeDriver(chromeOptions)
                    driver.get(cachepath)

                    val maxWaitTimeInSeconds = Config.time
                    var startTime = System.currentTimeMillis()
                    var waittime = 1000
                    while (true) {
                        val pageLoadStatus = (driver as JavascriptExecutor).executeScript("return document.readyState") as String
                        if (pageLoadStatus == "complete" || (System.currentTimeMillis() - startTime) > maxWaitTimeInSeconds * 1000) {
                            break
                        }
                        TimeUnit.MILLISECONDS.sleep(waittime.toLong()) // 添加等待间隔
                    }

                    val width =
                        (driver as JavascriptExecutor).executeScript("return document.documentElement.scrollWidth") as Long
                    val height =
                        (driver as JavascriptExecutor).executeScript("return document.documentElement.scrollHeight") as Long
                    driver.manage().window().size = Dimension((width+1).toInt(), (height+1).toInt())

                    val srcFile = driver.getScreenshotAs(OutputType.FILE);

                    srcFile.toExternalResource().use { resource ->
                        val aa = subject.uploadImage(resource)

                        resource.close()
                        subject.sendMessage(aa)
                    }
                    driver.quit()

                    if(File(cachepath).exists()){
                        File(cachepath).delete()
                        return@reply null
                    }else{return@reply null}

                } catch (e: Exception){
                    logger.error(e)
                    subject.sendMessage("出错了")
                    return@reply null
                }
            }
        }
    }

    fun reloadConfig() {
        Config.reload()
        Config.save()
    }

    private fun timerr(){
        timer.schedule(10000,6*60*60*1000) {
            val scope = CoroutineScope(Dispatchers.Default)
            val job = scope.launch{
                try {
                    val status = updateVtbList()
                    if(status == "ture"){
                        logger.info("vtb列表更新完成")
                    }else{logger.error("vtb列表更新失败 $status")}

                }catch (e:Exception){
                    logger.error("vtb列表更新失败$e")
                }
            }
            runBlocking {
                job.join()
            }
            scope.cancel()
        }
    }

    override fun onDisable() {
        coroutineContext.cancelChildren()
        timer.cancel()

    }
}
