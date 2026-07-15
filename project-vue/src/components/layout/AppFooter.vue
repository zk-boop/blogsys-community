<template>
  <footer class="app-footer">
    <div class="footer-container">
      <div class="footer-index">©{{ currentYear }}</div>
      <div class="footer-copy">
        <h2>基于 Spring Boot + Vue 的多用户博客系统</h2>
        <p>软件23-9班第八小组小小学期末设计</p>
      </div>
      <router-link class="footer-link" to="/about-us">关于我们</router-link>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'AppFooter',
  setup() {
    const currentYear = new Date().getFullYear()

    return {
      currentYear
    }
  }
}
</script>

<style scoped>
.app-footer {
  margin-top: 72px;
  color: #FFFFFF;
  background: #111111;
  border-top: 1px solid #111111;
}

.footer-container {
  width: min(var(--fd-content), calc(100% - 40px));
  margin: 0 auto;
  display: grid;
  grid-template-columns: 180px 1fr 180px;
  border-left: 1px solid #FFFFFF;
  border-right: 1px solid #FFFFFF;
}

.footer-index,
.footer-copy,
.footer-link {
  min-height: 160px;
  padding: 24px;
}

.footer-index {
  border-right: 1px solid #FFFFFF;
  font-size: 38px;
  font-weight: 700;
  line-height: .95;
}

.footer-copy h2 {
  max-width: 720px;
  margin: 0 0 12px;
  font-size: clamp(26px, 4vw, 52px);
  line-height: .95;
}

.footer-copy p {
  margin: 0;
  color: #D8D8DC;
  font-size: 15px;
}

.footer-link {
  display: flex;
  align-items: flex-end;
  justify-content: flex-start;
  color: #FFFFFF;
  border-left: 1px solid #FFFFFF;
  font-weight: 700;
  text-decoration: underline;
  text-underline-offset: 4px;
}

.footer-link:hover {
  color: #002FA7;
}

@media (max-width: 768px) {
  .app-footer {
    margin-top: 48px;
  }

  .footer-container {
    width: min(100% - 20px, var(--fd-content));
    grid-template-columns: 1fr;
  }

  .footer-index,
  .footer-copy,
  .footer-link {
    min-height: auto;
    border: 0;
    border-bottom: 1px solid #FFFFFF;
  }

  .footer-link {
    border-bottom: 0;
  }
}
</style>

<style scoped>
.app-footer {
  margin-top: 32px;
  color: var(--ui-text);
  background: var(--ui-white);
  border-top: 1px solid var(--ui-line-strong);
}

.footer-container {
  width: min(var(--ui-content), calc(100% - 40px));
  grid-template-columns: 120px 1fr 140px;
  border: 0;
}

.footer-index, .footer-copy, .footer-link {
  min-height: 136px;
  padding: 28px 0;
}
.footer-index { color: var(--ui-text-faint); border-right: 0; font-size: 14px; font-weight: 400; }
.footer-copy { padding-inline: 28px; border-left: 1px solid var(--ui-line); }
.footer-copy h2 { margin-bottom: 10px; font-size: clamp(22px, 3vw, 34px); font-weight: 500; line-height: 1.15; }
.footer-copy p { color: var(--ui-text-soft); }
.footer-link {
  align-items: center;
  color: var(--ui-accent);
  border-left: 1px solid var(--ui-line);
  text-decoration: none;
}
.footer-link:hover { color: var(--ui-text); }

@media (max-width: 768px) {
  .footer-container { width: calc(100% - 24px); grid-template-columns: 1fr; }
  .footer-index, .footer-copy, .footer-link { min-height: auto; padding: 18px 0; border: 0; border-bottom: 1px solid var(--ui-line); }
  .footer-copy { padding-block: 24px; }
  .footer-link { border-bottom: 0; }
}
</style>
