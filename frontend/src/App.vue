<template>
  <div id="app">
    <router-view></router-view>
    <Toast 
      :show="toast.show" 
      :message="toast.message" 
      :type="toast.type"
      @close="closeToast"
    />
    <ConfirmDialog />
  </div>
</template>

<script setup>
import Toast from './components/Toast.vue'
import ConfirmDialog from './components/ConfirmDialog.vue'
import { useToast } from './composables/useToast'

const { toast, closeToast } = useToast()
</script>
