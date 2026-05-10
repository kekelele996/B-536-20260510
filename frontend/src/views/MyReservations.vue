<template>
  <div>
    <div class="header">
      <div class="header-content">
        <h1>自习室预约系统</h1>
        <div class="nav">
          <router-link to="/study-rooms">自习室列表</router-link>
          <router-link to="/my-reservations" class="active">我的预约</router-link>
          <router-link v-if="user.role === 'ADMIN'" to="/admin">管理后台</router-link>
          <a href="#" @click.prevent="handleLogout">退出登录</a>
        </div>
      </div>
    </div>

    <div class="container">
      <div class="card">
        <h2 style="color: #667eea; margin-bottom: 20px;">我的预约</h2>
        
        <div v-if="reservations.length === 0" class="empty-state">
          <h3>暂无预约记录</h3>
          <p>快去预约自习室吧！</p>
          <button @click="$router.push('/study-rooms')" class="btn btn-primary" style="margin-top: 20px;">
            去预约
          </button>
        </div>

        <table v-else>
          <thead>
            <tr>
              <th>自习室</th>
              <th>位置</th>
              <th>日期</th>
              <th>时间段</th>
              <th>座位号</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="reservation in reservations" :key="reservation.id">
              <td>{{ reservation.studyRoomName }}</td>
              <td>{{ reservation.location }}</td>
              <td>{{ reservation.date }}</td>
              <td>{{ reservation.startTime }} - {{ reservation.endTime }}</td>
              <td>{{ reservation.seatNumber }}</td>
              <td>
                <span :class="getStatusBadgeClass(reservation.status)">
                  {{ getStatusText(reservation.status) }}
                </span>
              </td>
              <td>
                <button 
                  v-if="reservation.status === 'ACTIVE'"
                  @click="cancelReservation(reservation.id)" 
                  class="btn btn-danger"
                >
                  取消预约
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { reservationApi } from '../api'
import { useToast } from '../composables/useToast'
import { useConfirm } from '../composables/useConfirm'

const router = useRouter()
const { success, error } = useToast()
const { confirm: showConfirm } = useConfirm()
const reservations = ref([])
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

const loadReservations = async () => {
  try {
    const res = await reservationApi.getUserReservations(user.value.id)
    reservations.value = res.data
  } catch (err) {
    error(err.message || '加载预约失败')
  }
}

const cancelReservation = async (id) => {
  if (!await showConfirm('确定要取消这个预约吗？')) return
  
  try {
    await reservationApi.cancel(id, user.value.id)
    success('取消成功')
    await loadReservations()
  } catch (err) {
    error(err.message || '取消预约失败')
  }
}

const getStatusBadgeClass = (status) => {
  if (status === 'ACTIVE') return 'badge badge-success'
  if (status === 'CANCELLED') return 'badge badge-danger'
  return 'badge badge-warning'
}

const getStatusText = (status) => {
  const statusMap = {
    'ACTIVE': '已预约',
    'CANCELLED': '已取消',
    'COMPLETED': '已完成'
  }
  return statusMap[status] || status
}

const handleLogout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  loadReservations()
})
</script>
