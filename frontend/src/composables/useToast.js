import { ref } from 'vue'

const toast = ref({
  show: false,
  message: '',
  type: 'info'
})

export function useToast() {
  const showToast = (message, type = 'info') => {
    toast.value = {
      show: true,
      message,
      type
    }
  }

  const success = (message) => showToast(message, 'success')
  const error = (message) => showToast(message, 'error')
  const warning = (message) => showToast(message, 'warning')
  const info = (message) => showToast(message, 'info')

  const closeToast = () => {
    toast.value.show = false
  }

  return {
    toast,
    showToast,
    success,
    error,
    warning,
    info,
    closeToast
  }
}
