<template>
  <div class="home-page">
    <section class="channel-strip" aria-label="内容频道">
      <router-link to="/articles" class="channel-primary">全部文章</router-link>
      <router-link
        v-for="category in hotCategories"
        :key="category.id"
        :to="`/articles/category/${category.id}`"
        class="channel-link"
      >
        {{ category.name }}
      </router-link>
      <span class="channel-divider" aria-hidden="true"></span>
      <router-link
        v-for="tag in hotTags.slice(0, 5)"
        :key="tag.id"
        :to="`/articles/tag/${tag.id}`"
        class="channel-tag"
      >
        {{ tag.name }}
      </router-link>
    </section>

    <section v-if="featuredArticles.length" class="discovery-grid" aria-labelledby="featured-title">
      <h1 id="featured-title" class="sr-only">推荐文章</h1>

      <button class="lead-story" type="button" @click="goToArticle(featuredArticles[0].id)">
        <img
          :src="featuredArticles[0].coverImage || defaultCoverImage"
          :alt="`${featuredArticles[0].title}封面`"
          @error="handleCoverError"
        >
        <span class="story-shade"></span>
        <span class="lead-copy">
          <span class="story-label">推荐阅读</span>
          <strong>{{ featuredArticles[0].title }}</strong>
          <span>{{ featuredArticles[0].excerpt }}</span>
        </span>
      </button>

      <div class="supporting-stories">
        <button
          v-for="article in featuredArticles.slice(1, 3)"
          :key="article.id"
          class="supporting-story"
          type="button"
          @click="goToArticle(article.id)"
        >
          <img
            :src="article.coverImage || defaultCoverImage"
            :alt="`${article.title}封面`"
            @error="handleCoverError"
          >
          <span>
            <small>{{ article.categoryName || '文章' }}</small>
            <strong>{{ article.title }}</strong>
          </span>
        </button>
      </div>

      <aside class="creator-entry">
        <span class="creator-eyebrow">创作社区</span>
        <h2>分享你的经验与观点</h2>
        <p>发布文章、参与讨论，在持续交流中沉淀自己的内容。</p>
        <router-link to="/user/write" class="creator-button">开始创作</router-link>
        <router-link to="/articles" class="creator-link">浏览全部文章</router-link>
      </aside>
    </section>

    <section class="feed-section" aria-labelledby="feed-title">
      <div class="section-heading">
        <div>
          <span class="section-kicker">内容动态</span>
          <h2 id="feed-title">推荐阅读</h2>
        </div>
        <router-link to="/articles">查看更多</router-link>
      </div>

      <div class="feed-layout">
        <main>
          <div v-if="feedArticles.length" class="article-grid">
            <article
              v-for="article in feedArticles"
              :key="article.id"
              class="feed-card"
              tabindex="0"
              role="link"
              @click="goToArticle(article.id)"
              @keydown.enter="goToArticle(article.id)"
            >
              <div class="feed-cover">
                <img
                  :src="article.coverImage || defaultCoverImage"
                  :alt="`${article.title}封面`"
                  @error="handleCoverError"
                >
                <span class="cover-category">{{ article.categoryName || '文章' }}</span>
              </div>
              <div class="feed-card-body">
                <h3>{{ article.title }}</h3>
                <p>{{ article.excerpt }}</p>
                <div class="feed-meta">
                  <span>{{ article.authorNickname || article.authorName || article.authorUsername || '匿名' }}</span>
                  <span>{{ article.viewCount || 0 }} 阅读</span>
                  <span>{{ article.commentCount || 0 }} 评论</span>
                </div>
              </div>
            </article>
          </div>
          <div v-else class="empty-state">暂无文章</div>
        </main>

        <aside class="ranking-panel" aria-labelledby="ranking-title">
          <div class="ranking-heading">
            <h2 id="ranking-title">热门文章</h2>
            <span>按站内推荐</span>
          </div>
          <ol v-if="rankingArticles.length" class="ranking-list">
            <li v-for="(article, index) in rankingArticles" :key="article.id">
              <span class="ranking-number">{{ String(index + 1).padStart(2, '0') }}</span>
              <router-link :to="`/articles/${article.id}`">
                <strong>{{ article.title }}</strong>
                <span>{{ article.viewCount || 0 }} 阅读 · {{ article.likeCount || 0 }} 点赞</span>
              </router-link>
            </li>
          </ol>
          <div v-else class="empty-small">暂无热门文章</div>
        </aside>
      </div>
    </section>
  </div>
