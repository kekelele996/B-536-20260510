<template>
  <div>
    <div class="header">
      <div class="header-content">
        <h1>自习室预约系统</h1>
        <div class="nav">
          <router-link to="/study-rooms" class="active">自习室列表</router-link>
          <router-link to="/my-reservations">我的预约</router-link>
          <router-link v-if="user.role === 'ADMIN'" to="/admin">管理后台</router-link>
          <NotificationBell :userId="user.id" />
          <a href="#" @click.prevent="handleLogout">退出登录</a>
        </div>
      </div>
    </div>

    <div class="container">
      <h2 style="margin-bottom: 20px; color: white;">自习室列表</h2>
      
      <div v-if="rooms.length === 0" class="empty-state card">
        <h3>暂无自习室</h3>
        <p>请联系管理员添加自习室</p>
      </div>

      <div class="grid" v-else>
        <div v-for="room in rooms" :key="room.id" class="card">
          <h3 style="color: #667eea; margin-bottom: 12px;">{{ room.name }}</h3>
          <p style="color: #718096; margin-bottom: 8px;">
            <strong>位置：</strong>{{ room.location }}
          </p>
          <p style="color: #718096; margin-bottom: 8px;">
            <strong>容量：</strong>{{ room.capacity }} 人
          </p>
          <p style="color: #718096; margin-bottom: 16px;">
            <strong>描述：</strong>{{ room.description }}
          </p>
          <button @click="selectRoom(room.id)" class="btn btn-primary" style="width: 100%">
            查看可预约时间
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { studyRoomApi } from '../api'
import { useToast } from '../composables/useToast'
import NotificationBell from '../components/NotificationBell.vue'

const router = useRouter()
const { error } = useToast()
const rooms = ref([])
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

const loadRooms = async () => {
  try {
    const res = await studyRoomApi.getAll()
    rooms.value = res.data
  } catch (err) {
    error(err.message || '加载自习室失败')
  }
}

const selectRoom = (roomId) => {
  router.push(`/time-slots/${roomId}`)
}

const handleLogout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  loadRooms()
})
</script>
