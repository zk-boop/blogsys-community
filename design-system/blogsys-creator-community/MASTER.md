# BlogSys Creator Community — Design System

## Direction

BlogSys is a multi-user writing community inspired by the content-discovery structure of large creator platforms. It borrows interaction patterns—not branding or protected assets—from Bilibili: a strong search entry, compact channels, cover-led discovery, dense content feeds, rankings, and visible creation paths.

The visual anchor is **Swiss**. The interface remains restrained and suitable for a broad age range: neutral surfaces, one blue accent, a single sans-serif family, compact radii, clear hierarchy, and no decorative gradients.

## Signature move

Every public discovery page combines a horizontally scrollable channel strip with a cover-led content grid and a compact ranking rail. This is the recognizable community pattern across Home, Articles, Search, Category, and Tag pages.

## Tokens

- Canvas: `#F7F7F8`
- Surface: `#FFFFFF`
- Text: `#111111`
- Secondary text: `#575757`
- Faint text: `#737373`
- Line: `#E2E2E5`
- Strong line: `#C8C8CC`
- Accent: Yves Klein Blue `#002FA7`
- Accent wash: `#F1F3F8`
- Type: `Helvetica Neue`, Helvetica, Arial, `PingFang SC`, `Microsoft YaHei`, sans-serif
- Radius: 2–10px according to control size; never pill-shaped for primary actions
- Shadow: only restrained overlay elevation; ordinary cards use borders or no container
- Motion: color, opacity, and image crop only, 180–260ms; no layout-shifting scale on containers

## Information architecture

1. Global header: brand, primary search, core routes, category menu, account and creation entry.
2. Channel strip: real categories first, then real tags.
3. Discovery stage: one primary recommendation, up to two supporting recommendations, one creation entry.
4. Feed: responsive 3/2-column cover cards using real article data.
5. Ranking rail: real articles ordered by available engagement data.
6. Detail pages: reading width remains 760px; community actions sit after the article.

## Component rules

- Article covers use `16:9`; titles clamp to two lines.
- Metadata shows only fields returned by the API. Never fabricate authors, counters, or dates.
- All image controls require descriptive `alt` text.
- Cards that navigate must support pointer and keyboard activation.
- Search inputs require labels and submit on Enter.
- Dropdown menus remain open across pointer travel and keyboard focus.
- Primary creation actions use the single blue accent; secondary actions remain neutral.
- Empty states use plain standard language such as “暂无文章”.

## Responsive rules

- Validate at 375px, 768px, 1024px, and 1440px.
- Channel navigation scrolls horizontally rather than wrapping into multiple noisy rows.
- Feed: 3 columns on desktop, 2 on tablet/small mobile, with ranking moved above the feed below 820px.
- Supporting recommendations may collapse on narrow mobile, but the main recommendation and creation entry remain.
- No horizontal page overflow and no content hidden beneath the fixed header.

## Avoid

- Bilibili logos, mascots, illustrations, exact copy, or copied assets.
- Pink-blue brand replication, ornamental gradients, glow, glass effects, or oversized rounded cards.
- Huge editorial hero headlines that delay access to content.
- Full-page card walls, fake community metrics, emoji icons, and hover effects that shift layout.
