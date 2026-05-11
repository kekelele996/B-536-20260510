<template>
  <Transition name="toast">
    <div v-if="visible" :class="['toast', `toast-${type}`]">
      <div class="toast-icon">
        <span v-if="type === 'success'">✓</span>
        <span v-else-if="type === 'error'">✕</span>
        <span v-else-if="type === 'warning'">⚠</span>
        <span v-else>ℹ</span>
      </div>
      <div class="toast-message">{{ message }}</div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  message: String,
  type: {
    type: String,
    default: 'info' // success, error, warning, info
  },
  duration: {
    type: Number,
    default: 3000
  },
  show: Boolean
})

const emit = defineEmits(['close'])
const visible = ref(props.show)

watch(() => props.show, (newVal) => {
  visible.value = newVal
  if (newVal) {
    setTimeout(() => {
      visible.value = false
      emit('close')
    }, props.duration)
  }
})
</script>

<style scoped>
.toast {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06), 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  z-index: 9999;
  min-width: 320px;
  max-width: 500px;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.toast-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 800;
  flex-shrink: 0;
}

.toast-message {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
  color: #2d3748;
}

.toast-success {
  border-left: 4px solid #48bb78;
}

.toast-success .toast-icon {
  background: #c6f6d5;
  color: #22543d;
}

.toast-error {
  border-left: 4px solid #f56565;
}

.toast-error .toast-icon {
  background: #fed7d7;
  color: #742a2a;
}

.toast-warning {
  border-left: 4px solid #ed8936;
}

.toast-warning .toast-icon {
  background: #feebc8;
  color: #744210;
}

.toast-info {
  border-left: 4px solid #4299e1;
}

.toast-info .toast-icon {
  background: #bee3f8;
  color: #2c5282;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px) scale(0.95);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px) scale(0.95);
}
</style>
