import { createRouter, createWebHistory } from 'vue-router'
import { useToast } from '../composables/useToast'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import StudyRoomList from '../views/StudyRoomList.vue'
import TimeSlotSelect from '../views/TimeSlotSelect.vue'
import MyReservations from '../views/MyReservations.vue'
import Admin from '../views/Admin.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/study-rooms',
    name: 'StudyRoomList',
    component: StudyRoomList,
    meta: { requiresAuth: true }
  },
  {
    path: '/time-slots/:roomId',
    name: 'TimeSlotSelect',
    component: TimeSlotSelect,
    meta: { requiresAuth: true }
  },
  {
    path: '/my-reservations',
    name: 'MyReservations',
    component: MyReservations,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: Admin,
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  const { warning } = useToast()
  
  if (to.meta.requiresAuth && !user.id) {
    warning('请先登录')
    next('/login')
  } else if (to.meta.requiresAdmin && user.role !== 'ADMIN') {
    warning('需要管理员权限')
    next('/study-rooms')
  } else {
    next()
  }
})

export default router
