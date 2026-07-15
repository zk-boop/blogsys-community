<template>
  <div class="comment-item">
    <div class="comment-avatar">
      <el-avatar
        :size="40"
        :src="getAvatarUrl(comment.userAvatar)"
        :alt="`${comment.userNickname || comment.username || '评论用户'}头像`"
        @error="handleAvatarError"
      ></el-avatar>
    </div>

    <div class="comment-content">
      <div class="comment-header">
        <div class="commenter-info">
          <span class="commenter-name">{{ comment.userNickname || comment.username }}</span>
          <span class="comment-time">{{ formatTimeAgo(comment.createdAt) }}</span>
        </div>

        <div class="comment-actions">
          <el-button
            type="primary"
            link
            size="small"
            @click="handleReply"
          >回复</el-button>

          <el-button
            :type="isLiked ? 'primary' : 'default'"
            plain
            size="small"
            @click="toggleLike"
            class="like-button"
            :class="{ 'is-liked': isLiked }"
          >
            <div class="thumb-icon">
              <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="20" height="20">
                <path :fill="isLiked ? '#002FA7' : '#909399'" d="M885.9 533.7c16.8-22.2 26.1-49.4 26.1-77.7 0-44.9-25.1-87.4-65.5-111.1a67.67 67.67 0 0 0-34.3-9.3H572.4l6-122.9c1.4-29.7-9.1-57.9-29.5-79.4-20.5-21.5-48.1-33.4-77.9-33.4-52 0-98 35-111.8 85.1l-85.9 311H144c-17.7 0-32 14.3-32 32v364c0 17.7 14.3 32 32 32h601.3c9.2 0 18.2-1.8 26.5-5.4 47.6-20.3 78.3-66.8 78.3-118.4 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7 0-12.6-1.8-25-5.4-37 16.8-22.2 26.1-49.4 26.1-77.7-.2-12.6-2-25.1-5.6-37.1zM184 852V568h81v284h-81zm636.4-353l-21.9 19 13.9 25.4c4.6 8.4 6.9 17.6 6.9 27.3 0 16.5-7.2 32.2-19.6 43l-21.9 19 13.9 25.4c4.6 8.4 6.9 17.6 6.9 27.3 0 16.5-7.2 32.2-19.6 43l-21.9 19 13.9 25.4c4.6 8.4 6.9 17.6 6.9 27.3 0 22.4-13.2 42.6-33.6 51.8H329V564.8l99.5-360.5c5.2-18.9 22.5-32.2 42.2-32.3 7.6 0 15.1 2.2 21.1 6.7 9.9 7.4 15.2 18.6 14.6 30.5l-9.6 198.4h314.4C829 418.5 840 436.9 840 456c0 16.5-7.2 32.1-19.6 43z" />
              </svg>
            </div>
            <span class="like-count">{{ comment.likeCount || 0 }}</span>
          </el-button>
        </div>
      </div>

      <div class="comment-text">
        <!-- 如果是回复某人的评论，显示被回复人 -->
        <template v-if="comment.replyToNickname">
          <span class="reply-to">@{{ comment.replyToNickname }}</span>
        </template>

        {{ comment.content }}
      </div>

      <!-- 回复表单 -->
      <div class="reply-form" v-if="showReplyForm">
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="2"
          placeholder="写下你的回复..."
        ></el-input>

        <div class="reply-actions">
          <el-button size="small" @click="cancelReply">取消</el-button>
          <el-button type="primary" size="small" @click="submitReply" :loading="submitting">回复</el-button>
        </div>
      </div>

      <!-- 子评论 -->
      <div class="replies" v-if="replies.length > 0">
        <comment-item
          v-for="reply in replies"
          :key="reply.id"
          :comment="reply"
          @reply="$emit('reply', reply)"
          @like="handleChildLike"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { commentApi, fileApi } from '@/api'
import { timeAgo } from '@/utils'

