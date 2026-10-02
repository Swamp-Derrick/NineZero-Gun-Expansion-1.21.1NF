# NineZero’s Gun Expansion — Minecraft 1.21.1 / NeoForge

这是 NineZero’s Gun Expansion 的 NeoForge 移植分支，适配 **createmeow 的 CGM UnUnofficial 1.4.4**。

当前版本：`1.5.0-port.1+1.21.1`（测试移植版）。包含 21 把枪、1 种弹药、19 个配件、11 个外观变体及 52 个工作台配方。

- [安装说明、移植内容与兼容边界](PORTING.md)
- [实际测试结果及未验证事项](TEST_REPORT.md)
- [原始更新记录](Changelog.md)
- [GPL 许可](LICENSE)

## 运行要求

Minecraft 1.21.1、NeoForge 21.1.228+（21.1.x）、Java 21、CGM UnUnofficial 1.4.4、Framework 0.13.11 NeoForge。

## 从源码构建

将目标 CGM JAR 放到 `libs/cgm-1.4.4.jar`，运行：

```powershell
.\gradlew.bat build
```

产物在 `build/libs/`。运行 `scripts/test.ps1` 可执行服务器 GameTest 和独立测试世界的客户端渲染检查；测试源码不会打入成品 JAR。

原作及模型/音效归 zaeonNineZero 和相应原作者所有；基础 CGM 由 MrCrayfish 开发。此分支不包含 CGM Expanded 独立引擎的专属功能，保留 NZGE 的普通 CGM 动画分支。
