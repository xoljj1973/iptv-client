# IPTV Client（直播 + EPG 节目单客户端）

IPTV 播放器：M3U 直播源 + XMLTV 节目单（EPG），频道列表显示当前正在播的节目，播放时顶部叠加"频道名 + 正在播放"。

## 使用

1. 打开应用，点「直播源」填入 M3U 地址（`#EXTINF` 里的 `tvg-id` 用于匹配 EPG）
2. 点「EPG」填入 XMLTV 地址（通常是 `*.xml` 或 `*.xml.gz` 解压后的地址）
3. 频道列表第二行会显示当前节目；TV 遥控器上下键/频道键换台

## 构建

- 本地：`gradle assembleDebug`（需 JDK 17 + Android SDK）
- 云端：推送到 GitHub 后 Actions 自动打包，在 Artifacts 下载 APK

## 技术栈

Kotlin + Media3/ExoPlayer（HLS）+ XmlPullParser（XMLTV 解析），minSdk 21（Android 5.0+），手机与 Android TV 双端（Leanback 启动器入口已声明）。
