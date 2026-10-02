# NZGE-Unofficial 基础测试报告

测试日期：2026-10-02（Australia/Sydney）。环境：Windows 11、Microsoft OpenJDK 21.0.10、Minecraft 1.21.1、NeoForge 21.1.228、CGM 1.4.4、Framework 0.13.11。

0.1.1 更名发布时重新运行构建及五组服务器 GameTest，全部通过。以下客户端与视觉检查来自前一版相同功能代码；本次只调整名称、版本和文档。

## 已通过

| 检查 | 实际覆盖 |
| --- | --- |
| Gradle 构建 | 编译、NeoForge 元数据、发布 JAR 和 sources JAR |
| 静态资源校验 | 309 个 JSON/元数据文件；模型、贴图、音效引用；注册数量和配方 ID |
| `gunsAndRegistries` | 41 个物品、21 把枪均从数据包加载，弹药物品有效，基础参数有效，枪械物品保存/读取一致 |
| `recipesAndNetwork` | 52 条配方；工作台 ID 唯一；所有材料标签可解析；配方经过实际网络 codec 往返后仍保留变体和材料 |
| `creativeVariants` | 11 个创意栏变体有弹药，物品网络 codec 往返后数据一致 |
| `allGunsFireAndConsumeAmmo` | 使用服务器 FakePlayer 调用 CGM 实际开火处理器；21 把枪的弹药各减少 1，产生的弹丸数量符合各自参数 |
| `allGunsReloadFromInventory` | 21 把枪调用 CGM 实际 ReloadTracker tick 逻辑，使用各自弹药装满弹仓，并消耗相应背包弹药 |
| 客户端与整合服务器启动 | 进入独立测试世界，客户端已同步左轮容量等枪械数据 |
| 模型与渲染 | 154 个特殊模型加载、实际模型面未使用 missing sprite；52 个基础/变体物品模型存在；真实玩家环境下 105 次 GUI/第一人称/第三人称 override 渲染调用；矩阵栈平衡 |
| 视觉查看 | 查看游戏实际渲染的 52 格模型图，无紫黑缺失贴图或空白物品；见 `artifacts/model-gallery.png` |

服务器输出：`All 5 required tests passed`。

客户端输出：`PASS: 154 special models; 52 inventory stacks; 105 override render calls; player/world=true.`

## 在测试中发现并修复

1. 旧贴图路径没有进入新版默认图集，并且 CGM 引用仍使用旧路径。
2. 旧配方目录、Forge 标签，以及物品 NBT 写法不符合 1.21.1。
3. CGM 原工作台序列化器会丢弃结果组件，且多个外观配方共享 ID。
4. 猎用霰弹枪和杠杆步枪的原模型残留 `#missing` 面。
5. 附魔开火字幕引用和一个已注册上膛声音缺少有效定义。

## 限制与剩余验证

- 渲染调用测试可捕获崩溃、缺失模型/贴图和矩阵失衡，不证明所有动画动作、瞄准镜对齐和视觉手感均正确。
- 开火测试验证服务器处理器、弹丸数量和耗弹；尚未逐枪测量实体命中伤害、爆头判定和弹道平衡。
- 装填测试直接推进 CGM 的服务器 tick 处理器，未模拟真人按键输入与网络延迟。
- 未做两台客户端连接专用服务器的多人测试、全部配件/附魔/染色组合测试，亦未做其他模组兼容性矩阵。
- 目标 CGM JAR 自带 `cgm:sounds/SOUND-LICENSE.txt` 大写资源路径日志错误；该文本被游戏忽略，不影响本次通过的测试。测试保持用户 CGM JAR 原样。
- CGM Expanded 的专属动画引擎不在本次目标中，详见 [PORTING.md](PORTING.md)。

原始日志：`artifacts/build.log`、`artifacts/gametest.log`、`artifacts/client-smoke.log`。开发测试源码不会包含在成品 JAR。
