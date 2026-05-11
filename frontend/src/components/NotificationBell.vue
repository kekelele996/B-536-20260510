<template>
  <div class="notification-bell" @click="togglePanel">
    <div class="bell-icon">
      🔔
      <span v-if="unreadCount > 0" class="badge-count">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
    </div>

    <div v-if="showPanel" class="notification-panel">
      <div class="panel-header">
        <h3>通知中心</h3>
        <button v-if="unreadCount > 0" @click.stop="markAllAsRead" class="btn-text">
          全部已读
        </button>
      </div>

      <div class="panel-content">
        <div v-if="notifications.length === 0" class="empty-notifications">
          暂无通知
        </div>

        <div
          v-for="notification in notifications"
          :key="notification.id"
          class="notification-item"
          :class="{ unread: notification.status === 'UNREAD' }"
          @click.stop="handleNotificationClick(notification)"
        >
          <div class="notification-type">
            <span v-if="notification.type === 'WAITLIST_SUCCESS'" class="type-icon success">🎉</span>
            <span v-else class="type-icon">📢</span>
          </div>
          <div class="notification-content">
            <div class="notification-title">{{ notification.title }}</div>
            <div class="notification-text">{{ notification.content }}</div>
            <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { notificationApi } from '../api'
import { useToast } from '../composables/useToast'

const props = defineProps({
  userId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['notification-click'])

const { success } = useToast()
const showPanel = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
let pollInterval = null

const loadNotifications = async () => {
  try {
    const [allRes, countRes] = await Promise.all([
      notificationApi.getUserNotifications(props.userId),
      notificationApi.getUnreadCount(props.userId)
    ])
    notifications.value = allRes.data.slice(0, 20)
    unreadCount.value = countRes.data
  } catch (err) {
    console.error('加载通知失败', err)
  }
}

const markAllAsRead = async () => {
  try {
    await notificationApi.markAllAsRead(props.userId)
    success('已全部标记为已读')
    await loadNotifications()
  } catch (err) {
    console.error('标记已读失败', err)
  }
}

const handleNotificationClick = async (notification) => {
  if (notification.status === 'UNREAD') {
    try {
      await notificationApi.markAsRead(notification.id, props.userId)
      unreadCount.value = Math.max(0, unreadCount.value - 1)
      notification.status = 'READ'
    } catch (err) {
      console.error('标记已读失败', err)
    }
  }
  emit('notification-click', notification)
}

const togglePanel = async () => {
  showPanel.value = !showPanel.value
  if (showPanel.value) {
    await loadNotifications()
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

const handleClickOutside = (e) => {
  if (showPanel.value && !e.target.closest('.notification-bell')) {
    showPanel.value = false
  }
}

onMounted(() => {
  loadNotifications()
  pollInterval = setInterval(loadNotifications, 30000)
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  if (pollInterval) {
    clearInterval(pollInterval)
  }
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.notification-bell {
  position: relative;
  cursor: pointer;
  user-select: none;
}

.bell-icon {
  font-size: 20px;
  position: relative;
  padding: 5px;
}

.badge-count {
  position: absolute;
  top: -2px;
  right: -2px;
  background: #f56565;
  color: white;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
  font-weight: bold;
}

.notification-panel {
  position: absolute;
  top: 100%;
  right: 0;
  width: 360px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  margin-top: 10px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
  background: #f7fafc;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #2d3748;
}

.btn-text {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 4px;
}

.btn-text:hover {
  background: #ebf4ff;
}

.panel-content {
  max-height: 400px;
  overflow-y: auto;
}

.empty-notifications {
  padding: 40px 20px;
  text-align: center;
  color: #a0aec0;
  font-size: 14px;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f4f8;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-item:hover {
  background: #f7fafc;
}

.notification-item.unread {
  background: #ebf8ff;
}

.notification-item.unread:hover {
  background: #bee3f8;
}

.type-icon {
  font-size: 20px;
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #e2e8f0;
}

.type-icon.success {
  background: #c6f6d5;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 4px;
}

.notification-text {
  font-size: 13px;
  color: #4a5568;
  line-height: 1.4;
  margin-bottom: 6px;
}

.notification-time {
  font-size: 12px;
  color: #a0aec0;
}
</style>
