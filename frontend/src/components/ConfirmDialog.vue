<template>
  <Transition name="modal">
    <div v-if="state.show" class="modal-mask">
      <div class="modal-wrapper" @click.self="handleCancel">
        <div class="modal-container">
          <div class="modal-header">
            <h3>{{ state.title }}</h3>
          </div>

          <div class="modal-body">
            <p>{{ state.message }}</p>
          </div>

          <div class="modal-footer">
            <button class="modal-btn cancel" @click="handleCancel">
              取消
            </button>
            <button class="modal-btn confirm" @click="handleConfirm">
              确定
            </button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { useConfirm } from '../composables/useConfirm'

const { state, handleConfirm, handleCancel } = useConfirm()
</script>

<style scoped>
.modal-mask {
  position: fixed;
  z-index: 9998;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  transition: opacity 0.3s ease;
  backdrop-filter: blur(4px);
}

.modal-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-container {
  width: 300px;
  margin: 0px auto;
  padding: 24px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  transform: scale(1);
}

.modal-header h3 {
  margin-top: 0;
  margin-bottom: 8px;
  color: #1a202c;
  font-size: 1.125rem;
  font-weight: 600;
  line-height: 1.5;
}

.modal-body {
  margin: 10px 0 24px;
}

.modal-body p {
  color: #4a5568;
  font-size: 1rem;
  line-height: 1.5;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.modal-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 500;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
  outline: none;
}

.modal-btn.cancel {
  background-color: #f7fafc;
  color: #4a5568;
  border: 1px solid #e2e8f0;
}

.modal-btn.cancel:hover {
  background-color: #edf2f7;
}

.modal-btn.confirm {
  background-color: #667eea;
  color: white;
  box-shadow: 0 4px 6px -1px rgba(102, 126, 234, 0.4);
}

.modal-btn.confirm:hover {
  background-color: #5a67d8;
  transform: translateY(-1px);
}

.modal-btn.confirm:active {
  transform: translateY(0);
}

/* Transitions */
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.95);
}
</style>