</template>

<script>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi, categoryApi, tagApi } from '@/api'
import defaultCoverImage from '@/assets/default-cover.png'

export default {
  name: 'Home',
  setup() {
    const router = useRouter()
    const articles = ref([])
    const featuredArticles = ref([])
    const topArticles = ref([])
    const hotTags = ref([])
    const hotCategories = ref([])

    const normalizeArticleList = (payload) => {
      if (Array.isArray(payload)) return payload
      if (Array.isArray(payload?.content)) return payload.content
      return []
    }

    const normalizeArticle = (article) => ({
      ...article,
      title: article.title || '无标题',
      excerpt: article.excerpt || article.summary || (article.content ? `${article.content.replace(/<[^>]+>/g, '').substring(0, 100)}...` : ''),
      viewCount: article.viewCount || 0,
      commentCount: article.commentCount || 0,
      likeCount: article.likeCount || 0,
      authorNickname: article.authorNickname || article.authorName
    })

    const feedArticles = computed(() => {
      const seen = new Set()
      return [...topArticles.value, ...articles.value]
        .filter(article => {
          if (!article?.id || seen.has(article.id)) return false
          seen.add(article.id)
          return true
        })
        .slice(0, 9)
    })

    const rankingArticles = computed(() => {
      return [...topArticles.value, ...articles.value]
        .filter((article, index, list) => list.findIndex(item => item.id === article.id) === index)
        .sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0))
        .slice(0, 6)
    })

    const fetchArticles = async () => {
      try {
        const response = await articleApi.getArticles(0, 12)
        if (response.data.success) {
          articles.value = normalizeArticleList(response.data.data || response.data).map(normalizeArticle)
        }
      } catch (error) {
        console.error('获取文章列表失败:', error)
      }
    }

    const fetchTopArticles = async () => {
      try {
        const response = await articleApi.getTopArticles()
        if (response.data.success) {
          topArticles.value = normalizeArticleList(response.data.data).map(normalizeArticle)
        }
      } catch (error) {
        console.error('获取热门文章失败:', error)
      }
    }

    const fetchFeaturedArticles = async () => {
      try {
        const response = await articleApi.getFeaturedArticles()
        if (response.data.success) {
          featuredArticles.value = normalizeArticleList(response.data.data).slice(0, 3).map(normalizeArticle)
        }
      } catch (error) {
        console.error('获取推荐文章失败:', error)
      }
    }

    const fetchTags = async () => {
      try {
        const response = await tagApi.getHotTags(8)
        if (response.data.success) hotTags.value = response.data.data || []
      } catch (error) {
        console.error('获取热门标签失败:', error)
      }
    }

    const fetchCategories = async () => {
      try {
        const response = await categoryApi.getAllCategories()
        if (response.data.success) {
          hotCategories.value = (response.data.data || [])
            .filter(category => !category.parentId && (category.status === '1' || category.status === 1 || category.status === 'approved'))
            .slice(0, 8)
        }
      } catch (error) {
        console.error('获取分类失败:', error)
      }
    }

    const handleCoverError = (event) => {
      event.target.src = defaultCoverImage
    }

    const goToArticle = (id) => router.push(`/articles/${id}`)

    onMounted(() => {
      Promise.all([
        fetchArticles(),
        fetchTopArticles(),
        fetchFeaturedArticles(),
        fetchTags(),
        fetchCategories()
      ])
    })

    return {
      featuredArticles,
      hotTags,
      hotCategories,
      feedArticles,
      rankingArticles,
      defaultCoverImage,
      handleCoverError,
      goToArticle
    }
  }
}
</script>

<style scoped>
.home-page { width: 100%; }
.sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0, 0, 0, 0); white-space: nowrap; border: 0; }

