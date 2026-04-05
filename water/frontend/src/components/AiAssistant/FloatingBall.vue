<template>
  <div
    ref="ball"
    :class="ballClasses"
    :style="ballStyle"
    @mousedown="handleMouseDown"
    @click="handleClick"
  >
    <!-- AI 助手图标：使用聊天气泡+水滴组合 -->
    <svg class="ai-assistant-icon" viewBox="0 0 24 24">
      <!-- 水滴形状（居中） -->
      <path d="M12 3c-4.97 4.97-7.5 8.25-7.5 11.5a7.5 7.5 0 1 0 15 0c0-3.25-2.53-6.53-7.5-11.5z" fill="currentColor"/>
      <!-- 高光效果 -->
      <path d="M12 5.5c-3.5 3.5-5.5 6-5.5 8a5.5 5.5 0 1 0 11 0c0-2-2-4.5-5.5-8z" fill="rgba(255,255,255,0.3)"/>
    </svg>
  </div>
</template>

<script>
export default {
  name: 'AiAssistantFloatingBall',

  props: {
    // 抽屉可见性
    drawerVisible: {
      type: Boolean,
      default: false
    },
    // 警报状态（从父组件传入）
    alertState: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      // 位置
      position: {
        x: 0,
        y: 0
      },

      // 拖拽状态
      isDragging: false,
      isClick: true, // 用于区分点击和拖拽
      dragStart: {
        x: 0,
        y: 0
      },
      offset: {
        x: 0,
        y: 0
      },

      // 状态机（计划书 3.1）：Idle -> Dragging -> Snapped -> Alert
      state: 'idle', // idle, dragging, snapping, alert

      // 警报状态
      isAlert: false,

      // 透明度超时
      idleTimer: null,
      isIdleTimeout: false,

      // 主题色
      brandColor: '#0052D9',
      brandColorLight: '#3385FF',
      brandColorDark: '#3B82F6',
      brandColorDarkLight: '#60A5FA',

      // 是否在深色模式
      isDarkMode: false
    };
  },

  computed: {
    ballClasses() {
      return [
        'ai-assistant-ball',
        `ai-assistant-${this.state}`,
        {
          'ai-assistant-alert': this.isAlert || this.alertState,
          'ai-assistant-idle-timeout': this.isIdleTimeout
        }
      ];
    },

    ballStyle() {
      return {
        left: `${this.position.x}px`,
        top: `${this.position.y}px`,
        // 动态设置 CSS 变量，实现主题自适应
        '--ai-assistant-brand-color': this.isDarkMode ? this.brandColorDark : this.brandColor,
        '--ai-assistant-brand-color-light': this.isDarkMode ? this.brandColorDarkLight : this.brandColorLight,
        '--ai-assistant-brand-color-dark': this.brandColorDark,
        '--ai-assistant-brand-color-dark-light': this.brandColorDarkLight,
        '--ai-assistant-brand-color-rgba': this.isDarkMode
          ? 'rgba(59, 130, 246, 0.2)'
          : 'rgba(0, 82, 217, 0.15)'
      };
    }
  },

  mounted() {
    // 初始化位置（右下角）
    this.initializePosition();

    // 读取主题色（计划书 2.1：动态读取 TDesign 主题色）
    this.readThemeColor();

    // 检测深色模式（计划书 2.1）
    this.detectDarkMode();

    // 监听主题变化
    this.observeThemeChanges();

    // 启动空闲检测（计划书 2.3：超过 30 秒未操作，透明度降至 0.6）
    this.startIdleDetection();

    // 绑定全局拖拽事件（计划书 3.1：原生 JS 实现）
    this.bindDragEvents();

    // 从 localStorage 恢复位置
    this.restorePosition();
  },

  beforeDestroy() {
    // 清理事件监听
    this.unbindDragEvents();

    // 清理定时器
    if (this.idleTimer) {
      clearTimeout(this.idleTimer);
    }

    // 清理主题观察器
    if (this.themeObserver) {
      this.themeObserver.disconnect();
    }

    // 保存位置到 localStorage
    this.savePosition();
  },

  methods: {
    /**
     * 初始化位置
     * 计划书 2.3：智能避让，默认右下角，检测是否有重叠
     */
    initializePosition() {
      const windowWidth = window.innerWidth;
      const windowHeight = window.innerHeight;
      const ballSize = 56;
      const safeMargin = 24; // 安全距离

      // 默认右下角位置
      let x = windowWidth - ballSize - safeMargin;
      let y = windowHeight - ballSize - safeMargin;

      // 智能避让：检测是否有"回到顶部"按钮或其他固定元素
      const backToTopBtn = document.querySelector('.t-back-top, [class*="back-top"], [class*="backtotop"]');
      if (backToTopBtn) {
        const btnRect = backToTopBtn.getBoundingClientRect();
        // 检测是否重叠
        if (btnRect.right > x - 20 && btnRect.left < x + ballSize + 20 &&
            btnRect.bottom > y - 20 && btnRect.top < y + ballSize + 20) {
          // 向上偏移 20px
          y -= 80; // 假设回到顶部按钮高度约 60px，多偏移一些
        }
      }

      this.position = { x, y };
      this.state = 'idle';
    },

    /**
     * 读取主题色
     * 计划书 2.1：动态读取 --td-brand-color
     */
    readThemeColor() {
      try {
        const rootStyle = getComputedStyle(document.documentElement);
        const brandColor = rootStyle.getPropertyValue('--td-brand-color').trim();

        if (brandColor) {
          this.brandColor = brandColor;
          // 生成浅色变体（模拟 lighten 20%）
          this.brandColorLight = this.lightenColor(brandColor, 20);
          // 生成深色模式变体
          this.brandColorDark = this.lightenColor(brandColor, 40);
          this.brandColorDarkLight = this.lightenColor(brandColor, 55);
        }
      } catch (e) {
        // 忽略错误
      }
    },

    /**
     * 颜色变淡工具函数
     */
    lightenColor(color, percent) {
      const num = parseInt(color.replace('#', ''), 16);
      const amt = Math.round(2.55 * percent);
      const R = (num >> 16) + amt;
      const G = (num >> 8 & 0x00FF) + amt;
      const B = (num & 0x0000FF) + amt;
      return '#' + (
        0x1000000 +
        (R < 255 ? (R < 1 ? 0 : R) : 255) * 0x10000 +
        (G < 255 ? (G < 1 ? 0 : G) : 255) * 0x100 +
        (B < 255 ? (B < 1 ? 0 : B) : 255)
      ).toString(16).slice(1);
    },

    /**
     * 检测深色模式
     * 计划书 2.1：检测系统深色模式
     */
    detectDarkMode() {
      const themeMode = document.documentElement.getAttribute('theme-mode');
      this.isDarkMode = themeMode === 'dark';
    },

    /**
     * 监听主题变化
     */
    observeThemeChanges() {
      this.themeObserver = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
          if (mutation.type === 'attributes' && mutation.attributeName === 'theme-mode') {
            this.detectDarkMode();
          }
        });
      });

      this.themeObserver.observe(document.documentElement, {
        attributes: true,
        attributeFilter: ['theme-mode']
      });
    },

    /**
     * 启动空闲检测
     * 计划书 2.3：超过 30 秒未操作，透明度降至 0.6
     */
    startIdleDetection() {
      const resetIdleTimer = () => {
        this.isIdleTimeout = false;
        if (this.idleTimer) {
          clearTimeout(this.idleTimer);
        }
        this.idleTimer = setTimeout(() => {
          if (!this.isDragging && !this.drawerVisible) {
            this.isIdleTimeout = true;
          }
        }, 30000); // 30 秒
      };

      // 监听鼠标移动
      document.addEventListener('mousemove', resetIdleTimer);
      document.addEventListener('click', resetIdleTimer);
      document.addEventListener('keydown', resetIdleTimer);

      // 保存引用以便清理
      this.resetIdleTimer = resetIdleTimer;
    },

    /**
     * 绑定拖拽事件
     * 计划书 3.1：原生 JS 实现 mousedown/mousemove/mouseup
     */
    bindDragEvents() {
      this.handleMouseMove = this.handleMouseMove.bind(this);
      this.handleMouseUp = this.handleMouseUp.bind(this);

      document.addEventListener('mousemove', this.handleMouseMove);
      document.addEventListener('mouseup', this.handleMouseUp);
    },

    /**
     * 解绑拖拽事件
     */
    unbindDragEvents() {
      document.removeEventListener('mousemove', this.handleMouseMove);
      document.removeEventListener('mouseup', this.handleMouseUp);
    },

    /**
     * 鼠标按下
     */
    handleMouseDown(event) {
      // 左键点击
      if (event.button !== 0) return;

      this.isDragging = true;
      this.isClick = true; // 假设是点击，如果移动超过 5px 则视为拖拽

      this.dragStart = {
        x: event.clientX,
        y: event.clientY
      };

      this.offset = {
        x: event.clientX - this.position.x,
        y: event.clientY - this.position.y
      };

      this.state = 'dragging';

      // 重置空闲检测
      if (this.resetIdleTimer) {
        this.resetIdleTimer();
      }
    },

    /**
     * 鼠标移动
     * 计划书 3.1：拖拽时球体跟随鼠标无延迟
     */
    handleMouseMove(event) {
      if (!this.isDragging) return;

      const newX = event.clientX - this.offset.x;
      const newY = event.clientY - this.offset.y;

      // 防误触：位移 > 5px 视为拖拽（计划书 3.1）
      const moveDistance = Math.sqrt(
        Math.pow(event.clientX - this.dragStart.x, 2) +
        Math.pow(event.clientY - this.dragStart.y, 2)
      );

      if (moveDistance > 5) {
        this.isClick = false;
      }

      // 限制在窗口内
      const windowWidth = window.innerWidth;
      const windowHeight = window.innerHeight;
      const ballSize = 56;

      this.position = {
        x: Math.max(0, Math.min(newX, windowWidth - ballSize)),
        y: Math.max(0, Math.min(newY, windowHeight - ballSize))
      };
    },

    /**
     * 鼠标松开
     * 计划书 3.1：自动计算最近边缘，平滑吸附（保留 24px 安全距）
     */
    handleMouseUp() {
      if (!this.isDragging) return;

      this.isDragging = false;

      // 吸附到最近边缘
      this.snapToEdge();

      // 保存位置
      this.savePosition();
    },

    /**
     * 吸附到边缘
     * 计划书 3.1：保留 24px 安全距
     * 计划书 2.2：使用 Cubic Bezier 曲线模拟惯性阻尼
     */
    snapToEdge() {
      this.state = 'snapping';

      const windowWidth = window.innerWidth;
      const windowHeight = window.innerHeight;
      const ballSize = 56;
      const safeMargin = 24;

      // 计算到各边的距离
      const distToLeft = this.position.x;
      const distToRight = windowWidth - this.position.x - ballSize;
      const distToTop = this.position.y;
      const distToBottom = windowHeight - this.position.y - ballSize;

      // 找到最近的垂直边缘
      if (distToLeft < distToRight) {
        this.position.x = safeMargin;
      } else {
        this.position.x = windowWidth - ballSize - safeMargin;
      }

      // 找到最近的水平边缘
      if (distToTop < distToBottom) {
        this.position.y = safeMargin;
      } else {
        this.position.y = windowHeight - ballSize - safeMargin;
      }

      // 延迟恢复 idle 状态，等待吸附动画完成
      setTimeout(() => {
        this.state = 'idle';
      }, 400); // 与 CSS transition 时间一致
    },

    /**
     * 点击事件
     * 计划书 3.1：防误触，位移 > 5px 视为拖拽，不触发点击
     */
    handleClick() {
      if (this.isClick) {
        this.$emit('toggle-drawer');
      }
    },

    /**
     * 设置警报状态
     * 计划书 2.3：当 AI 返回"报警/超标"信息时触发
     */
    setAlertState(isAlert) {
      this.isAlert = isAlert;
    },

    /**
     * 保存位置到 localStorage
     */
    savePosition() {
      try {
        localStorage.setItem('ai_assistant_ball_position', JSON.stringify(this.position));
      } catch (e) {
        // 忽略错误
      }
    },

    /**
     * 从 localStorage 恢复位置
     */
    restorePosition() {
      try {
        const saved = localStorage.getItem('ai_assistant_ball_position');
        if (saved) {
          const parsed = JSON.parse(saved);
          // 验证位置是否在窗口内
          const windowWidth = window.innerWidth;
          const windowHeight = window.innerHeight;
          const ballSize = 56;

          if (parsed.x >= 0 && parsed.x <= windowWidth - ballSize &&
              parsed.y >= 0 && parsed.y <= windowHeight - ballSize) {
            this.position = parsed;
          }
        }
      } catch (e) {
        // 忽略错误
      }
    }
  },

  watch: {
    drawerVisible(newVal) {
      // 抽屉打开时停止呼吸动画
      if (newVal) {
        this.state = 'snapped';
        this.isIdleTimeout = false;
      } else {
        this.state = 'idle';
      }
    }
  }
};
</script>

<style scoped>
/* 样式已通过 index.js 全局导入，这里不需要重复导入 */
</style>
