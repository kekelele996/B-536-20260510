<template>
  <div>
    <div class="header">
      <div class="header-content">
        <h1>自习室预约系统</h1>
        <div class="nav">
          <router-link to="/study-rooms">自习室列表</router-link>
          <router-link to="/my-reservations">我的预约</router-link>
          <router-link v-if="user.role === 'ADMIN'" to="/admin">管理后台</router-link>
          <NotificationBell :userId="user.id" />
          <a href="#" @click.prevent="handleLogout">退出登录</a>
        </div>
      </div>
    </div>

    <div class="container">
      <div class="card" style="margin-bottom: 20px;">
        <button @click="$router.back()" class="btn" style="background: #e2e8f0; color: #2d3748;">
          ← 返回
        </button>
      </div>

      <div class="card">
        <h2 style="color: #667eea; margin-bottom: 20px;">选择预约时间</h2>
        
        <div v-if="timeSlots.length === 0" class="empty-state">
          <h3>暂无可预约时间段</h3>
          <p>请稍后再试或选择其他自习室</p>
        </div>

        <table v-else>
          <thead>
            <tr>
              <th>日期</th>
              <th>开始时间</th>
              <th>结束时间</th>
              <th>剩余座位</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="slot in timeSlots" :key="slot.id">
              <td>{{ slot.date }}</td>
              <td>{{ slot.startTime }}</td>
              <td>{{ slot.endTime }}</td>
              <td>
                <span :class="getSeatBadgeClass(slot.availableSeats)">
                  {{ slot.availableSeats }} 个
                </span>
              </td>
              <td>
                <button 
                  @click="handleButtonClick(slot)" 
                  class="btn"
                  :class="getButtonClass(slot)"
                  :disabled="isButtonDisabled(slot)"
                >
                  {{ getButtonText(slot) }}
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
import { useRoute, useRouter } from 'vue-router'
import { timeSlotApi, reservationApi, waitlistApi } from '../api'
import { useToast } from '../composables/useToast'
import NotificationBell from '../components/NotificationBell.vue'

const route = useRoute()
const router = useRouter()
const { success, error } = useToast()
const timeSlots = ref([])
const myReservations = ref([])
const myWaitlist = ref([])
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

const loadTimeSlots = async () => {
  try {
    const roomId = route.params.roomId
    const res = await timeSlotApi.getByStudyRoom(roomId)
    timeSlots.value = res.data
  } catch (err) {
    error(err.message || '加载时间段失败')
  }
}

const loadMyReservations = async () => {
  try {
    const res = await reservationApi.getUserReservations(user.value.id)
    myReservations.value = res.data.filter(r => r.status === 'ACTIVE')
  } catch (err) {
    console.error('加载预约记录失败', err)
  }
}

const loadMyWaitlist = async () => {
  try {
    const res = await waitlistApi.getUserWaitlist(user.value.id)
    myWaitlist.value = res.data.filter(w => w.status === 'WAITING')
  } catch (err) {
    console.error('加载候补记录失败', err)
  }
}

const isAlreadyReserved = (timeSlotId) => {
  return myReservations.value.some(r => r.timeSlotId === timeSlotId)
}

const isInWaitlist = (timeSlotId) => {
  return myWaitlist.value.some(w => w.timeSlotId === timeSlotId)
}

const getWaitlistPosition = (timeSlotId) => {
  const item = myWaitlist.value.find(w => w.timeSlotId === timeSlotId)
  return item ? item.queuePosition : 0
}

const getButtonText = (slot) => {
  if (isAlreadyReserved(slot.id)) {
    return '已预约'
  }
  if (isInWaitlist(slot.id)) {
    return `候补第${getWaitlistPosition(slot.id)}位`
  }
  if (slot.availableSeats === 0) {
    return '加入候补'
  }
  return '立即预约'
}

const handleButtonClick = async (slot) => {
  if (isAlreadyReserved(slot.id)) {
    return
  }
  if (isInWaitlist(slot.id)) {
    await cancelWaitlist(slot.id)
  } else if (slot.availableSeats === 0) {
    await joinWaitlist(slot.id)
  } else {
    await makeReservation(slot.id)
  }
}

const makeReservation = async (timeSlotId) => {
  try {
    await reservationApi.create({
      userId: user.value.id,
      timeSlotId
    })
    success('预约成功')
    await Promise.all([loadTimeSlots(), loadMyReservations(), loadMyWaitlist()])
  } catch (err) {
    error(err.message || '预约失败')
  }
}

const joinWaitlist = async (timeSlotId) => {
  try {
    await waitlistApi.join({
      userId: user.value.id,
      timeSlotId
    })
    success('加入候补成功，有人取消预约时会按顺序通知您')
    await Promise.all([loadTimeSlots(), loadMyReservations(), loadMyWaitlist()])
  } catch (err) {
    error(err.message || '加入候补失败')
  }
}

const cancelWaitlist = async (timeSlotId) => {
  try {
    const item = myWaitlist.value.find(w => w.timeSlotId === timeSlotId)
    if (item) {
      await waitlistApi.cancel(item.id, user.value.id)
      success('已取消候补')
      await Promise.all([loadTimeSlots(), loadMyReservations(), loadMyWaitlist()])
    }
  } catch (err) {
    error(err.message || '取消候补失败')
  }
}

const getSeatBadgeClass = (seats) => {
  if (seats === 0) return 'badge badge-danger'
  if (seats < 10) return 'badge badge-warning'
  return 'badge badge-success'
}

const getButtonClass = (slot) => {
  if (isAlreadyReserved(slot.id)) {
    return 'btn btn-secondary'
  }
  if (isInWaitlist(slot.id)) {
    return 'btn btn-warning'
  }
  if (slot.availableSeats === 0) {
    return 'btn btn-info'
  }
  return 'btn btn-success'
}

const isButtonDisabled = (slot) => {
  return isAlreadyReserved(slot.id)
}

const handleLogout = () => {
  localStorage.removeItem('user')
  router.push('/login')
}

onMounted(() => {
  loadTimeSlots()
  loadMyReservations()
  loadMyWaitlist()
})
</script>
