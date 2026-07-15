<template>
  <div class="about-us-container">
    <h1 class="page-title">关于我们</h1>

    <div class="authors-container">
      <div class="author-card">
        <div class="author-header">
          <h2>第一作者</h2>
        </div>
        <div class="author-details">
          <div class="detail-item">
            <span class="label">姓名：</span>
            <span class="value">谢万烽</span>
          </div>
          <div class="detail-item">
            <span class="label">学号：</span>
            <span class="value">20232501272</span>
          </div>
          <div class="detail-item">
            <span class="label">班级：</span>
            <span class="value">软件23-9班</span>
          </div>
        </div>
      </div>

      <div class="author-card">
        <div class="author-header">
          <h2>第二作者</h2>
        </div>
        <div class="author-details">
          <div class="detail-item">
            <span class="label">姓名：</span>
            <span class="value">张坤</span>
          </div>
          <div class="detail-item">
            <span class="label">学号：</span>
            <span class="value">20232501484</span>
          </div>
          <div class="detail-item">
            <span class="label">班级：</span>
            <span class="value">软件23-9班</span>
          </div>
        </div>
      </div>

      <div class="author-card">
        <div class="author-header">
          <h2>第三作者</h2>
        </div>
        <div class="author-details">
          <div class="detail-item">
            <span class="label">姓名：</span>
            <span class="value">徐嘉伟</span>
          </div>
          <div class="detail-item">
            <span class="label">学号：</span>
            <span class="value">20232501</span>
          </div>
          <div class="detail-item">
            <span class="label">班级：</span>
            <span class="value">软件23-9班</span>
          </div>
        </div>
      </div>
    </div>

    <div class="project-description">
      <h3>项目描述</h3>
      <p>
        本博客系统是基于Spring Boot和Vue.js开发的现代化博客平台，采用前后端分离的架构，
        实现了用户注册登录、文章发布管理、评论、分类、标签等功能。
        项目旨在提供一个简洁高效、功能完备的博客系统，同时也是我们团队在Web开发方面的实践成果。
      </p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AboutUs',
  setup() {
    // 无需复杂状态管理
    return {}
  }
}
</script>

<style scoped>
.about-us-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.page-title {
  font-size: 28px;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
  text-align: center;
}

.authors-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 30px;
  margin-bottom: 40px;
}

.author-card {
  background-color: #fff;
  border-radius: 0;
  box-shadow: none;
  width: 300px;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.author-card:hover {
  transform: translateY(-5px);
  box-shadow: none;
}

.author-header {
  background-color: #002FA7;
  color: white;
  padding: 15px 20px;
  text-align: center;
}

.author-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.author-details {
  padding: 20px;
}

.detail-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.label {
  font-weight: 600;
  min-width: 60px;
}

.value {
  flex: 1;
}

.project-description {
  margin-top: 40px;
  background-color: #fff;
  border-radius: 0;
  padding: 30px;
  box-shadow: none;
}

.project-description h3 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 22px;
  color: #002FA7;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.project-description p {
  line-height: 1.7;
  font-size: 16px;
}

@media (max-width: 768px) {
  .authors-container {
    flex-direction: column;
    align-items: center;
  }

  .author-card {
    width: 100%;
    max-width: 400px;
  }
}
</style>
