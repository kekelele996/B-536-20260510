<template>
  <div>
    <div class="header">
      <div class="header-content">
        <h1>自习室预约系统</h1>
        <div class="nav">
          <router-link to="/study-rooms">自习室列表</router-link>
          <router-link to="/my-reservations">我的预约</router-link>
          <router-link to="/admin" class="active">管理后台</router-link>
          <a href="#" @click.prevent="handleLogout">退出登录</a>
        </div>
      </div>
    </div>

    <div class="container">
      <!-- 自习室管理 -->
      <div class="card">
        <h2 style="color: #667eea; margin-bottom: 20px;">自习室管理</h2>
        
        <form @submit.prevent="handleCreateRoom" style="margin-bottom: 30px; padding: 20px; background: #f7fafc; border-radius: 8px;">
          <h3 style="margin-bottom: 16px;">添加新自习室</h3>
          <div class="form-group">
            <label>名称</label>
            <input v-model="roomForm.name" type="text" required />
          </div>
          <div class="form-group">
            <label>位置</label>
            <input v-model="roomForm.location" type="text" required />
          </div>
          <div class="form-group">
            <label>容量</label>
            <input v-model.number="roomForm.capacity" type="number" required />
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea v-model="roomForm.description" rows="3"></textarea>
          </div>
          <button type="submit" class="btn btn-primary">添加自习室</button>
        </form>

        <table>
          <thead>
            <tr>
              <th>名称</th>
              <th>位置</th>
              <th>容量</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="room in rooms" :key="room.id">
              <td>{{ room.name }}</td>
              <td>{{ room.location }}</td>
              <td>{{ room.capacity }}</td>
              <td>
                <span :class="room.status === 'ACTIVE' ? 'badge badge-success' : 'badge badge-danger'">
                  {{ room.status === 'ACTIVE' ? '启用' : '禁用' }}
                </span>
              </td>
              <td>
                <button @click="deleteRoom(room.id)" class="btn btn-danger">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 时间段管理 -->
      <div class="card">
        <h2 style="color: #667eea; margin-bottom: 20px;">时间段管理</h2>
        
        <form @submit.prevent="handleCreateTimeSlot" style="margin-bottom: 30px; padding: 20px; background: #f7fafc; border-radius: 8px;">
          <h3 style="margin-bottom: 16px;">添加时间段</h3>
          <div class="form-group">
            <label>选择自习室</label>
            <select v-model="timeSlotForm.studyRoomId" required>
              <option value="">请选择</option>
              <option v-for="room in rooms" :key="room.id" :value="room.id">
                {{ room.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>日期</label>
            <input v-model="timeSlotForm.date" type="date" required />
          </div>
          <div class="form-group">
            <label>开始时间</label>
            <input v-model="timeSlotForm.startTime" type="time" required />
          </div>
          <div class="form-group">
            <label>结束时间</label>
            <input v-model="timeSlotForm.endTime" type="time" required />
          </div>
          <div class="form-group">
            <label>可用座位数</label>
            <input v-model.number="timeSlotForm.availableSeats" type="number" required />
          </div>
          <button type="submit" class="btn btn-primary">添加时间段</button>
        </form>
      </div>

      <!-- 所有预约记录 -->
      <div class="card">
        <h2 style="color: #667eea; margin-bottom: 20px;">所有预约记录</h2>
        
        <div v-if="allReservations.length === 0" class="empty-state">
          <p>暂无预约记录</p>
        </div>

        <table v-else>
          <thead>
            <tr>
              <th>用户</th>
              <th>自习室</th>
              <th>日期</th>
              <th>时间段</th>
              <th>座位号</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="reservation in allReservations" :key="reservation.id">
              <td>{{ reservation.username }}</td>
              <td>{{ reservation.studyRoomName }}</td>
              <td>{{ reservation.date }}</td>
              <td>{{ reservation.startTime }} - {{ reservation.endTime }}</td>
              <td>{{ reservation.seatNumber }}</td>
              <td>
                <span :class="getStatusBadgeClass(reservation.status)">
                  {{ getStatusText(reservation.status) }}
                </span>
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
import { studyRoomApi, timeSlotApi, reservationApi } from '../api'
import { useToast } from '../composables/useToast'
import { useConfirm } from '../composables/useConfirm'

const router = useRouter()
const { success, error } = useToast()
const { confirm: showConfirm } = useConfirm()
const rooms = ref([])
const allReservations = ref([])
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

const roomForm = ref({
  name: '',
  location: '',
  capacity: 0,
  description: '',
  status: 'ACTIVE'
})

const timeSlotForm = ref({
  studyRoomId: '',
  date: '',
  startTime: '',
  endTime: '',
  availableSeats: 0
})

const loadRooms = async () => {
  try {
    const res = await studyRoomApi.getAll()
    rooms.value = res.data
  } catch (err) {
    error(err.message || '加载自习室失败')
  }
}

const loadAllReservations = async () => {
  try {
    const res = await reservationApi.getAllReservations()
    allReservations.value = res.data
  } catch (err) {
    error(err.message || '加载预约失败')
  }
}

const handleCreateRoom = async () => {
  try {
    await studyRoomApi.create(roomForm.value)
    success('添加成功')
    roomForm.value = {
      name: '',
      location: '',
      capacity: 0,
      description: '',
      status: 'ACTIVE'
    }
    await loadRooms()
  } catch (err) {
    error(err.message || '添加自习室失败')
  }
}

const deleteRoom = async (id) => {
  if (!await showConfirm('确定要删除这个自习室吗？')) return
  
  try {
    await studyRoomApi.delete(id)
    success('删除成功')
    await loadRooms()
  } catch (err) {
    error(err.message || '删除自习室失败')
  }
}

const handleCreateTimeSlot = async () => {
  try {
    await timeSlotApi.create(timeSlotForm.value)
    success('添加成功')
    timeSlotForm.value = {
      studyRoomId: '',
      date: '',
      startTime: '',
      endTime: '',
      availableSeats: 0
    }
  } catch (err) {
    error(err.message || '添加时间段失败')
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
  loadRooms()
  loadAllReservations()
})
</script>
