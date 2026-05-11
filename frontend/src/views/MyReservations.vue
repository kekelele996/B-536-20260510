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
      <div v-if="unreadCount > 0" class="card notification-banner" style="background: #ebf8ff; border-left: 4px solid #4299e1;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h3 style="color: #2b6cb0; margin: 0;">
            📬 您有 {{ unreadCount }} 条未读通知
          </h3>
          <button @click="markAllRead" class="btn" style="background: #4299e1; color: white; font-size: 13px; padding: 6px 14px;">
            全部已读
          </button>
        </div>
        <div style="margin-top: 12px;">
          <div 
            v-for="n in notifications.filter(n => !n.isRead)" 
            :key="n.id" 
            class="notification-item"
            @click="handleNotificationClick(n)"
          >
            <span :class="n.isRead ? 'notification-read' : 'notification-unread'">●</span>
            {{ n.message }}
            <span class="notification-time">{{ formatTime(n.createdAt) }}</span>
          </div>
        </div>
      </div>

      <div v-if="notifications.some(n => n.isRead)" class="card" style="margin-bottom: 20px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
          <h3 style="color: #718096; margin: 0; font-size: 15px;">历史通知</h3>
        </div>
        <div 
          v-for="n in notifications.filter(n => n.isRead)" 
          :key="n.id" 
          class="notification-item"
          style="color: #a0aec0;"
        >
          <span class="notification-read">●</span>
          {{ n.message }}
          <span class="notification-time">{{ formatTime(n.createdAt) }}</span>
        </div>
      </div>

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

      <div class="card">
        <h2 style="color: #d69e2e; margin-bottom: 20px;">我的候补</h2>
        
        <div v-if="waitlistEntries.length === 0" class="empty-state" style="padding: 30px;">
          <p style="color: #a0aec0;">暂无候补记录</p>
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
            <tr v-for="entry in waitlistEntries" :key="entry.id">
              <td>{{ entry.studyRoomName }}</td>
              <td>{{ entry.location }}</td>
              <td>{{ entry.date }}</td>
              <td>{{ entry.startTime }} - {{ entry.endTime }}</td>
              <td>
                <span class="badge badge-warning">第 {{ entry.position }} 位</span>
              </td>
              <td>
                <span :class="getWaitlistStatusBadgeClass(entry.status)">
                  {{ getWaitlistStatusText(entry.status) }}
                </span>
              </td>
              <td>
                <button 
                  v-if="entry.status === 'WAITING'"
                  @click="leaveWaitlist(entry.id)" 
                  class="btn btn-danger"
                  style="font-size: 13px; padding: 6px 14px;"
                >
                  退出候补
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
import { reservationApi, waitlistApi, notificationApi } from '../api'
import { useToast } from '../composables/useToast'
import { useConfirm } from '../composables/useConfirm'

const router = useRouter()
const { success, error } = useToast()
const { confirm: showConfirm } = useConfirm()
const reservations = ref([])
const waitlistEntries = ref([])
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

const loadWaitlist = async () => {
  try {
    const res = await waitlistApi.getUserWaitlist(user.value.id)
    waitlistEntries.value = res.data
  } catch (err) {
    error(err.message || '加载候补记录失败')
  }
}

const loadNotifications = async () => {
  try {
    const [listRes, countRes] = await Promise.all([
      notificationApi.getUserNotifications(user.value.id),
      notificationApi.getUnreadCount(user.value.id)
    ])
    notifications.value = listRes.data
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
    await Promise.all([loadReservations(), loadWaitlist(), loadNotifications()])
  } catch (err) {
    error(err.message || '取消预约失败')
  }
}

const leaveWaitlist = async (id) => {
  if (!await showConfirm('确定要退出候补吗？')) return

  try {
    await waitlistApi.leave(id, user.value.id)
    success('已退出候补')
    await loadWaitlist()
  } catch (err) {
    error(err.message || '退出候补失败')
  }
}

const markAllRead = async () => {
  try {
    await notificationApi.markAllRead(user.value.id)
    await loadNotifications()
  } catch (err) {
    error(err.message || '操作失败')
  }
}

const handleNotificationClick = async (n) => {
  if (!n.isRead) {
    try {
      await notificationApi.markRead(n.id, user.value.id)
      await loadNotifications()
    } catch (err) {
      // ignore
    }
  }
}

const formatTime = (dt) => {
  if (!dt) return ''
  const d = new Date(dt)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
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

const getWaitlistStatusBadgeClass = (status) => {
  if (status === 'WAITING') return 'badge badge-warning'
  if (status === 'CONVERTED') return 'badge badge-success'
  if (status === 'CANCELLED') return 'badge badge-danger'
  return 'badge badge-info'
}

const getWaitlistStatusText = (status) => {
  const map = {
    'WAITING': '排队中',
    'CONVERTED': '已自动预约',
    'CANCELLED': '已退出'
  }
  return map[status] || status
}

const handleLogout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  loadReservations()
  loadWaitlist()
  loadNotifications()
})
</script>
