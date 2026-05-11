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
              <td>
                <template v-if="reservation.status === 'WAITLIST'">
                  <span class="badge badge-warning">候补第 {{ reservation.waitlistPosition }} 位</span>
                </template>
                <template v-else>
                  {{ reservation.seatNumber || '-' }}
                </template>
              </td>
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
                <button 
                  v-else-if="reservation.status === 'WAITLIST'"
                  @click="cancelWaitlist(reservation.id)" 
                  class="btn"
                  style="background: #ed8936; color: white;"
                >
                  取消候补
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="card" style="margin-top: 20px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
          <h2 style="color: #667eea; margin: 0;">我的通知</h2>
          <button 
            v-if="unreadCount > 0" 
            @click="markAllAsRead" 
            class="btn"
            style="background: #667eea; color: white;"
          >
            全部标记已读
          </button>
        </div>
        
        <div v-if="notifications.length === 0" class="empty-state">
          <h3>暂无通知</h3>
          <p>您的候补预约如果成功会在这里收到通知</p>
        </div>

        <div v-else class="notification-list">
          <div 
            v-for="notification in notifications" 
            :key="notification.id" 
            class="notification-item"
            :class="{ 'notification-unread': notification.status === 'UNREAD' }"
            @click="markAsRead(notification.id)"
          >
            <div class="notification-header">
              <span class="notification-title" :class="getNotificationTitleClass(notification.type)">
                {{ notification.title }}
              </span>
              <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
            </div>
            <div class="notification-content">{{ notification.content }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { reservationApi, notificationApi } from '../api'
import { useToast } from '../composables/useToast'
import { useConfirm } from '../composables/useConfirm'

const router = useRouter()
const { success, error } = useToast()
const { confirm: showConfirm } = useConfirm()
const reservations = ref([])
const notifications = ref([])
const unreadCount = ref(0)
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

const loadReservations = async () => {
  try {
    const res = await reservationApi.getUserReservations(user.value.id)
    reservations.value = res.data
  } catch (err) {
    error(err.message || '加载预约失败')
  }
}

const loadNotifications = async () => {
  try {
    const res = await notificationApi.getUserNotifications(user.value.id)
    notifications.value = res.data
    const countRes = await notificationApi.getUnreadCount(user.value.id)
    unreadCount.value = countRes.data
  } catch (err) {
    console.error('加载通知失败', err)
  }
}

const cancelReservation = async (id) => {
  if (!await showConfirm('确定要取消这个预约吗？')) return
  
  try {
    await reservationApi.cancel(id, user.value.id)
    success('取消成功')
    await Promise.all([loadReservations(), loadNotifications()])
  } catch (err) {
    error(err.message || '取消预约失败')
  }
}

const cancelWaitlist = async (id) => {
  if (!await showConfirm('确定要取消候补吗？')) return
  
  try {
    await reservationApi.cancelWaitlist(id, user.value.id)
    success('取消候补成功')
    await loadReservations()
  } catch (err) {
    error(err.message || '取消候补失败')
  }
}

const markAsRead = async (id) => {
  try {
    await notificationApi.markAsRead(id, user.value.id)
    await loadNotifications()
  } catch (err) {
    console.error('标记已读失败', err)
  }
}

const markAllAsRead = async () => {
  try {
    await notificationApi.markAllAsRead(user.value.id)
    success('全部标记已读')
    await loadNotifications()
  } catch (err) {
    error(err.message || '标记已读失败')
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}

const getStatusBadgeClass = (status) => {
  if (status === 'ACTIVE') return 'badge badge-success'
  if (status === 'CANCELLED') return 'badge badge-danger'
  if (status === 'WAITLIST') return 'badge badge-warning'
  return 'badge'
}

const getStatusText = (status) => {
  const statusMap = {
    'ACTIVE': '已预约',
    'CANCELLED': '已取消',
    'COMPLETED': '已完成',
    'WAITLIST': '候补中'
  }
  return statusMap[status] || status
}

const getNotificationTitleClass = (type) => {
  if (type === 'SUCCESS') return 'notification-success'
  if (type === 'WAITLIST') return 'notification-waitlist'
  return ''
}

const handleLogout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  loadReservations()
  loadNotifications()
})
</script>

<style scoped>
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  padding: 16px;
  background: #f7fafc;
  border-radius: 8px;
  border-left: 4px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
}

.notification-item:hover {
  background: #edf2f7;
}

.notification-unread {
  background: #ebf8ff;
  border-left-color: #3182ce;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title {
  font-weight: bold;
  color: #2d3748;
}

.notification-success {
  color: #38a169;
}

.notification-waitlist {
  color: #dd6b20;
}

.notification-time {
  font-size: 12px;
  color: #718096;
}

.notification-content {
  color: #4a5568;
  font-size: 14px;
  line-height: 1.5;
}
</style>
