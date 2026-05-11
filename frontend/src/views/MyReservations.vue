<template>
  <div>
    <div class="header">
      <div class="header-content">
        <h1>自习室预约系统</h1>
        <div class="nav">
          <router-link to="/study-rooms">自习室列表</router-link>
          <router-link to="/my-reservations" class="active">我的预约</router-link>
          <router-link v-if="user.role === 'ADMIN'" to="/admin">管理后台</router-link>
          <NotificationBell :userId="user.id" @notification-click="handleNotificationClick" />
          <a href="#" @click.prevent="handleLogout">退出登录</a>
        </div>
      </div>
    </div>

    <div class="container">
      <div class="card" style="margin-bottom: 20px;">
        <h2 style="color: #667eea; margin-bottom: 20px;">我的预约</h2>
        
        <div v-if="reservations.length === 0" class="empty-state">
          <h3>暂无预约记录</h3>
          <p>快去预约自习室吧！</p>
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
            <tr v-for="reservation in reservations" :key="'res-' + reservation.id">
              <td>{{ reservation.studyRoomName }}</td>
              <td>{{ reservation.location }}</td>
              <td>{{ reservation.date }}</td>
              <td>{{ reservation.startTime }} - {{ reservation.endTime }}</td>
              <td>{{ reservation.seatNumber }}</td>
              <td>
                <span :class="getReservationStatusBadgeClass(reservation.status)">
                  {{ getReservationStatusText(reservation.status) }}
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

      <div class="card">
        <h2 style="color: #f6ad55; margin-bottom: 20px;">我的候补</h2>
        
        <div v-if="waitlist.length === 0" class="empty-state">
          <h3>暂无候补记录</h3>
          <p>当预约满员时可以加入候补队列</p>
        </div>

        <table v-else>
          <thead>
            <tr>
              <th>自习室</th>
              <th>位置</th>
              <th>日期</th>
              <th>时间段</th>
              <th>排队位置</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in waitlist" :key="'wait-' + item.id">
              <td>{{ item.studyRoomName }}</td>
              <td>{{ item.location }}</td>
              <td>{{ item.date }}</td>
              <td>{{ item.startTime }} - {{ item.endTime }}</td>
              <td>
                <span class="badge badge-info">第 {{ item.queuePosition }} 位</span>
              </td>
              <td>
                <span :class="getWaitlistStatusBadgeClass(item.status)">
                  {{ getWaitlistStatusText(item.status) }}
                </span>
              </td>
              <td>
                <button 
                  v-if="item.status === 'WAITING'"
                  @click="cancelWaitlist(item.id)" 
                  class="btn btn-danger"
                >
                  取消候补
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="reservations.length === 0 && waitlist.length === 0" style="text-align: center; margin-top: 20px;">
        <button @click="$router.push('/study-rooms')" class="btn btn-primary">
          去预约
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { reservationApi, waitlistApi } from '../api'
import { useToast } from '../composables/useToast'
import { useConfirm } from '../composables/useConfirm'
import NotificationBell from '../components/NotificationBell.vue'

const router = useRouter()
const { success, error } = useToast()
const { confirm: showConfirm } = useConfirm()
const reservations = ref([])
const waitlist = ref([])
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

const handleNotificationClick = (notification) => {
  if (notification.type === 'WAITLIST_SUCCESS') {
    loadReservations()
    loadWaitlist()
  }
}

const loadReservations = async () => {
  try {
    const res = await reservationApi.getUserReservations(user.value.id)
    reservations.value = res.data
  } catch (err) {
    error(err.message || '加载预约失败')
  }
}

const loadWaitlist = async () => {
  try {
    const res = await waitlistApi.getUserWaitlist(user.value.id)
    waitlist.value = res.data
  } catch (err) {
    error(err.message || '加载候补失败')
  }
}

const cancelReservation = async (id) => {
  if (!await showConfirm('确定要取消这个预约吗？取消后候补用户会按顺序顶上。')) return
  
  try {
    await reservationApi.cancel(id, user.value.id)
    success('取消成功，候补用户已按顺序顶上')
    await Promise.all([loadReservations(), loadWaitlist()])
  } catch (err) {
    error(err.message || '取消预约失败')
  }
}

const cancelWaitlist = async (id) => {
  if (!await showConfirm('确定要取消这个候补吗？')) return
  
  try {
    await waitlistApi.cancel(id, user.value.id)
    success('取消候补成功')
    await loadWaitlist()
  } catch (err) {
    error(err.message || '取消候补失败')
  }
}

const getReservationStatusBadgeClass = (status) => {
  if (status === 'ACTIVE') return 'badge badge-success'
  if (status === 'CANCELLED') return 'badge badge-danger'
  return 'badge badge-warning'
}

const getReservationStatusText = (status) => {
  const statusMap = {
    'ACTIVE': '已预约',
    'CANCELLED': '已取消',
    'COMPLETED': '已完成'
  }
  return statusMap[status] || status
}

const getWaitlistStatusBadgeClass = (status) => {
  if (status === 'WAITING') return 'badge badge-warning'
  if (status === 'CONVERTED') return 'badge badge-success'
  if (status === 'CANCELLED') return 'badge badge-danger'
  return 'badge badge-secondary'
}

const getWaitlistStatusText = (status) => {
  const statusMap = {
    'WAITING': '等待中',
    'CONVERTED': '已预约',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const handleLogout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  loadReservations()
  loadWaitlist()
})
</script>
