import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (user.id) {
      config.headers['User-Id'] = user.id
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      // 不再直接 alert，而是返回错误让组件处理
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    return res
  },
  error => {
    // 处理 HTTP 错误
    let errorMessage = '网络错误，请稍后重试'
    
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      // 优先使用后端返回的错误消息
      if (data && data.message) {
        errorMessage = data.message
      } else {
        // 根据状态码提供友好的错误提示
        switch (status) {
          case 400:
            errorMessage = '请求参数错误，请检查输入'
            break
          case 401:
            errorMessage = '未授权，请重新登录'
            break
          case 403:
            errorMessage = '没有权限执行此操作'
            break
          case 404:
            errorMessage = '请求的资源不存在'
            break
          case 500:
            errorMessage = '服务器错误，请稍后重试'
            break
          default:
            errorMessage = `操作失败（错误码：${status}）`
        }
      }
    } else if (error.request) {
      errorMessage = '无法连接到服务器，请检查网络'
    }
    
    return Promise.reject(new Error(errorMessage))
  }
)

// 用户相关API
export const userApi = {
  register(data) {
    return request.post('/users/register', data)
  },
  login(data) {
    return request.post('/users/login', data)
  },
  getUserById(id) {
    return request.get(`/users/${id}`)
  }
}

// 自习室相关API
export const studyRoomApi = {
  getAll() {
    return request.get('/study-rooms')
  },
  getById(id) {
    return request.get(`/study-rooms/${id}`)
  },
  create(data) {
    return request.post('/study-rooms', data)
  },
  update(id, data) {
    return request.put(`/study-rooms/${id}`, data)
  },
  delete(id) {
    return request.delete(`/study-rooms/${id}`)
  }
}

// 时间段相关API
export const timeSlotApi = {
  getByStudyRoom(studyRoomId, date) {
    const params = date ? { date } : {}
    return request.get(`/time-slots/study-room/${studyRoomId}`, { params })
  },
  create(data) {
    return request.post('/time-slots', data)
  },
  delete(id) {
    return request.delete(`/time-slots/${id}`)
  }
}

// 预约相关API
export const reservationApi = {
  create(data) {
    return request.post('/reservations', data)
  },
  getUserReservations(userId) {
    return request.get(`/reservations/user/${userId}`)
  },
  getAllReservations() {
    return request.get('/reservations/all')
  },
  cancel(id, userId) {
    return request.delete(`/reservations/${id}`, { params: { userId } })
  }
}

export default request
