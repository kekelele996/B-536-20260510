<template>
  <div class="login-container">
    <div class="login-box">
      <h2>校园自习室预约系统</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" type="text" placeholder="请输入用户名" required />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" required />
        </div>
        <button type="submit" class="btn btn-primary" style="width: 100%">登录</button>
        <div class="text-center mt-3">
          <span>还没有账号？</span>
          <router-link to="/register" class="link">立即注册</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../api'
import { useToast } from '../composables/useToast'

const router = useRouter()
const { success, error } = useToast()

const form = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  try {
    const res = await userApi.login(form.value)
    localStorage.setItem('user', JSON.stringify(res.data))
    success('登录成功')
    setTimeout(() => {
      router.push('/study-rooms')
    }, 500)
  } catch (err) {
    error(err.message || '登录失败')
  }
}
</script>
