# Blog System Frontend Design System

## Direction

Chosen anchor: Swiss.

The blog is an editorial reading product, so the redesign uses a strict white-grid layout instead of a soft SaaS card style. The unexpected move is to treat the homepage and article list like an editorial index: large folio numbers, hard 1 px rules, black-and-white imagery that turns color on hover, and one deliberate Swiss red accent.

## Tokens

- Surface: `#FFFFFF`
- Neutral surface: `#F7F7F8`
- Ink: `#111111`
- Muted text: `#575757`
- Hairline: `#D8D8DC`
- Strong rule: `#111111`
- Accent: `#E4002B`
- Accent hover: `#B80022`
- Typography: `"Helvetica Neue", Arial, "PingFang SC", "Microsoft YaHei", sans-serif`
- Corners: `0`
- Shadows: none
- Structure: visible 1 px grid lines, left-aligned typography, asymmetric numbered sections

## Component Rules

- Header is a fixed editorial bar with hard cell divisions.
- Primary buttons use Swiss red with white text.
- Cards use 1 px black borders, no shadow, no rounded corners.
- Article cards use a three-zone index layout: number, text, image.
- Images are grayscale by default and become color on hover.
- Tags, inputs, dialogs, and tables inherit square corners and hairline borders.
- Page sections are unframed grid bands; individual repeated items may be bordered cards.

## Content Discipline

- Standard actions keep standard Chinese labels: 登录, 注册, 搜索, 重置.
- No fake telemetry, decorative subtitles, or themed replacements for normal UI copy.
- Homepage counts and lists come from API data.
- Empty states say 暂无文章, 暂无标签, or 暂无分类.

## Implemented Files

- `project-vue/src/assets/design-system.css`
- `project-vue/src/App.vue`
- `project-vue/src/components/layout/AppHeader.vue`
- `project-vue/src/components/layout/AppFooter.vue`
- `project-vue/src/components/ArticleCard.vue`
- `project-vue/src/views/Home.vue`
- `project-vue/src/views/Login.vue`
- `project-vue/src/views/Register.vue`