.channel-strip {
  display: flex;
  align-items: center;
  gap: 9px;
  min-height: 58px;
  margin-bottom: 24px;
  overflow-x: auto;
  scrollbar-width: none;
}
.channel-strip::-webkit-scrollbar { display: none; }
.channel-strip a { flex: 0 0 auto; text-decoration: none; }
.channel-primary, .channel-link, .channel-tag {
  padding: 7px 13px;
  border: 1px solid var(--ui-line);
  border-radius: 7px;
  color: var(--ui-text-soft);
  background: var(--ui-white);
  font-size: 13px;
  transition: color var(--ui-transition), border-color var(--ui-transition), background-color var(--ui-transition);
}
.channel-primary { color: var(--ui-white); border-color: var(--ui-accent); background: var(--ui-accent); }
.channel-link:hover, .channel-tag:hover { color: var(--ui-accent); border-color: var(--ui-accent); background: #F1F3F8; }
.channel-divider { width: 1px; height: 22px; flex: 0 0 auto; background: var(--ui-line); }
.channel-tag { border-style: dashed; }

.discovery-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(220px, .7fr) minmax(230px, .62fr);
  gap: 16px;
  margin-bottom: 48px;
}
.lead-story, .supporting-story {
  position: relative;
  padding: 0;
  overflow: hidden;
  border: 0;
  border-radius: 10px;
  background: #dfe7eb;
  text-align: left;
}
.lead-story { min-height: 390px; color: #fff; }
.lead-story img, .supporting-story img { width: 100%; height: 100%; display: block; object-fit: cover; transition: transform 260ms ease; }
.lead-story:hover img, .supporting-story:hover img { transform: scale(1.025); }
.story-shade { position: absolute; inset: 30% 0 0; background: linear-gradient(transparent, rgba(12, 20, 25, .9)); }
.lead-copy { position: absolute; left: 28px; right: 28px; bottom: 26px; display: grid; gap: 8px; }
.story-label { width: max-content; padding: 4px 8px; border-radius: 4px; background: var(--ui-accent); font-size: 12px; }
.lead-copy strong { font-size: clamp(24px, 3vw, 38px); line-height: 1.12; letter-spacing: -.025em; }
.lead-copy > span:last-child { max-width: 620px; overflow: hidden; color: rgba(255,255,255,.84); font-size: 14px; line-height: 1.55; text-overflow: ellipsis; white-space: nowrap; }

.supporting-stories { display: grid; grid-template-rows: 1fr 1fr; gap: 16px; }
.supporting-story { min-height: 0; color: #fff; }
.supporting-story::after { content: ""; position: absolute; inset: 35% 0 0; background: linear-gradient(transparent, rgba(12,20,25,.88)); }
.supporting-story > span { position: absolute; z-index: 1; left: 16px; right: 16px; bottom: 14px; display: grid; gap: 4px; }
.supporting-story small { color: rgba(255,255,255,.76); }
.supporting-story strong { display: -webkit-box; overflow: hidden; font-size: 16px; line-height: 1.35; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }

.creator-entry {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 25px;
  border: 1px solid var(--ui-line);
  border-radius: 10px;
  background: var(--ui-canvas);
}
.creator-eyebrow { color: var(--ui-accent); font-size: 12px; font-weight: 600; }
.creator-entry h2 { margin: 20px 0 12px; font-size: 25px; line-height: 1.2; letter-spacing: -.025em; }
.creator-entry p { margin: 0; color: var(--ui-text-soft); font-size: 14px; line-height: 1.7; }
.creator-button { width: 100%; margin-top: auto; padding: 11px 16px; border-radius: 6px; color: #fff; background: var(--ui-accent); text-align: center; text-decoration: none; font-weight: 600; }
.creator-button:hover { background: var(--ui-text); }
.creator-link { width: 100%; padding-top: 13px; color: var(--ui-text-soft); text-align: center; text-decoration: none; font-size: 13px; }

.section-heading { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 20px; }
.section-kicker { color: var(--ui-accent); font-size: 12px; font-weight: 600; }
.section-heading h2 { margin: 4px 0 0; font-size: 28px; font-weight: 600; letter-spacing: -.025em; }
.section-heading > a { color: var(--ui-text-soft); font-size: 13px; text-decoration: none; }
.section-heading > a:hover { color: var(--ui-accent); }

.feed-layout { display: grid; grid-template-columns: minmax(0, 1fr) 280px; gap: 34px; }
.article-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 30px 16px; }
.feed-card { min-width: 0; outline: 0; }
.feed-card:focus-visible .feed-cover { outline: 2px solid var(--ui-accent); outline-offset: 3px; }
.feed-cover { position: relative; aspect-ratio: 16 / 9; overflow: hidden; border-radius: 8px; background: #e9eef1; }
.feed-cover img { width: 100%; height: 100%; display: block; object-fit: cover; transition: opacity var(--ui-transition), transform 260ms ease; }
.feed-card:hover img { transform: scale(1.025); }
.cover-category { position: absolute; left: 10px; bottom: 9px; padding: 4px 7px; border-radius: 4px; color: #fff; background: rgba(14,20,24,.72); font-size: 11px; backdrop-filter: blur(4px); }
.feed-card-body { padding-top: 11px; }
.feed-card h3 { min-height: 44px; margin: 0 0 8px; display: -webkit-box; overflow: hidden; font-size: 16px; font-weight: 600; line-height: 1.4; -webkit-box-orient: vertical; -webkit-line-clamp: 2; transition: color var(--ui-transition); }
.feed-card:hover h3 { color: var(--ui-accent); }
.feed-card p { margin: 0 0 10px; display: -webkit-box; overflow: hidden; color: var(--ui-text-soft); font-size: 13px; line-height: 1.55; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.feed-meta { display: flex; flex-wrap: wrap; gap: 5px 11px; color: var(--ui-text-faint); font-size: 11px; }

.ranking-panel { align-self: start; padding: 18px 18px 6px; border: 1px solid var(--ui-line); border-radius: 9px; background: var(--ui-white); }
.ranking-heading { display: flex; align-items: baseline; justify-content: space-between; padding-bottom: 13px; border-bottom: 1px solid var(--ui-line); }
.ranking-heading h2 { margin: 0; font-size: 19px; }
.ranking-heading span { color: var(--ui-text-faint); font-size: 11px; }
.ranking-list { margin: 0; padding: 0; list-style: none; }
.ranking-list li { display: grid; grid-template-columns: 28px minmax(0, 1fr); gap: 8px; padding: 14px 0; border-bottom: 1px solid var(--ui-line); }
.ranking-list li:last-child { border-bottom: 0; }
.ranking-number { color: var(--ui-text-faint); font-size: 13px; font-weight: 600; }
.ranking-list li:nth-child(-n+3) .ranking-number { color: var(--ui-accent); }
.ranking-list a { display: grid; gap: 5px; text-decoration: none; }
.ranking-list strong { display: -webkit-box; overflow: hidden; font-size: 14px; font-weight: 500; line-height: 1.45; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.ranking-list a:hover strong { color: var(--ui-accent); }
.ranking-list a span { color: var(--ui-text-faint); font-size: 11px; }
.empty-state, .empty-small { padding: 32px 0; color: var(--ui-text-faint); }

@media (max-width: 1080px) {
  .discovery-grid { grid-template-columns: minmax(0, 1.5fr) minmax(230px, .72fr); }
  .creator-entry { grid-column: 1 / -1; display: grid; grid-template-columns: 1fr auto; align-items: center; gap: 8px 24px; }
  .creator-entry h2 { margin: 4px 0; }
  .creator-entry p { grid-column: 1; }
  .creator-button { grid-column: 2; grid-row: 1 / 3; width: 150px; margin: 0; }
  .creator-link { display: none; }
  .article-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 820px) {
  .discovery-grid { grid-template-columns: 1fr; }
  .lead-story { min-height: 350px; }
  .supporting-stories { grid-template-columns: 1fr 1fr; grid-template-rows: 170px; }
  .feed-layout { grid-template-columns: 1fr; }
  .ranking-panel { order: -1; }
  .ranking-list { display: grid; grid-template-columns: 1fr 1fr; column-gap: 24px; }
}

@media (max-width: 560px) {
  .channel-strip { margin-inline: -12px; padding-inline: 12px; }
  .lead-story { min-height: 300px; }
  .lead-copy { left: 18px; right: 18px; bottom: 18px; }
  .lead-copy > span:last-child { display: none; }
  .supporting-stories { display: none; }
  .creator-entry { display: flex; align-items: stretch; }
  .creator-button { width: 100%; margin-top: 18px; }
  .article-grid { grid-template-columns: 1fr 1fr; gap: 24px 12px; }
  .feed-card p { display: none; }
  .feed-meta span:nth-child(n+2) { display: none; }
  .ranking-list { grid-template-columns: 1fr; }
}
</style>