export default {
  name: 'CommentItem',
  props: {
    comment: {
      type: Object,
      required: true
    },
    isReply: {
      type: Boolean,
      default: false
    }
  },
  emits: ['reply', 'like', 'unlike'],
  setup(props, { emit }) {
    const store = useStore()
    const showReplyForm = ref(false)
    const replyContent = ref('')
    const submitting = ref(false)
    const replies = ref([])
    const isLiked = ref(false)
    const defaultAvatar = require('@/assets/default-avatar.png')

    const isAuthenticated = computed(() => store.getters.isAuthenticated)
    const currentUserId = computed(() => store.getters.userId)

    // 从localStorage获取用户点赞状态
    const getLikedStatus = () => {
      if (!isAuthenticated.value) return false

      const likedComments = JSON.parse(localStorage.getItem('likedComments') || '{}')
      return !!likedComments[props.comment.id]
    }

    // 保存用户点赞状态到localStorage
    const saveLikedStatus = (status) => {
      if (!isAuthenticated.value) return

      const likedComments = JSON.parse(localStorage.getItem('likedComments') || '{}')

      if (status) {
        likedComments[props.comment.id] = true
      } else {
        delete likedComments[props.comment.id]
      }

      localStorage.setItem('likedComments', JSON.stringify(likedComments))
    }

    const fetchReplies = async () => {
      // 只有父评论才需要获取回复
      if (!props.comment.parentId) {
        try {
          const response = await commentApi.getRepliesByCommentId(props.comment.id)
          if (response.data.success) {
            replies.value = response.data.data
          }
        } catch (error) {
          console.error('获取评论回复失败:', error)
        }
      }
    }

    const handleReply = () => {
      if (!isAuthenticated.value) {
        ElMessage.warning('请先登录后再回复评论')
        return
      }

      showReplyForm.value = true
    }

    const cancelReply = () => {
      showReplyForm.value = false
      replyContent.value = ''
    }

    // 处理头像加载错误
    const handleAvatarError = (e) => {
      e.target.src = defaultAvatar
    }

    // 获取头像URL
    const getAvatarUrl = (avatar) => {
      if (!avatar) return defaultAvatar
      if (avatar.startsWith('http')) return avatar
      return fileApi.getAvatarUrl(avatar)
    }

    const submitReply = async () => {
      if (!replyContent.value.trim()) {
        ElMessage.warning('回复内容不能为空')
        return
      }

      try {
        submitting.value = true
        const comment = { content: replyContent.value }

        // 构建回复参数
        const articleId = props.comment.articleId
        const parentId = props.comment.parentId || props.comment.id
        const replyToUserId = props.comment.userId

        await commentApi.createComment(comment, articleId, currentUserId.value, parentId, replyToUserId)
        ElMessage.success('回复提交成功')
        replyContent.value = ''
        showReplyForm.value = false

        // 通知父组件，以便更新评论列表
        emit('reply', props.comment)
      } catch (error) {
        ElMessage.error('回复提交失败，请稍后重试')
        console.error('回复提交失败:', error)
      } finally {
        submitting.value = false
      }
    }

    const toggleLike = async () => {
      if (!isAuthenticated.value) {
        ElMessage.warning('请先登录后再点赞评论')
        return
      }

      try {
        if (isLiked.value) {
          // 取消点赞 - 只在本地更新状态
          // 不再直接调用API: await commentApi.unlikeComment(props.comment.id)
          props.comment.likeCount = Math.max(0, props.comment.likeCount - 1)
          isLiked.value = false
          saveLikedStatus(false)
          emit('unlike', props.comment.id) // 让父组件处理API调用
          ElMessage.success('已取消点赞')
        } else {
          // 点赞 - 只在本地更新状态
          // 不再直接调用API: await commentApi.likeComment(props.comment.id)
          props.comment.likeCount = (props.comment.likeCount || 0) + 1
          isLiked.value = true
          saveLikedStatus(true)
          emit('like', props.comment.id) // 让父组件处理API调用
          ElMessage.success('点赞成功')
        }
      } catch (error) {
        console.error('点赞操作失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      }
    }

    const handleChildLike = (commentId) => {
      // 只需将事件向上传递
      emit('like', commentId)
    }

    const formatTimeAgo = (dateString) => {
      return timeAgo(dateString)
    }

    onMounted(() => {
      fetchReplies()
      isLiked.value = getLikedStatus()
    })

    return {
      showReplyForm,
      replyContent,
      submitting,
      replies,
      isLiked,
      handleReply,
      cancelReply,
      submitReply,
      toggleLike,
      handleChildLike,
      formatTimeAgo,
      getAvatarUrl,
      handleAvatarError
    }
  }
}
</script>

<style scoped>
.comment-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background-color: #fff;
  border-radius: 0;
  box-shadow: none;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-content {
  flex-grow: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.commenter-info {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.commenter-name {
  font-weight: 500;
  font-size: 16px;
}

.comment-time {
  color: #999;
  font-size: 14px;
}

.comment-actions {
  display: flex;
  gap: 10px;
}

.comment-actions .el-button {
  display: flex;
  align-items: center;
  gap: 5px;
}

.comment-actions .el-button.like-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 15px;
  border-radius: 0;
  transition: all 0.3s;
  box-shadow: none;
}

.comment-actions .el-button.like-button:not(.is-liked) {
  background-color: #f5f7fa;
  border-color: #dcdfe6;
  color: #606266;
}

.comment-actions .el-button.like-button.is-liked {
  background-color: #ecf5ff;
  border-color: #002FA7;
  color: #002FA7;
}

.comment-actions .el-button.like-button:hover {
  transform: translateY(-2px);
  box-shadow: none;
}

.thumb-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.is-liked .thumb-icon {
  transform: scale(1.2);
  animation: pulse 0.4s ease;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.4); }
  100% { transform: scale(1.2); }
}

.like-count {
  font-weight: 500;
}

.comment-text {
  line-height: 1.6;
  margin-bottom: 15px;
  word-break: break-word;
}

.reply-to {
  color: #002FA7;
  margin-right: 5px;
}

.reply-form {
  margin: 15px 0;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
  gap: 10px;
}

.replies {
  margin-top: 20px;
  margin-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

@media (max-width: 768px) {
  .comment-header {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
