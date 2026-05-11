<template>
  <div>
    <div class="header">
      <div class="header-content">
        <h1>自习室预约系统</h1>
        <div class="nav">
          <router-link to="/study-rooms">自习室列表</router-link>
          <router-link to="/my-reservations">我的预约</router-link>
          <router-link v-if="user.role === 'ADMIN'" to="/admin">管理后台</router-link>
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
              <th>候补人数</th>
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
                <span class="badge badge-info">
                  {{ waitlistCounts[slot.id] || 0 }} 人
                </span>
              </td>
              <td>
                <template v-if="isAlreadyReserved(slot.id)">
                  <button class="btn" disabled style="background: #a0aec0; color: white;">
                    已预约
                  </button>
                </template>
                <template v-else-if="slot.availableSeats > 0">
                  <button 
                    @click="makeReservation(slot.id)" 
                    class="btn btn-success"
                  >
                    立即预约
                  </button>
                </template>
                <template v-else-if="isUserInWaitlist(slot.id)">
                  <button 
                    @click="leaveWaitlist(slot.id)" 
                    class="btn btn-danger"
                  >
                    退出候补
                  </button>
                </template>
                <template v-else>
                  <button 
                    @click="joinWaitlist(slot.id)" 
                    class="btn btn-warning"
                  >
                    加入候补
                  </button>
                </template>
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

const route = useRoute()
const router = useRouter()
const { success, error, warning } = useToast()
const timeSlots = ref([])
const myReservations = ref([])
const myWaitlist = ref([])
const waitlistCounts = ref({})
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

const loadTimeSlots = async () => {
  try {
    const roomId = route.params.roomId
    const res = await timeSlotApi.getByStudyRoom(roomId)
    timeSlots.value = res.data
    loadWaitlistCounts()
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

const loadWaitlistCounts = async () => {
  for (const slot of timeSlots.value) {
    try {
      const res = await waitlistApi.getCount(slot.id)
      waitlistCounts.value[slot.id] = res.data
    } catch (err) {
      waitlistCounts.value[slot.id] = 0
    }
  }
}

const isAlreadyReserved = (timeSlotId) => {
  return myReservations.value.some(r => r.timeSlotId === timeSlotId)
}

const isUserInWaitlist = (timeSlotId) => {
  return myWaitlist.value.some(w => w.timeSlotId === timeSlotId)
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
    warning('已加入候补队列，有空位将通知您')
    await Promise.all([loadMyWaitlist(), loadWaitlistCounts()])
  } catch (err) {
    error(err.message || '加入候补失败')
  }
}

const leaveWaitlist = async (timeSlotId) => {
  const entry = myWaitlist.value.find(w => w.timeSlotId === timeSlotId)
  if (!entry) return
  try {
    await waitlistApi.leave(entry.id, user.value.id)
    success('已退出候补')
    await Promise.all([loadMyWaitlist(), loadWaitlistCounts()])
  } catch (err) {
    error(err.message || '退出候补失败')
  }
}

const getSeatBadgeClass = (seats) => {
  if (seats === 0) return 'badge badge-danger'
  if (seats < 10) return 'badge badge-warning'
  return 'badge badge-success'
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
