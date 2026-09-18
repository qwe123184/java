<script setup>
import { onMounted, onBeforeUnmount, ref } from 'vue'

const cv = ref(null)
let ctx, w, h, raf, parts = []
const COLORS = ['#2ee6ff', '#b14dff', '#ff4d8d', '#f5d27a']

function resize() {
  w = cv.value.width = window.innerWidth
  h = cv.value.height = window.innerHeight
  const n = Math.min(120, Math.floor((w * h) / 16000))
  parts = Array.from({ length: n }, () => ({
    x: Math.random() * w,
    y: Math.random() * h,
    r: Math.random() * 1.8 + 0.4,
    vx: (Math.random() - 0.5) * 0.25,
    vy: (Math.random() - 0.5) * 0.25 - 0.08,
    a: Math.random() * 0.5 + 0.15,
    c: COLORS[(Math.random() * COLORS.length) | 0]
  }))
}

let t = 0
function loop() {
  t += 0.005
  ctx.clearRect(0, 0, w, h)

  // 漂浮迷雾团
  for (let i = 0; i < 3; i++) {
    const cx = w * (0.25 + 0.25 * i) + Math.sin(t + i) * 60
    const cy = h * (0.4 + 0.2 * Math.sin(t * 0.7 + i))
    const g = ctx.createRadialGradient(cx, cy, 0, cx, cy, 260)
    const col = i % 2 ? '177,77,255' : '46,230,255'
    g.addColorStop(0, `rgba(${col},0.10)`)
    g.addColorStop(1, 'rgba(0,0,0,0)')
    ctx.fillStyle = g
    ctx.fillRect(0, 0, w, h)
  }

  // 星尘
  for (const p of parts) {
    p.x += p.vx
    p.y += p.vy
    if (p.y < -5) p.y = h + 5
    if (p.y > h + 5) p.y = -5
    if (p.x < -5) p.x = w + 5
    if (p.x > w + 5) p.x = -5
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.r, 0, 7)
    ctx.fillStyle = p.c
    ctx.globalAlpha = p.a
    ctx.shadowBlur = 8
    ctx.shadowColor = p.c
    ctx.fill()
  }
  ctx.globalAlpha = 1
  ctx.shadowBlur = 0
  raf = requestAnimationFrame(loop)
}

onMounted(() => {
  ctx = cv.value.getContext('2d')
  resize()
  window.addEventListener('resize', resize)
  loop()
})

onBeforeUnmount(() => {
  cancelAnimationFrame(raf)
  window.removeEventListener('resize', resize)
})
</script>

<template>
  <canvas ref="cv" class="fog"></canvas>
</template>

<style scoped>
.fog {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}
</style>
