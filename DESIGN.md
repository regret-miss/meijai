---
name: "湘韵甲艺双端美甲平台"
description: "以暖白纸张为共同材质、让运营工作室与东方美甲内容各守边界的双端设计系统"
colors:
  admin-porcelain: "#f4f1ee"
  admin-paper: "#ffffff"
  admin-paper-soft: "#fbfaf8"
  admin-ink: "#24211f"
  admin-muted: "#8f8984"
  admin-line: "#e7e1dc"
  admin-rose: "#a66d78"
  admin-blush: "#f7eeef"
  admin-sand: "#f5f0e7"
  admin-charcoal: "#332e2b"
  public-paper: "#f5f1e8"
  public-paper-light: "#fbf8f0"
  public-ink: "#25231f"
  public-ink-soft: "#625d53"
  public-cinnabar: "#9e3027"
  public-cinnabar-dark: "#72221d"
  nav-cinnabar: "#a53229"
  public-indigo: "#29485a"
  public-jade: "#587267"
  public-gold: "#b3975f"
  public-ai-page: "#f2f3f5"
  public-ai-rose: "#b97987"
typography:
  admin-display:
    fontFamily: "Noto Sans SC, Microsoft YaHei, system-ui, sans-serif"
    fontSize: "27px"
    fontWeight: 640
    lineHeight: 1.15
    letterSpacing: "-0.035em"
  admin-headline:
    fontFamily: "Noto Sans SC, Microsoft YaHei, system-ui, sans-serif"
    fontSize: "24px"
    fontWeight: 650
    lineHeight: 1.25
    letterSpacing: "-0.025em"
  admin-title:
    fontFamily: "Noto Sans SC, Microsoft YaHei, system-ui, sans-serif"
    fontSize: "14px"
    fontWeight: 640
    lineHeight: 1.4
  body:
    fontFamily: "Noto Sans SC, Microsoft YaHei, system-ui, sans-serif"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: 1.6
  editorial-label:
    fontFamily: "Georgia, Times New Roman, serif"
    fontSize: "9px"
    fontWeight: 400
    lineHeight: 1.3
    letterSpacing: "0.16em"
  public-display:
    fontFamily: "HYShangWeiShouShuW, STKaiti, KaiTi, FangSong, serif"
    fontSize: "clamp(32px, 3.2vw, 44px)"
    fontWeight: 600
    lineHeight: 1.15
  public-body:
    fontFamily: "Noto Serif SC, Songti SC, SimSun, serif"
    fontSize: "16px"
    fontWeight: 400
    lineHeight: 1.9
rounded:
  hairline: "1px"
  public-control: "4px"
  nav-control: "10px"
  public-card: "14px"
  pill: "999px"
spacing:
  xs: "4px"
  sm: "8px"
  control: "10px"
  md: "14px"
  lg: "18px"
  xl: "24px"
  section: "34px"
components:
  admin-button-primary:
    backgroundColor: "{colors.admin-charcoal}"
    textColor: "{colors.admin-paper}"
    typography: "{typography.body}"
    rounded: "{rounded.hairline}"
    padding: "11px 16px"
  admin-button-secondary:
    backgroundColor: "{colors.admin-paper}"
    textColor: "{colors.admin-ink}"
    typography: "{typography.body}"
    rounded: "{rounded.hairline}"
    padding: "10px 13px"
  admin-card:
    backgroundColor: "{colors.admin-paper}"
    textColor: "{colors.admin-ink}"
    rounded: "{rounded.hairline}"
    padding: "18px"
  public-button-primary:
    backgroundColor: "{colors.public-cinnabar}"
    textColor: "{colors.public-paper-light}"
    typography: "{typography.public-body}"
    rounded: "{rounded.public-control}"
    padding: "12px 22px"
  public-card:
    backgroundColor: "{colors.public-paper-light}"
    textColor: "{colors.public-ink}"
    rounded: "{rounded.public-control}"
    padding: "20px"
---

# Design System: 湘韵甲艺双端美甲平台

## Overview

**Creative North Star: "双面纸本工坊 / The Two-Sided Paper Atelier"**

整套系统像一间拥有前店与后坊的纸本美甲工作室：后台是暖白瓷纸上的运营台，以可扫描、可追踪和可审计为优先；前台是带湘地手书与朱砂印记的内容空间，以克制的东方叙事承载真实作品。两端共享纸张材质、细线、墨色层级和真实图片资产，但不共享页面构图、导航壳层或装饰语法。

