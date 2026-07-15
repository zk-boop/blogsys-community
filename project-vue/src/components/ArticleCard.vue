<template>
  <article
    class="article-card"
    role="link"
    tabindex="0"
    @click="$emit('click')"
    @keydown.enter="$emit('click')"
    @keydown.space.prevent="$emit('click')"
  >
    <div class="article-main">
      <div class="article-kicker">
        <span>{{ article?.categoryName || '未分类' }}</span>
        <span>{{ formatDate(article?.publishedAt || article?.createTime) }}</span>
      </div>

      <h3 class="article-title">{{ article?.title || '无标题' }}</h3>
      <p class="article-excerpt">{{ articleExcerpt }}</p>

      <div class="article-meta">
        <div class="author-info">
          <el-avatar
            :size="24"
            :src="article?.authorAvatar || defaultAvatar"
            :alt="`${article?.authorNickname || article?.authorName || article?.authorUsername || '作者'}头像`"
          ></el-avatar>
          <strong class="author-name">{{ article?.authorNickname || article?.authorName || article?.authorUsername || '未知作者' }}</strong>
        </div>

        <div class="stats">
          <span><el-icon><View /></el-icon>{{ article?.viewCount || 0 }}</span>
          <span><el-icon><ChatLineSquare /></el-icon>{{ article?.commentCount || 0 }}</span>
          <span><el-icon><Star /></el-icon>{{ article?.likeCount || 0 }}</span>
        </div>
      </div>

      <div class="tags" v-if="tagsList.length > 0">
        <el-tag v-for="(tag, index) in tagsList" :key="tag.id || index" size="small" effect="plain">
          {{ tag.name || tag }}
        </el-tag>
      </div>
    </div>

    <div class="article-image">
      <img
        :src="article?.coverImage || defaultCover"
        loading="lazy"
        decoding="async"
        width="640"
        height="360"
        :alt="`${article?.title || '文章'}封面`"
        @error="handleImageError"
      >
      <span class="read-mark">阅读</span>
    </div>
  </article>
</template>

<script>
import { computed } from 'vue'
import { formatDate } from '@/utils'
import { View, ChatLineSquare, Star } from '@element-plus/icons-vue'
import defaultAvatarImg from '@/assets/default-avatar.png'
import defaultCoverImg from '@/assets/default-cover.webp'

export default {
  name: 'ArticleCard',
  components: {
    View,
    ChatLineSquare,
    Star
  },
  props: {
    article: {
      type: Object,
      required: true
    },
    index: {
      type: Number,
      default: 0
    }
  },
  emits: ['click'],
  setup(props) {
    const tagsList = computed(() => {
      if (!props.article || !props.article.tags) return []
      if (Array.isArray(props.article.tags)) return props.article.tags
      return typeof props.article.tags === 'string' ? props.article.tags.split(',').filter(Boolean) : []
    })

    const articleExcerpt = computed(() => {
      if (!props.article) return ''
      return props.article.excerpt || props.article.summary || (props.article.content ? `${props.article.content.substring(0, 140)}...` : '')
    })

    const handleImageError = (event) => {
      event.target.src = defaultCoverImg
    }

    return {
      tagsList,
      articleExcerpt,
      formatDate,
      defaultAvatar: defaultAvatarImg,
      defaultCover: defaultCoverImg,
      handleImageError
    }
  }
}
</script>

<style scoped>
.article-card {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr) 220px;
  min-height: 206px;
  background: #FFFFFF;
  border: 1px solid #111111;
  transition: background-color .18s ease, color .18s ease;
}

.article-card:hover {
  background: #F7F7F8;
}

.article-index {
  padding: 18px 14px;
  border-right: 1px solid #111111;
  color: #002FA7;
  font-size: 48px;
  font-weight: 700;
  line-height: .9;
}

.article-main {
  min-width: 0;
  padding: 20px 24px;
}

.article-kicker {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 14px;
  color: #575757;
  font-size: 13px;
  font-weight: 700;
}

