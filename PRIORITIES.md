# 实验一 · 团队优先事项（PRIORITIES）

> 本文档由 PO 定义整体优先事项，SM 解析转化后发布为 GitHub Milestones 与 Issues。仓库：`LIfe-evoe/f26-lab01`（fork 自 CMU-17-214/f26-lab01）。
> 完成标准以《实验一_课程基础设施与智能体任务_实验指导书》为准；分工清单参考团队 PPT（步骤 0 检查、里程碑 2 小步提交→DV、代码审查→SM）。

## 1. 目标

完成实验一全部检查点：跑通工具链，沿调用链修复预设缺陷，实现 cancelBooking，识别并处理错误的智能体补丁，确认会话导出与工具声明。满分 100：环境/课前 10 + 里程碑 1 30 + 里程碑 2 35 + 里程碑 3 25。

## 2. 执行顺序（优先级 P0→P4，对应 GitHub Milestones）

| 优先级 | Milestone | 完成标志 | 主要角色 |
| --- | --- | --- | --- |
| P0 | 步骤 0 环境准备 | fork/remote 正确；4 条命令能跑；mvn test 恰好 1 个失败 | SM1（仓库）+ DV（环境） |
| P1 | 里程碑 1 诊断修复 | 根因定位到仓储层并修复；全绿；回归测试；能讲清"哪里坏了" | DV 修复，SM1 审查 |
| P2 | 里程碑 2 cancelBooking | 实现满足 R1～R6；2～3 个小提交；晋升测试在"只删除"实现上失败 | DV 实现，SM1 逐轮审查 |
| P3 | 里程碑 3 审查补丁 | 识别 isAvailable 违背不变式；失败测试；修复合并或拒绝保留分支 | SM2 分支操作，DV 测试/修复，PO 决策 |
| P4 | 课前任务 | transcripts 导出未提交；README 声明；全部提交已推送 | SM2 收尾，SM1 push |

## 3. 团队分工

| 角色 | 成员 | 职责 |
| --- | --- | --- |
| PO | 组长 | 定义优先事项与验收标准（R1～R6、检查点）；里程碑 3"修复 or 拒绝"决策 |
| SM1 | 本机操作者（肖） | GitHub 运营主责：fork、remote、push；里程碑 1/2 的 diff 审查与小步提交把关 |
| SM2 | 另一名 SM | 里程碑 3 分支与补丁操作；课前任务收尾（transcripts 检查、README 声明）；验收复核（预留任务见 §5） |
| DV | 实现工作者（2 人） | 按 GitHub Issues 从上往下领取任务实现；代码编辑、写测试、提交推送；两人自行商量拆分 |

## 4. GitHub 发布结构（SM 解析转化）

SM 已将本文件转化为仓库内的 **5 个 Milestones** 与 **Issues**（标题带 `[角色]` 前缀，便于领取与过滤）：

- **Milestone：步骤 0 环境准备**
  - [SM] fork 与 clone、git remote -v 确认 origin 指向 fork
  - [SM] 4 条命令能跑：java --version / mvn --version / git --version / claude --version
  - [DV] mvn test 运行，确认恰好 1 个失败（预设缺陷）
- **Milestone：里程碑 1 诊断修复**
  - [DV] 沿调用链追踪失败测试，填写诊断记录表
  - [DV] 修复根因（仓储层 bookingsForRoom 只返回本房间预订）+ 回归测试 + 提交
  - [SM1] 审查修复 diff（附录 C 清单）
- **Milestone：里程碑 2 cancelBooking**
  - [DV] 读 TASK.md，设计 S1～S6 场景期望
  - [DV] 智能体分步实现：提交 1（只删除 + 无效 id，附 S1/S2 测试）
  - [SM1] 审智能体每轮 diff（9 项清单：是否处理晋升、按 seq、至多一人、不跨房间、复用 overlaps 等）
  - [DV] 实现晋升逻辑：提交 2（附晋升测试）
  - [SM1] 小步提交把关：每 commit 后 mvn test 绿；git log --oneline 能看到 3 笔
  - [DV] 补充 S4～S6 边界测试：提交 3
- **Milestone：里程碑 3 审查补丁**
  - [SM2] 建 agent-attempt 分支、git apply 补丁、提交
  - [DV] 分析 9 种位置关系表格（真实冲突 vs 补丁返回值）
  - [DV] 写暴露问题的测试（IsAvailableTest，红）
  - [SM2] 审查 isAvailable 是否违背不变式
  - [PO] 决策：修复 or 拒绝
  - [DV] 修复（复用 TimeInterval.overlaps）或配合拒绝
  - [SM2] 合并回 main 或保留分支不合并
- **Milestone：课前任务**
  - [SM2] 确认 transcripts/ 已导出且未被提交（git check-ignore -v transcripts）
  - [DV] README 填写"工具与模型"声明
  - [SM1] git push origin main（推送全部提交，transcripts 不推送）

## 5. SM 分工与 SM2 预留任务

两个 SM 按里程碑切分，互不重叠：

| 任务 | 归属 | 说明 |
| --- | --- | --- |
| fork、clone、remote 确认、push | SM1 | 已部分完成 |
| 里程碑 1 修复 diff 审查 | SM1 | 对照附录 C |
| 里程碑 2 逐轮 diff 审查 + 小步提交把关 | SM1 | 9 项审查清单 + mvn test/log 检查 |
| 里程碑 3 分支创建、git apply、审查、合并/拒绝操作 | **SM2（预留）** | 指导书 3.2/3.4 分支操作全程 |
| 课前任务 transcripts 检查与 README 确认 | **SM2（预留）** | git check-ignore + 声明核对 |
| 验收复核（三个里程碑演示前自检） | **SM2（预留）** | 附录 D 自查清单 |

> 说明：SM2 的预留任务已在 GitHub 以 `[SM2]` 前缀的 Issue 发布，SM2 可直接领取；SM1 负责已有任务与整体推进。

## 6. 提交与推送纪律

- 每次提交只做一件能用一句话说清楚的事；提交前 `git diff --staged` 复查
- 每个提交后 `mvn test` 保持全绿（里程碑 2 至少 3 个提交）
- **禁止**提交或推送 `transcripts/`（fork 是公开的）
- 分支约定：里程碑 3 在 `agent-attempt` 分支试验；修复后合并回 `main` 或拒绝后保留分支
- README 声明工具与模型后再 push

## 7. 完成前自查（附录 D 精简，全员对照）

- [ ] 已 fork 而非直接 clone；git remote -v 指向 fork
- [ ] JDK 21+、Maven 3.8+、Git、IDE、智能体工具可用
- [ ] 能说出里程碑 1 真正根因并已在正确层修复
- [ ] 全部测试为绿，能论证核心不变式成立（建议回归测试）
- [ ] cancelBooking 满足 R1～R6；已逐项审查智能体 diff
- [ ] 里程碑 2 由几个小提交组成；晋升测试在"只删除"实现上失败
- [ ] agent-attempt 分支已应用补丁、有暴露问题的测试
- [ ] 已修复并合并回 main，或已拒绝保留分支；当前位于 main
- [ ] 本地 transcripts/ 有导出且未被提交推送
- [ ] README 声明工具与模型；所有提交已推送
