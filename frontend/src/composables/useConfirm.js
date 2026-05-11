import { ref } from 'vue'

const state = ref({
  show: false,
  title: '提示',
  message: '',
  resolve: null,
  reject: null
})

export function useConfirm() {
  const confirm = (message, title = '提示') => {
    state.value.message = message
    state.value.title = title
    state.value.show = true
    
    return new Promise((resolve, reject) => {
      state.value.resolve = resolve
      state.value.reject = reject
    })
  }

  const handleConfirm = () => {
    state.value.show = false
    if (state.value.resolve) {
      state.value.resolve(true)
    }
  }

  const handleCancel = () => {
    state.value.show = false
    if (state.value.resolve) {
      state.value.resolve(false)
    }
  }

  return {
    state,
    confirm,
    handleConfirm,
    handleCancel
  }
}