后台壳层、工作台、AI 创作与设计详情属于同一个 Operate 世界：近直角模块、紧凑编辑标签、灰褐细线与少量灰玫瑰焦点，让异步任务、参考资产和采纳动作保持清楚。前台正文以 `G:\desktop\美甲网页（最终版）1` 为内容和视觉基准，保留 `G:\desktop\美甲` 的现有固定顶部导航，并把独立 AI 页面视为面向访客的专用工作区，而不是后台页面的复制品。

**Key Characteristics:**

- 暖白纸张是共同材质；后台偏瓷白与灰褐，前台偏宣纸与朱砂。
- 后台近直角、高密度、状态优先；前台留白更大、手书标题与作品图优先。
- 灰玫瑰只承担后台选择、焦点和创作提示；朱砂只承担前台品牌、导航与行动。
- 前后台共享后端任务、资产来源和 Ark 生图工作流，不共享壳层与视觉组件。
- 所有图片、任务数、结果状态和版权说明均来自真实数据，不用虚构背书填充界面。

## Colors

后台以暖瓷白、纸白、墨褐和灰玫瑰形成低噪声操作环境；前台以宣纸、墨色、朱砂为主，并用靛青、玉色与旧金作极少量东方辅助色。

### Primary

- **工作室灰玫瑰** (`admin-rose`): 后台激活菜单、焦点轮廓、选中卡片与编辑标签；单屏保持稀少，不能铺成大面积背景。
- **湘印朱砂** (`public-cinnabar`): 前台正文中的品牌行动、标题装饰、筛选选中和价格强调。
- **导航朱砂** (`nav-cinnabar`): 仅用于现有顶部导航的方印、指示线与悬停语义，保留其比正文朱砂略亮的现状。

### Secondary

- **窑砂米色** (`admin-sand`): 后台参考图创作入口及轻量分区，不表达警告。
- **湖湘靛青** (`public-indigo`): 前台少量文化辅助信息，不与朱砂争夺主行动。
- **青玉绿** (`public-jade`): 前台分类或属性标签；不得代替成功状态的语义色。

### Tertiary

- **旧金** (`public-gold`): 前台分隔装饰、星级与手工质感点缀；避免金属渐变与大面积金色面板。
- **柔粉纸** (`admin-blush`): 后台描述生成入口、选中任务与参考关系的轻底色。
- **访客 AI 灰玫瑰** (`public-ai-rose`): 独立前台 AI 工作页的选项、焦点和进度强调，与前台导航朱砂并存但不互换。

### Neutral

- **后台暖瓷底** (`admin-porcelain`) 与 **后台纸白** (`admin-paper`): 分别用于应用画布和工作模块。
- **后台墨褐** (`admin-ink`) 与 **后台灰褐** (`admin-muted`): 分别用于标题/关键数据和说明/元信息。
- **后台细褐线** (`admin-line`): 所有后台模块、行和控件的主要分隔方式。
- **炭墨操作色** (`admin-charcoal`): 后台“开始新设计”“生成提案”“继续迭代”等主操作。
- **前台宣纸** (`public-paper`) 与 **净宣纸** (`public-paper-light`): 前台正文画布和卡片表面。
- **前台浓墨** (`public-ink`) 与 **前台淡墨** (`public-ink-soft`): 前台标题、正文和次要说明。
- **访客 AI 冷纸底** (`public-ai-page`): 仅用于独立 AI 工作页外层，不能蔓延到普通前台正文。

**The Two Accents Rule.** 后台灰玫瑰和前台朱砂属于不同壳层；同一组件不得同时使用两者。

**The Paper Before Color Rule.** 先用纸色层级和细线组织信息，再用强调色标记状态或行动；强调色不得承担大面积结构分区。

## Typography

**Display Font:** 后台使用 Noto Sans SC / Microsoft YaHei；前台使用 HYShangWeiShouShuW，并回退到 STKaiti / KaiTi。

**Body Font:** 后台使用 Noto Sans SC / Microsoft YaHei；前台正文使用 Noto Serif SC / Songti SC / SimSun。

**Label Font:** 后台英文眉题与编号使用 Georgia / Times New Roman；前台导航和说明使用宋体体系。

**Character:** 后台字体是利落、紧凑的运营语言，数字和标题稳定易扫；前台标题像手书题签，正文则像安静的宋体图录。书法字体只进入品牌与展示层，不进入任务状态、表单值或长段正文。

### Hierarchy

