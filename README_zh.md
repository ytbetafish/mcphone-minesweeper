# MCphone 扫雷

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

![logo](logo.png)

> 把 Windows XP 原版《扫雷》完整搬进 mcphone 手机屏幕的附属模组，像素级还原原版视觉与玩法，连音效都是原汁原味。

一个为 [mcphone](https://github.com/november521/mcphone) 制作的 NeoForge 客户端附属模组：在 mcphone 手机里提供完整可玩的 WinXP 扫雷。

阅读：[英文版](README.md) | 中文版

## 下载

- **CurseForge**：https://www.curseforge.com/minecraft/mc-mods/minesweeper-for-mcphone
- **Nexus Mods**：https://www.nexusmods.com/minecraft/mods/1347
- **GitHub Releases**：https://github.com/ytbetafish/mcphone-minesweeper/releases

## 功能特性

- 完整扫雷玩法：初级 9×9 / 中级 16×16 / 高级 30×16，支持自定义宽高与雷数
- 可变表情的笑脸（普通/按下/受惊/失败/胜利），点击随时重开
- LED 数码计数器：剩余雷数与用时，负数正确显示
- 完整游戏菜单：快速开局、?标记开关、颜色开关、声音开关、扫雷英雄榜、初/中/高级、自定义
- 自定义雷区对话框：加减按钮 + 数字输入，自动校验合法值（如 2×2 最多 3 雷）
- 旗子/问号工具按钮：mcphone 无法捕获右键，故做成互斥按钮，自绘按下状态
- 扫雷英雄榜：记录三个难度最佳成绩
- 原版音效（点击/失败/胜利）
- TNT 购买解锁（mcphone 应用商店购买，与原版应用一致）

## 环境要求

| 项目 | 版本 |
|---|---|
| Minecraft | 1.21.1（Java 21） |
| NeoForge | 21.1.200+ |
| mcphone | 1.10.2+（硬前置） |

## 安装

1. 安装前置模组 **mcphone**（v1.10.2+）：<https://github.com/november521/mcphone/releases>
2. 将 `mcphone_minesweeper-*.jar` 放入 `.minecraft/mods`
3. 启动游戏，在手机桌面打开「扫雷」，用 TNT 购买解锁

## 从源码构建

```bash
# 1. 将 mcphone-1.10.2-1.21.1-neoforge.jar 放入 libs/ 目录（见 libs/README.md）
# 2. 构建
./gradlew jar
# 产物：build/libs/mcphone_minesweeper-<版本号>.jar
```

## 目录结构

```
src/main/java/com/mcphoneminesweeper/  模组代码（页面/图集/价格）
src/main/resources/                   资源（贴图/音效/语言/元数据）
```

## 许可证

代码以 [MIT](LICENSE) 许可证开源。
- 贴图与布局参考 Windows XP 原版扫雷
- 音效提取自原版 winmine.exe，版权归微软所有

## 致谢

- [mcphone](https://github.com/november521/mcphone) —— 手机模组框架
- Windows XP 原版扫雷 —— 玩法与美术灵感来源
