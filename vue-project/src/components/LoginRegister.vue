<script setup>
import { ref, watch } from 'vue'
import { Lock, User, ArrowRight, ChevronLeft, ChevronRight, Eye, EyeOff } from 'lucide-vue-next'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'login-success'])

const isLogin = ref(true)
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const email = ref('')
const nickname = ref('')
const rememberMe = ref(false)
const showPassword = ref(false)
const error = ref('')
const success = ref('')
const isLoading = ref(false)

const closeModal = () => {
  emit('update:modelValue', false)
  resetForm()
  isLogin.value = true
  showPassword.value = false
}

const resetForm = () => {
  username.value = ''
  password.value = ''
  confirmPassword.value = ''
  email.value = ''
  nickname.value = ''
  rememberMe.value = false
  error.value = ''
  success.value = ''
}

const toggleMode = () => {
  isLogin.value = !isLogin.value
  error.value = ''
  success.value = ''
}

watch(() => props.modelValue, (visible) => {
  if (visible) {
    // 每次打开弹窗默认回到登录页，避免停留在注册态
    isLogin.value = true
    error.value = ''
    success.value = ''
    showPassword.value = false
  }
})

const handleSubmit = async () => {
  error.value = ''

  if (!username.value) {
    error.value = '请输入用户名'
    return
  }

  if (!password.value) {
    error.value = '请输入密码'
    return
  }

  if (!isLogin.value && password.value !== confirmPassword.value) {
    error.value = '两次输入的密码不一致'
    return
  }

  isLoading.value = true
  success.value = ''

  try {
    const endpoint = isLogin.value ? '/api/auth/login' : '/api/auth/register'
    const requestBody = {
      username: username.value,
      password: password.value
    }

    // 注册时添加 email 和 nickname
    if (!isLogin.value) {
      if (email.value) requestBody.email = email.value
      if (nickname.value) requestBody.nickname = nickname.value
    }

    const response = await fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody)
    })

    const data = await response.json()

    if (data.code === 200) {
      if (isLogin.value) {
        const userData = {
          id: data.data.userId,
          username: data.data.username,
          nickname: data.data.nickname,
          token: data.data.token,
          role: data.data.role || 'USER'
        }
        localStorage.setItem('currentUser', JSON.stringify(userData))
        if (rememberMe.value) {
          localStorage.setItem('rememberMe', 'true')
        } else {
          localStorage.removeItem('rememberMe')
        }
        emit('login-success', userData)
        closeModal()
      } else {
        // 注册成功后切回登录
        isLogin.value = true
        error.value = ''
        success.value = '注册成功，请使用刚刚的账号登录'
        confirmPassword.value = ''
      }
    } else {
      error.value = data.message || (isLogin.value ? '登录失败' : '注册失败')
    }
  } catch (err) {
    error.value = '网络错误，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

const handleGoogleLogin = () => {
  console.log('Google login')
}
</script>

<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="modelValue"
        class="fixed inset-0 z-[100] flex items-center justify-center"
        @click.self="closeModal"
      >
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/45 backdrop-blur-sm"></div>

        <!-- Modal -->
        <div class="auth-modal relative w-full max-w-[1220px] h-[620px] bg-white rounded-2xl overflow-hidden shadow-2xl flex animate-slide-up mx-4">
          <!-- Close Button -->
          <button
            class="absolute top-4 right-4 z-20 w-9 h-9 rounded-full bg-black/5 backdrop-blur-sm flex items-center justify-center text-black/60 hover:text-black hover:bg-black/10 transition-all"
            @click="closeModal"
          >
            <svg class="w-5 h-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>

          <button type="button" class="side-arrow left-5" aria-label="previous">
            <ChevronLeft class="w-7 h-7" />
          </button>
          <button type="button" class="side-arrow right-5" aria-label="next">
            <ChevronRight class="w-7 h-7" />
          </button>

          <!-- Left Side - Illustration -->
          <div class="hidden md:flex w-[52%] left-panel relative overflow-hidden">
            <div class="brand absolute top-7 left-7 z-10">MaoTeng-Java</div>

            <div class="shape shape-purple"><span class="eye"></span><span class="eye"></span></div>
            <div class="shape shape-black"><span class="eye"></span><span class="eye"></span></div>
            <div class="shape shape-orange"><span class="eye"></span><span class="eye"></span></div>
            <div class="shape shape-yellow"><span class="eye"></span><span class="eye"></span></div>

            <div class="mouth"></div>
          </div>

          <!-- Right Side - Form -->
          <div class="flex-1 flex flex-col justify-center px-8 md:px-14 py-8 bg-[#f7f7f8]">
            <div class="max-w-[380px] mx-auto w-full">
              <!-- Title -->
              <div class="mb-8">
                <h2 class="text-[40px] leading-tight font-extrabold mb-2 text-[#101828]" style="font-family: var(--font-display);">
                  {{ isLogin ? '欢迎回来!' : '创建账号' }}
                </h2>
                <p class="text-sm text-[#667085]">
                  {{ isLogin ? '请登录以继续使用' : '注册后即可探索更多风景' }}
                </p>
              </div>

              <!-- Error Message -->
              <div v-if="error" class="mb-4 p-3 bg-red-50 border border-red-200 rounded-xl text-red-600 text-sm">
                {{ error }}
              </div>

              <!-- Success Message -->
              <div v-if="success" class="mb-4 p-3 bg-green-50 border border-green-200 rounded-xl text-green-700 text-sm">
                {{ success }}
              </div>

              <!-- Form -->
              <form @submit.prevent="handleSubmit" class="space-y-4">
                <!-- Username Input -->
                <div>
                  <label class="block text-[13px] font-semibold mb-1.5 text-[#1D2939]">
                    用户名
                  </label>
                  <div class="relative">
                    <User class="w-4 h-4 absolute left-4 top-1/2 -translate-y-1/2 text-[#98A2B3]" />
                    <input
                      v-model="username"
                      type="text"
                      class="auth-input w-full pl-11 pr-4 py-3 rounded-xl text-sm transition-all"
                      placeholder="请输入用户名"
                    />
                  </div>
                </div>

                <!-- Password Input -->
                <div>
                  <label class="block text-[13px] font-semibold mb-1.5 text-[#1D2939]">
                    密码
                  </label>
                  <div class="relative">
                    <Lock class="w-4 h-4 absolute left-4 top-1/2 -translate-y-1/2 text-[#98A2B3]" />
                    <input
                      v-model="password"
                      :type="showPassword ? 'text' : 'password'"
                      class="auth-input w-full pl-11 pr-12 py-3 rounded-xl text-sm transition-all"
                      placeholder="请输入密码"
                    />
                    <button
                      type="button"
                      class="absolute right-3 top-1/2 -translate-y-1/2 text-[#98A2B3] hover:text-[#667085]"
                      @click="showPassword = !showPassword"
                    >
                      <EyeOff v-if="showPassword" class="w-4 h-4" />
                      <Eye v-else class="w-4 h-4" />
                    </button>
                  </div>
                </div>

                <!-- Confirm Password (Register only) -->
                <div v-if="!isLogin">
                  <label class="block text-[13px] font-semibold mb-1.5 text-[#1D2939]">
                    确认密码
                  </label>
                  <div class="relative">
                    <Lock class="w-4 h-4 absolute left-4 top-1/2 -translate-y-1/2 text-[#98A2B3]" />
                    <input
                      v-model="confirmPassword"
                      type="password"
                      class="auth-input w-full pl-11 pr-4 py-3 rounded-xl text-sm transition-all"
                      placeholder="请再次输入密码"
                    />
                  </div>
                </div>

                <!-- Email (Register only) -->
                <div v-if="!isLogin">
                  <label class="block text-[13px] font-semibold mb-1.5 text-[#1D2939]">
                    邮箱
                  </label>
                  <div class="relative">
                    <input
                      v-model="email"
                      type="email"
                      class="auth-input w-full px-4 py-3 rounded-xl text-sm transition-all"
                      placeholder="请输入邮箱（可选）"
                    />
                  </div>
                </div>

                <!-- Nickname (Register only) -->
                <div v-if="!isLogin">
                  <label class="block text-[13px] font-semibold mb-1.5 text-[#1D2939]">
                    昵称
                  </label>
                  <div class="relative">
                    <input
                      v-model="nickname"
                      type="text"
                      class="auth-input w-full px-4 py-3 rounded-xl text-sm transition-all"
                      placeholder="请输入昵称（可选）"
                    />
                  </div>
                </div>

                <!-- Remember Me & Forgot Password -->
                <div v-if="isLogin" class="flex items-center justify-between pt-1">
                  <label class="flex items-center gap-2 cursor-pointer text-sm text-[#667085]">
                    <input
                      v-model="rememberMe"
                      type="checkbox"
                      class="w-4 h-4 rounded border-gray-300"
                      style="accent-color: #0f172a;"
                    />
                    <span>记住我 30 天</span>
                  </label>
                  <button type="button" class="text-sm font-semibold text-[#6366f1] hover:underline">忘记密码?</button>
                </div>

                <!-- Submit Button -->
                <button
                  type="submit"
                  :disabled="isLoading"
                  class="submit-btn w-full mt-2 py-3 rounded-full text-white text-sm font-semibold transition-all disabled:opacity-50 flex items-center justify-center gap-2"
                >
                  <span v-if="isLoading" class="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full"></span>
                  <span v-else>{{ isLogin ? '登录' : '注册' }}</span>
                  <ArrowRight v-if="!isLoading" class="w-4 h-4" />
                </button>
              </form>

              <!-- Divider -->
              <div class="flex items-center gap-3 my-6">
                <div class="flex-1 h-px bg-[#D0D5DD]"></div>
                <span class="text-xs text-[#98A2B3]">or</span>
                <div class="flex-1 h-px bg-[#D0D5DD]"></div>
              </div>

              <!-- Social Login - Single Google -->
              <button
                type="button"
                class="google-btn w-full py-3 rounded-full border text-sm font-semibold flex items-center justify-center gap-3 transition-all"
                @click="handleGoogleLogin"
              >
                <svg class="w-5 h-5" viewBox="0 0 24 24">
                  <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                  <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                使用 Google 登录
              </button>

              <!-- Toggle Mode -->
              <div class="mt-6 text-center text-sm text-[#667085]">
                {{ isLogin ? '还没有账号？' : '已有账号？' }}
                <button
                  type="button"
                  class="font-semibold ml-1 text-[#0f172a] hover:underline"
                  @click="toggleMode"
                >
                  {{ isLogin ? '立即注册' : '返回登录' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.auth-modal {
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.left-panel {
  background:
    linear-gradient(rgba(255, 255, 255, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.06) 1px, transparent 1px),
    linear-gradient(180deg, #6e7787 0%, #4e5664 100%);
  background-size: 26px 26px, 26px 26px, cover;
}

.brand {
  font-size: 14px;
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: 0.2px;
}

.shape {
  position: absolute;
  display: flex;
  gap: 10px;
  padding-top: 26px;
  padding-inline: 24px;
  overflow: hidden;
}

.shape .eye {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #fff;
  position: relative;
}

.shape .eye::after {
  content: '';
  position: absolute;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #1f2937;
  right: 2px;
  top: 4px;
}

.shape-purple {
  width: 132px;
  height: 338px;
  background: linear-gradient(180deg, #6d5bff 0%, #5b36f5 100%);
  left: 235px;
  bottom: 34px;
  transform: skewX(-10deg);
  border-radius: 10px 10px 0 0;
}

.shape-black {
  width: 96px;
  height: 248px;
  background: #27272a;
  left: 292px;
  bottom: 34px;
  transform: skewX(-8deg);
  border-radius: 10px 10px 0 0;
}

.shape-orange {
  width: 168px;
  height: 138px;
  background: #ff9867;
  left: 98px;
  bottom: 34px;
  border-radius: 140px 140px 0 0;
}

.shape-yellow {
  width: 98px;
  height: 160px;
  background: #e5d95c;
  left: 336px;
  bottom: 34px;
  border-radius: 90px 90px 0 0;
}

.mouth {
  position: absolute;
  width: 36px;
  height: 4px;
  background: #374151;
  border-radius: 999px;
  left: 377px;
  bottom: 126px;
}

.side-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 84px;
  height: 84px;
  border-radius: 999px;
  background: rgba(203, 213, 225, 0.58);
  color: #9ca3af;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20;
}

.auth-input {
  background: #e9edf5;
  border: 1px solid #d0d5dd;
  color: #111827;
}

.auth-input::placeholder {
  color: #98a2b3;
}

.auth-input:focus {
  outline: none;
  border-color: #7c7cf4;
  box-shadow: 0 0 0 3px rgba(124, 124, 244, 0.18);
}

.submit-btn {
  background: linear-gradient(180deg, #0f172a 0%, #111f41 100%);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.25);
}

.submit-btn:hover {
  transform: translateY(-1px);
}

.google-btn {
  border-color: #d0d5dd;
  background: #f9fafb;
  color: #344054;
}

.google-btn:hover {
  background: #f3f4f6;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.animate-slide-up {
  animation: slideUp 0.35s ease-out;
}

@media (max-width: 768px) {
  .auth-modal {
    height: auto;
    min-height: 560px;
  }

  .side-arrow {
    display: none;
  }
}
</style>
