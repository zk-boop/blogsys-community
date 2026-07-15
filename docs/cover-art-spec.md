# BlogSys 插图资产规格

## 统一视觉语言

- 用途：博客首页和文章详情的 `16:9` 封面
- 风格：现代编辑插画，清晰几何构图，轻微纸张质感，成人化且克制
- 调色：白、冷灰、炭黑与少量 Yves Klein Blue `#002FA7`
- 构图：缩略图尺寸仍可辨识，主体位于安全裁切区域
- 禁止：文字、Logo、水印、渐变、霓虹、光泽 3D、幼态卡通和模板化 AI 元素

## 资产清单

| 文件 | 用户画像/主题 | 画面规格 |
|---|---|---|
| `demo-design-system.png` | 前端工程师 / 设计系统 | 两位成年协作者整理界面片段、字型层级、色板和组件网格 |
| `demo-backend.png` | 后端工程师 / 数据库优化 | 工程师观察抽象查询路径与结构化数据行，突出更高效的索引路线 |
| `demo-city-walk.png` | 摄影作者 / 城市漫步 | 雨后旧城街道、日常行人与携带轻便相机的观察者 |
| `demo-home-cooking.png` | 家庭料理作者 | 普通家庭厨房里的一锅番茄蔬菜炖菜，实用而非广告式摆拍 |
| `demo-reading.png` | 阅读推广 / 跨年龄用户 | 图书馆角落中不同年龄的成年读者，安静、自然、包容 |
| `demo-hiking.png` | 旅行作者 / 新手徒步 | 温和林间步道、开阔草坡和携带日用背包的混龄徒步者 |
| `default-cover-v2.png` | 默认封面 | 笔记本、折页、铅笔与抽象对话形状构成的 Swiss 编辑静物 |

## 目标路径

- 文章封面：`project-boot/uploads/covers/`
- 新默认封面：`project-vue/src/assets/generated/default-cover-v2.png`
- 确认成品后，用新默认封面替换 `project-vue/src/assets/default-cover.png`

## 当前生成状态

- 已完成并检查：`default-cover-v2.png`、`demo-design-system.png`
- 等待网络稳定后补齐：`demo-backend.png`、`demo-city-walk.png`、`demo-home-cooking.png`、`demo-reading.png`、`demo-hiking.png`

新默认封面已经同步替换 `project-vue/src/assets/default-cover.png`。所有后续封面继续以上述清单作为唯一视觉基准，不通过复制、换色或无关图库图片补位。