- **后台 Display** (`admin-display`): 工作台和 AI 创作页一级标题，通常一屏仅一个。
- **后台 Headline** (`admin-headline`): 设计档案名称和详情主标题，允许两行截断但不得缩成标签字号。
- **后台 Title** (`admin-title`): 模块标题、统计标题和卡片关键名称。
- **Body** (`body`): 后台字段、任务说明和操作文本；任务详情正文可提高到 14px、1.8 行高。
- **Editorial Label** (`editorial-label`): `STUDIO OPERATIONS`、`DESIGN BRIEF`、编号等短标签，保持大写、宽字距和极短长度。
- **前台 Display** (`public-display`): 品牌名、页面主标题和内容区大标题。
- **前台 Body** (`public-body`): 品牌叙事、作品描述和服务说明，正文行宽宜控制在约 34–40em。

**The Handwriting Is a Seal Rule.** 手书字体像印章一样稀有：用于品牌与大标题，不用于按钮、选择器、状态或密集数据。

**The Label Is Not Copy Rule.** 9px 编辑标签只作索引，不能承载用户必须读懂的说明或错误原因。

## Layout

后台内容容器最大宽度为 1240px，位于固定 LikeAdmin 侧栏与顶部壳层之内。工作台以 4 列指标条和约 `2fr / 0.72fr` 主次网格组织；AI 创作与详情以 258px 任务轨加可伸缩主舞台组织。模块间距以 10px 为主，内部常用 18px、24px、30–34px；这是“编辑桌”密度，不应被放大成营销落地页间距。

后台在 1100px 以下把任务轨转为顶部横向记录带，在约 820px 以下把工作台主次栏折为单列，在 760px 以下把模式、结果与底部操作折为单列。移动端仍保留任务状态、参考来源和提交动作，不通过隐藏关键信息来获得整洁。

前台正文沿用最终版最大 1200px 容器、较大的章节留白和真实图片主导的内容布局。现有顶部导航高 76px，980px 以下切换为菜单面板，720px 以下缩为 68px；该导航继续跨普通前台页面和独立 AI 页面。独立 AI 页面采用左记录、中创作、右参数三栏，1260px 以下将参数移到下方，930px 以下整体单列。

**The Separate Shells Rule.** 后台路由、侧栏和多标签头永远不进入前台；前台品牌导航永远不嵌进后台工作台。

**The Shared Workflow Rule.** 两端可以呈现同一任务、结果和资产来源，但必须使用各自的布局密度、导航与文案层级。

## Elevation & Depth

后台默认扁平，以纸色差与 1px 细线构造深度；只有悬浮迭代输入条使用环境阴影，结果图靠裁切和边框而非卡片浮层。前台允许更明显但柔和的纸张阴影：普通卡片低抬升，图片或移动菜单使用更扩散的环境阴影。阴影始终是结构或交互反馈，不是装饰光晕。

### Shadow Vocabulary

- **后台悬浮输入** (`0 18px 42px rgba(72, 56, 49, .11)`): 仅用于贴近视口底部的迭代创作条。
- **前台纸卡低抬升** (`0 1px 2px rgba(83, 72, 53, .04), 0 4px 16px rgba(83, 72, 53, .06)`): 普通内容卡片。
- **前台纸卡悬停** (`0 2px 4px rgba(83, 72, 53, .04), 0 12px 32px rgba(83, 72, 53, .12)`): 可点击卡片的悬停反馈。
- **导航毛玻璃** (`0 8px 28px rgba(45, 47, 44, .06)`): 现有前台顶部导航滚动后的轻环境阴影，并配合半透明纸色与 16px 模糊。

**The Flat Atelier Rule.** 后台静止模块不投影；只有真正悬浮在内容上方的输入或临时层可以有阴影。

## Shapes

后台的主体语言是近直角：工作台模块、任务条目、趋势选择、按钮、结果卡与图片裁切统一使用 1px 圆角，圆形仅留给模式图标、头像、状态点和工作室徽记。边框使用 1px 灰褐细线；虚线仅表示可添加参考资产的投放区。

前台正文以 4px 控件/卡片圆角为基准，现有顶部导航的方印与菜单按钮保留 10px 圆角；独立 AI 工作页可使用约 8–15px 的柔和工作区圆角。前台作品图可以使用 14px 纸卡轮廓，但不能把所有内容都变成胶囊。

**The Circle Has Meaning Rule.** 圆形只表示印章、身份、状态或模式；普通文本操作不得无条件做成胶囊。

**The Almost-Square Admin Rule.** 新增后台组件优先使用 1px 圆角；不要恢复通用 SaaS 的 12–16px 圆角卡片。

