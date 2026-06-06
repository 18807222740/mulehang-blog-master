import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 应用全局状态管理
 */
export const useAppStore = defineStore('app', () => {
  // 暗黑模式
  const isDark = ref<boolean>(false)

  // 侧边栏折叠状态
  const sidebarCollapsed = ref<boolean>(false)

  /**
   * 切换暗黑模式
   */
  function toggleDark() {
    isDark.value = !isDark.value
    updateDarkClass()
  }

  /**
   * 设置暗黑模式
   */
  function setDark(value: boolean) {
    isDark.value = value
    updateDarkClass()
  }

  /**
   * 更新 HTML 根元素的 dark 类
   */
  function updateDarkClass() {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  /**
   * 切换侧边栏折叠状态
   */
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // 初始化暗黑模式
  updateDarkClass()

  return {
    isDark,
    sidebarCollapsed,
    toggleDark,
    setDark,
    toggleSidebar
  }
})
