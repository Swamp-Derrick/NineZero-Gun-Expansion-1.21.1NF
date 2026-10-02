# NineZero's Gun Expansion — 1.21.1 NeoForge 移植

本项目以 NineZero 的 1.19.2 开发分支为基础，适配 createmeow 的 CGM 1.21.1 NeoForge 分支。保留 `nzgmaddon` 命名空间、21 把枪、1 种弹药、19 个配件、11 个创意栏外观变体及 52 个工作台配方。

## 安装

使用 Minecraft **1.21.1**、**NeoForge 21.1.228 或更高的 21.1.x**、Java **21**。将以下三个 JAR 放入同一个实例的 `mods` 文件夹，客户端和服务端均需安装：

- `nzgExpansion-neoforge-1.5.0-port.1+1.21.1.jar`
- 用户提供的 `cgm-1.4.4.jar`，即 createmeow 的非官方版本
- Framework **0.13.11 / Minecraft 1.21.1 / NeoForge**，构建依赖固定为 CurseForge 文件 `7530361`

完整依赖包内已提供这三个文件。已有相同版本依赖时保留一份即可。不要同时加载原来的 Forge 1.19.2 NZGE JAR，也不要混用 Forge 版或其他 Minecraft 版本的 Framework。

进入创意模式，打开 **NineZero's Gun Expansion** 标签页；生存模式通过 CGM 的武器工作台制作。首次试用建议新建测试世界。这里未向任何现有游戏实例或存档安装文件。

## 本次移植内容

- ForgeGradle / Java 17 改为 ModDevGradle / Java 21 / NeoForge 21.1.228。
- 迁移物品、声音和创意标签页注册；客户端入口与公共入口分离。
- 使用 1.21.1 的 `CustomData`、`CustomModelData` 保存弹药与外观数据，修改后明确写回组件。
- 迁移 `recipe`、`tags/item` 目录和 `c:` 公共材料标签。
- 增加 `nzgmaddon:workbench` 序列化器，仍使用 CGM 的工作台配方类型和界面。保留每条配方独立 ID、结果组件、材料标签与数量，避免 CGM 1.4.4 原序列化器丢弃外观数据和重复配方 ID 的问题。
- 迁移模型注册、渲染上下文、旋转 API；保留作者为普通 CGM 编写的射击机械运动和可拆配件模型。
- 迁移 `textures/items` 至 `textures/item`，修复 CGM 贴图、附魔开火字幕引用和猎枪缺失音效定义。
- 修复原模型中猎用霰弹枪及杠杆步枪四个模型文件的 `#missing` 面引用，改用同部件现有贴图。

## 兼容边界

目标是用户指定的 **CGM UnUnofficial 1.4.4**。原仓库还包含另一套 **CGM Expanded** 的专属接口和 `NZGE_Expanded` 数据包；目标 CGM 并未提供这套动画引擎。本次使用作者的普通 CGM 动画分支，不提供 Expanded 专属逐部件换弹、可换弹匣和其额外机制。`NZGE_Expanded` 文件夹保留作原始参考，不打入发布 JAR。

这是 `port.1` 测试移植版。自动测试已覆盖启动、数据、配方序列化、开火、装填和基础渲染；尚未逐项人工验证多人联机、所有瞄准镜视野、染色/附魔交互、长时间游玩和与其他模组共存。完整证据见 [TEST_REPORT.md](TEST_REPORT.md)。

## 构建与复测

安装 JDK 21。把用户提供的原始 `cgm-1.4.4.jar` 放入 `libs/`，然后执行：

```powershell
.\gradlew.bat build
.\gradlew.bat runGameTestServer
.\gradlew.bat runClient
```

构建文件位于 `build/libs/`。可选静态检查为 `python scripts/validate_resources.py`，只使用 Python 标准库。

执行 `powershell -ExecutionPolicy Bypass -File scripts/test.ps1` 会构建、运行五组服务器 GameTest，并复制自动测试世界到 `run/saves/NZGE-SmokeWorld` 后启动客户端渲染检查，完成后自动退出。需要可用的 OpenGL 图形环境。报告为 `artifacts/client-smoke-result.txt`，实机模型截图为 `artifacts/model-gallery.png`。

测试代码独立放在 `src/gameTest`，不会打入发布 JAR。`run/` 是本项目专用开发实例。

## 来源与版本

- NZGE fork：https://github.com/Swamp-Derrick/NineZero-Gun-Expansion-1.21.1NF
- NZGE 源码起点：`2b359710b57e572b7b74c6e593515d4cd05e5e25`
- CGM 参考源码：https://github.com/createmeow/MrCrayfishGunMod/tree/1.21.1
- CGM 参考提交：`00e6ec6de31f81fd46ec2476c03c8ec43d86c219`
- CGM 编译与测试均直接使用用户提供的 JAR；SHA-256：`50370fc5fdbd39406c8df50604324c4aa8c6c5c24e4a2aa38f057f6d3cbd9b4f`
- 用户提供的 1.19.2 JAR 用于内容交叉检查；资源以 fork 源码中的较新版本为准，原有全部 472 个资源路径均有对应内容。
- 原作者：zaeonNineZero；基础 CGM：MrCrayfish；许可保留在 [LICENSE](LICENSE)。
