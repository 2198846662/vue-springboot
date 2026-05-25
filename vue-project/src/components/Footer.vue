<script setup>
import { Compass, Instagram, Twitter, Facebook } from 'lucide-vue-next'

const footerSections = [
  {
    title: '探索',
    links: ['热门目的地', '旅行攻略', '用户游记', '旅行摄影']
  },
  {
    title: '发现',
    links: ['自然风光', '历史文化', '美食之旅', '户外探险']
  },
  {
    title: '关于',
    links: ['关于我们', '加入团队', '商务合作', '联系我们']
  },
  {
    title: '帮助',
    links: ['常见问题', '使用条款', '隐私政策', 'Cookie 设置']
  }
]
</script>

<template>
  <footer class="mt-16" style="background: var(--bg-secondary); border-top: 1px solid var(--border);">
    <div class="max-w-[1600px] mx-auto px-4 md:px-6 py-12 md:py-16">
      <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-8 mb-12">
        <!-- Brand Column -->
        <div class="col-span-2 md:col-span-4 lg:col-span-1 mb-4 lg:mb-0">
          <div class="flex items-center gap-2.5 mb-4">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center"
              style="background: linear-gradient(135deg, var(--primary), var(--primary-light));">
              <Compass class="w-5 h-5 text-white" />
            </div>
            <span class="text-xl font-bold" style="font-family: var(--font-display);">
              Travel<span style="color: var(--accent);">Vista</span>
            </span>
          </div>
          <p class="text-sm leading-relaxed mb-5" style="color: var(--text-secondary);">
            探索世界之美，发现你的下一个目的地。我们相信，每一次旅行都是一段值得珍藏的故事。
          </p>
          <!-- Social Links -->
          <div class="flex gap-3">
            <a href="#" class="social-btn w-9 h-9 rounded-lg flex items-center justify-center transition-all duration-200">
              <Instagram class="w-4 h-4" />
            </a>
            <a href="#" class="social-btn w-9 h-9 rounded-lg flex items-center justify-center transition-all duration-200">
              <Twitter class="w-4 h-4" />
            </a>
            <a href="#" class="social-btn w-9 h-9 rounded-lg flex items-center justify-center transition-all duration-200">
              <Facebook class="w-4 h-4" />
            </a>
          </div>
        </div>

        <!-- Link Columns -->
        <div v-for="section in footerSections" :key="section.title">
          <h4 class="footer-heading text-sm font-semibold mb-4" style="color: var(--text-primary);">
            {{ section.title }}
          </h4>
          <ul class="space-y-2.5">
            <li v-for="link in section.links" :key="link">
              <a
                href="#"
                class="footer-link text-sm inline-flex items-center gap-1 no-underline transition-all duration-200"
              >
                {{ link }}
              </a>
            </li>
          </ul>
        </div>
      </div>

      <!-- Divider -->
      <div class="h-px mb-6" style="background: linear-gradient(90deg, transparent, var(--border), transparent);"></div>

      <!-- Bottom Bar -->
      <div class="flex flex-col md:flex-row justify-between items-center gap-4 text-sm" style="color: var(--text-muted);">
        <p>&copy; 2024 TravelVista. 探索世界，发现自我。</p>
        <p>Made with passion for travelers</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer-heading {
  font-family: var(--font-display);
}

.footer-link {
  color: var(--text-secondary);
}

.footer-link:hover {
  color: var(--primary);
}

.social-btn {
  background: var(--bg-card);
  border: 1px solid var(--border);
  color: var(--text-secondary);
}

.social-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
  transform: translateY(-2px);
}
</style>
