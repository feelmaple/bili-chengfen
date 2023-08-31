# bili-chengfen
***
一个可以查B站用户关注列表的简陋mirai插件<br>
使用`pebble`动态生成网页<br>
使用`selenium`驱动`chromium`进行图片生成<br>
改编自[MeetWq](https://github.com/MeetWq)大佬的[nonebot-plugin-ddcheck](https://github.com/noneplugin/nonebot-plugin-ddcheck)
### 使用方法
***
将jar文件放到插件目录下<br>
运行mirai<br>
将压缩包解压放到配置目录下`com.feelmaple.bili-chengfen`<br>
更改`BiliConfig.yml`中的配置`cookie` `UA`等<br>
使用`/ccf reload`重载配置<br>

### 指令
***
`查成分 <用户名或UID>` 或 `/查成分 <用户名或UID>`<br>
例如<br>
`查成分 123` `/查成分 陈睿`
***
`/ccf update` 手动更新vtb列表<br>
`/ccf reload` 重载配置<br>

### 其他
***
压缩包中的`info.html`是网页模板，通过该模板使用`pebble`进行动态生成，可以自行更改<br>
该模板来自[nonebot-plugin-ddcheck](https://github.com/noneplugin/nonebot-plugin-ddcheck)<br>
使用的`selenium`版本为`4.8.1`<br>
如自定义`chromium及其驱动`请注意支持的`chromium及其驱动`版本<br>
默认的chromium路径为`...\config\com.feelmaple.bili-chengfen\chrome-win\chrome.exe`<br>
默认的chromium驱动路径为`...\config\com.feelmaple.bili-chengfen\chrome-win\chromedriver.exe`<br>
*代码水平低😂，可自行修改使用*<br>
应该没其他的了<br>

## 感谢
***
[mirai](https://github.com/mamoe/mirai)<br>
[MeetWq](https://github.com/MeetWq)-[nonebot-plugin-ddcheck](https://github.com/noneplugin/nonebot-plugin-ddcheck)<br>
[MrXiaoM](https://github.com/MrXiaoM)-[LoliYouWant](https://github.com/MrXiaoM/LoliYouWant)<br>
[cssxsh](https://github.com/cssxsh)-[Mirai Example](https://github.com/cssxsh/mirai-example)<br>