.article-title {
  margin: 0 0 12px;
  color: #111111;
  font-size: clamp(22px, 2.4vw, 34px);
  font-weight: 700;
  line-height: 1.04;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-excerpt {
  margin: 0 0 18px;
  color: #333333;
  font-size: 15px;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 14px;
  border-top: 1px solid #D8D8DC;
}

.author-info,
.stats,
.stats span {
  display: flex;
  align-items: center;
}

.author-info {
  gap: 8px;
}

.author-name {
  color: #111111;
  font-size: 14px;
}

.stats {
  gap: 14px;
  color: #575757;
  font-size: 13px;
}

.stats span {
  gap: 4px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.article-image {
  position: relative;
  min-height: 100%;
  border-left: 1px solid #111111;
  background: #F7F7F8;
  overflow: hidden;
}

.article-image img {
  width: 100%;
  height: 100%;
  min-height: 206px;
  display: block;
  object-fit: cover;
  filter: grayscale(100%) contrast(1.08);
  transition: filter .18s ease, transform .18s ease;
}

.article-card:hover .article-image img {
  filter: grayscale(0%) contrast(1);
  transform: scale(1.03);
}

.read-mark {
  position: absolute;
  right: 0;
  bottom: 0;
  padding: 8px 12px;
  color: #FFFFFF;
  background: #002FA7;
  font-weight: 700;
}

@media (max-width: 900px) {
  .article-card {
    grid-template-columns: 72px minmax(0, 1fr);
  }

  .article-image {
    grid-column: 1 / -1;
    grid-row: 1;
    height: 210px;
    border-left: 0;
    border-bottom: 1px solid #111111;
  }

  .article-index,
  .article-main {
    grid-row: 2;
  }
}

@media (max-width: 560px) {
  .article-card {
    grid-template-columns: 1fr;
  }

  .article-index {
    border-right: 0;
    border-bottom: 1px solid #111111;
    font-size: 36px;
  }

  .article-main {
    padding: 16px;
  }

  .article-title {
    font-size: 24px;
  }

  .article-image {
    grid-column: auto;
  }
}
</style>

<style scoped>
.article-card {
  grid-template-columns: minmax(0, 1fr) 208px;
  min-height: 0;
  border: 0 !important;
  border-top: 1px solid var(--ui-line-strong) !important;
  background: transparent !important;
  transition: border-color var(--ui-transition) !important;
}
.article-card:hover { background: transparent; border-top-color: var(--ui-accent) !important; }
.article-index { display: none; }
.article-main { padding: 26px 28px 28px 0; }
.article-kicker { margin-bottom: 11px; color: var(--ui-text-faint); font-size: 12px; font-weight: 500; }
.article-title {
  margin-bottom: 10px;
  font-size: clamp(22px, 2.4vw, 31px);
  font-weight: 500;
  line-height: 1.15;
  letter-spacing: -.025em;
  transition: color var(--ui-transition);
}
.article-card:hover .article-title { color: var(--ui-accent); }
.article-excerpt { margin-bottom: 18px; color: var(--ui-text-soft); font-size: 15px; line-height: 1.75; }
.article-meta { padding-top: 13px; border-top-color: var(--ui-line); }
.author-name { color: var(--ui-text-soft); }
.tags { margin-top: 12px; }
.article-image { min-height: 0; margin: 22px 0; border: 0; background: var(--ui-canvas); }
.article-image img {
  min-height: 160px;
  filter: none;
  transition: opacity var(--ui-transition);
}
.article-card:hover .article-image img { filter: none; transform: none; opacity: .9; }
.read-mark { display: none; }

@media (max-width: 720px) {
  .article-card { grid-template-columns: minmax(0, 1fr) 120px; }
  .article-image { grid-column: 2; grid-row: 1; height: auto; margin: 20px 0; border: 0; }
  .article-image img { min-height: 112px; }
  .article-main { grid-row: 1; padding: 22px 18px 24px 0; }
  .article-excerpt { -webkit-line-clamp: 2; }
}

@media (max-width: 520px) {
  .article-card { grid-template-columns: 1fr; }
  .article-main { padding-right: 0; }
  .article-image { display: none; }
  .article-title { font-size: 23px; }
}
</style>
