<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'

const canvasRef = ref<HTMLCanvasElement | null>(null)
let frame = 0
let raf = 0

function draw(time: number) {
  const canvas = canvasRef.value
  if (!canvas) return
  const parent = canvas.parentElement
  if (!parent) return
  const rect = parent.getBoundingClientRect()
  const dpr = Math.min(window.devicePixelRatio || 1, 2)
  const width = Math.max(1, Math.floor(rect.width * dpr))
  const height = Math.max(1, Math.floor(rect.height * dpr))
  if (canvas.width !== width || canvas.height !== height) {
    canvas.width = width
    canvas.height = height
  }
  canvas.style.width = `${rect.width}px`
  canvas.style.height = `${rect.height}px`

  const ctx = canvas.getContext('2d')
  if (!ctx) return
  ctx.clearRect(0, 0, width, height)
  ctx.save()
  ctx.scale(dpr, dpr)

  const w = rect.width
  const h = rect.height
  const cx = w * 0.68
  const cy = h * 0.48
  const radius = Math.min(w, h) * 0.28
  const pulse = Math.sin(time / 1300) * 0.04 + 1
  const colors = ['#247b75', '#4556a5', '#d96045', '#b8892f', '#1d625d']
  const labels = ['开放', '计划', '社交', '协调', '稳定']

  const gradient = ctx.createLinearGradient(0, 0, w, h)
  gradient.addColorStop(0, '#f8fbf8')
  gradient.addColorStop(0.52, '#edf4f1')
  gradient.addColorStop(1, '#eef0fb')
  ctx.fillStyle = gradient
  ctx.fillRect(0, 0, w, h)

  ctx.strokeStyle = 'rgba(69, 86, 165, 0.08)'
  ctx.lineWidth = 1
  for (let i = -8; i < 16; i += 1) {
    const offset = i * 90 + Math.sin(time / 2200 + i) * 8
    ctx.beginPath()
    ctx.moveTo(offset, h)
    ctx.lineTo(offset + h * 0.72, 0)
    ctx.stroke()
  }

  ctx.strokeStyle = 'rgba(36, 48, 54, 0.08)'
  ctx.lineWidth = 1
  for (let ring = 1; ring <= 5; ring += 1) {
    ctx.beginPath()
    for (let i = 0; i <= 5; i += 1) {
      const angle = -Math.PI / 2 + (i / 5) * Math.PI * 2
      const r = (radius * ring * pulse) / 5
      const x = cx + Math.cos(angle) * r
      const y = cy + Math.sin(angle) * r
      if (i === 0) ctx.moveTo(x, y)
      else ctx.lineTo(x, y)
    }
    ctx.stroke()
  }

  const points = [0.84, 0.68, 0.76, 0.72, 0.64].map((value, i) => {
    const angle = -Math.PI / 2 + (i / 5) * Math.PI * 2
    return {
      x: cx + Math.cos(angle) * radius * value * pulse,
      y: cy + Math.sin(angle) * radius * value * pulse,
      angle,
      color: colors[i],
      label: labels[i]
    }
  })

  ctx.beginPath()
  points.forEach((point, index) => {
    if (index === 0) ctx.moveTo(point.x, point.y)
    else ctx.lineTo(point.x, point.y)
  })
  ctx.closePath()
  ctx.fillStyle = 'rgba(36, 123, 117, 0.18)'
  ctx.strokeStyle = '#247b75'
  ctx.lineWidth = 3
  ctx.fill()
  ctx.stroke()

  points.forEach((point, index) => {
    ctx.fillStyle = point.color
    ctx.beginPath()
    ctx.arc(point.x, point.y, 6, 0, Math.PI * 2)
    ctx.fill()
    ctx.fillStyle = '#243036'
    ctx.font = '700 13px Microsoft YaHei, Arial'
    ctx.fillText(point.label, cx + Math.cos(point.angle) * (radius + 28), cy + Math.sin(point.angle) * (radius + 28))

    const next = points[(index + 2) % points.length]
    ctx.strokeStyle = 'rgba(217, 96, 69, 0.16)'
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.moveTo(point.x, point.y)
    ctx.lineTo(next.x, next.y)
    ctx.stroke()
  })

  ctx.restore()
  raf = requestAnimationFrame(draw)
}

onMounted(() => {
  raf = requestAnimationFrame(draw)
})

onUnmounted(() => {
  cancelAnimationFrame(raf)
})
</script>

<template>
  <canvas ref="canvasRef" class="hero-radar-canvas" aria-hidden="true"></canvas>
</template>
