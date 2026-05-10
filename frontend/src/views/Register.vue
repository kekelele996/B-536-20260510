<template>
  <div class="login-container">
    <div class="login-box">
      <h2>用户注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" type="text" placeholder="请输入用户名" required />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" required />
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input v-model="form.email" type="email" placeholder="请输入邮箱" />
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input v-model="form.phone" type="tel" placeholder="请输入手机号" />
        </div>
        <button type="submit" class="btn btn-primary" style="width: 100%">注册</button>
        <div class="text-center mt-3">
          <span>已有账号？</span>
          <router-link to="/login" class="link">立即登录</router-link>
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
  password: '',
  email: '',
  phone: ''
})

const handleRegister = async () => {
  try {
    await userApi.register(form.value)
    success('注册成功，请登录')
    setTimeout(() => {
      router.push('/login')
    }, 1000)
  } catch (err) {
    error(err.message || '注册失败')
  }
}
</script>