## Components

### Buttons

- **后台 Primary:** 炭墨底、纸白字、近直角，内部横向留白约 16px；承载生成、开始设计与继续迭代。
- **后台 Hover / Focus:** 悬停只做轻微色阶变化；键盘焦点统一为 2px 灰玫瑰轮廓、2px 外偏移；禁用态降低透明度但保留标签可读性。
- **后台 Secondary / Ghost:** 纸白或透明底、细褐线、墨褐文字，用于刷新、返回与资产入口。
- **前台 Primary:** 朱砂底、净宣纸字、4px 圆角；悬停转深朱砂，按下轻微下移或缩放。
- **前台 Secondary:** 透明或宣纸底、朱砂描边；不得使用后台灰玫瑰。

### Chips

- **后台 Style:** 趋势方向使用近直角纸白片，顶部色带表达预设；选中以灰玫瑰双线感和极淡粉纸强调。
- **后台 State:** 任务状态使用小色点或紧凑状态片；成功为沉静绿、进行中为赭金、失败为暗红，状态色不覆盖整张任务卡。
- **前台 Style:** 分类标签使用 3–4px 圆角，朱砂表示选中，玉色用于属性；避免彩虹标签集合。

### Cards / Containers

- **后台:** 纸白表面、1px 细褐线、1px 圆角、无静态阴影；18px 是普通内部留白，详情主区可用 24–34px。
- **前台:** 净宣纸表面、4px 圆角与低抬升阴影；以图片和题签组织内容，悬停最多上移 3–4px。
- **结果卡:** 图片保持真实比例或 1:1 结果预览，底部才放状态与操作；不得在图上叠加虚构质量评分。

### Inputs / Fields

- **后台 Brief:** 无外框的大文本区，用底部细线或分区边界组织；参考资产投放区使用细虚线和明确的版权/来源提示。
- **后台 Focus:** 灰玫瑰轮廓或边框，不用发光渐变。
- **前台正文表单:** 宣纸透明底与底线/4px 细框，焦点转朱砂或旧金。
- **前台 AI 参数:** 允许更紧凑的冷纸输入框和访客 AI 灰玫瑰焦点，但必须保留现有品牌顶部导航。

### Navigation

- **后台:** LikeAdmin 左侧栏保持 188px，活动项为极淡玫瑰底、左侧灰玫瑰标线；顶部壳层为纸白与细线，多标签活动态延续同一语言。
- **前台:** 固定顶部导航保留方形朱砂“湘”印、手书品牌名、宋体菜单和底部朱砂指示线；滚动后出现半透明宣纸与轻模糊。
- **Mobile:** 两端都必须提供可见菜单触发、键盘焦点和关闭状态；不能仅靠悬停揭示导航。

### Task Rail & Design Archive

任务轨是后台 AI 世界的签名组件：固定宽度、纵向可滚动、44px 方形封面、单行标题、时间/结果元信息和 7px 状态点。选中项使用极淡粉纸与灰玫瑰细线，不使用投影。前台独立 AI 页可采用更柔和圆角的记录轨，但状态、标题、封面和任务标识必须与后端数据一致。

## Do's and Don'ts

### Do:

- **Do** 让后台壳层、工作台、AI 创作和详情始终使用同一套暖瓷纸、细褐线、近直角和灰玫瑰焦点。
- **Do** 以前台最终版正文为内容与东方视觉基准，同时保留现有顶部导航和独立 AI 页面。
- **Do** 在两端展示相同的真实任务状态、参考资产版权、结果来源与采纳关系。
- **Do** 用排版、纸色和 1px 分隔线先建立层级，再少量使用品牌色。
- **Do** 为键盘焦点、窄屏折叠、减少动态偏好、加载、失败和空状态保留完整表达。

### Don't:

- **Don't** 把前台朱砂导航或手书标题移植进后台操作壳层。
- **Don't** 把后台侧栏、任务轨、Element Plus 控件或灰玫瑰主色直接复制到普通前台正文。
- **Don't** 使用通用 AI 紫蓝渐变、霓虹发光、玻璃卡片堆叠或大面积饱和彩色面板。
- **Don't** 给后台新组件套用 12–16px 的通用 SaaS 圆角；近直角是当前实现的系统承诺。
- **Don't** 虚构客户评价、生成成功率、商业背书、任务数量或资产版权信息。
- **Don't** 为了视觉整洁隐藏任务失败原因、参考来源、状态轮询或采纳动作。
