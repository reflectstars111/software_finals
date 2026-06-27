<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'

function hexToRgba(hex: string, alpha: number): string {
  const clean = hex.replace('#', '')
  const full = clean.length === 3 ? clean.split('').map((c) => c + c).join('') : clean
  const r = parseInt(full.slice(0, 2), 16)
  const g = parseInt(full.slice(2, 4), 16)
  const b = parseInt(full.slice(4, 6), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

const canvasRef = ref<HTMLCanvasElement | null>(null)
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
  const cy = h * 0.45
  const radius = Math.min(w, h) * 0.26
  const t = time / 1000
  const pulse = Math.sin(time / 1800) * 0.03 + 1

  // Deep field gradient
  const bg = ctx.createRadialGradient(cx, cy, radius * 0.2, cx, cy, radius * 2.5)
  bg.addColorStop(0, '#1a2740')
  bg.addColorStop(0.5, '#131b2e')
  bg.addColorStop(1, '#0d1522')
  ctx.fillStyle = bg
  ctx.fillRect(0, 0, w, h)

  // Subtle grid
  ctx.strokeStyle = 'rgba(108, 204, 160, 0.04)'
  ctx.lineWidth = 0.5
  const gridSize = 52
  for (let x = -gridSize + ((t * 8) % gridSize); x < w + gridSize; x += gridSize) {
    ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x + h * 0.45, h); ctx.stroke()
  }
  for (let y = 0; y < h; y += gridSize) {
    ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(w, y); ctx.stroke()
  }

  // Concentric radar rings
  for (let ring = 1; ring <= 5; ring++) {
    const alpha = 0.06 + ring * 0.015
    ctx.strokeStyle = `rgba(108, 204, 160, ${alpha})`
    ctx.lineWidth = ring === 3 ? 1.2 : 0.6
    ctx.beginPath()
    for (let i = 0; i <= 60; i++) {
      const angle = (i / 60) * Math.PI * 2
      const r = (radius * ring * pulse) / 5
      const x = cx + Math.cos(angle) * r
      const y = cy + Math.sin(angle) * r
      i === 0 ? ctx.moveTo(x, y) : ctx.lineTo(x, y)
    }
    ctx.stroke()
  }

  // Sweeping beam (rotating wedge)
  const sweepAngle = (t * 0.35) % (Math.PI * 2)
  ctx.save()
  ctx.globalAlpha = 0.07
  ctx.fillStyle = '#6ccca0'
  ctx.beginPath()
  ctx.moveTo(cx, cy)
  ctx.arc(cx, cy, radius * 1.15, sweepAngle, sweepAngle + 0.6)
  ctx.closePath()
  ctx.fill()
  ctx.restore()

  // Axis lines
  const axes = 5
  for (let i = 0; i < axes; i++) {
    const angle = -Math.PI / 2 + (i / axes) * Math.PI * 2
    ctx.strokeStyle = 'rgba(120, 135, 170, 0.15)'
    ctx.lineWidth = 0.5
    ctx.beginPath()
    ctx.moveTo(cx, cy)
    ctx.lineTo(cx + Math.cos(angle) * radius * 1.1, cy + Math.sin(angle) * radius * 1.1)
    ctx.stroke()
  }

  // Dimension blips
  const blipColors = ['#6ccca0', '#e8b44f', '#e8786a', '#a0c4f0', '#c0a0e0']
  const labels = ['开放', '尽责', '外向', '宜人', '稳定']
  const blipValues = [0.82, 0.64, 0.76, 0.70, 0.60]
  const points = blipValues.map((value, i) => {
    const angle = -Math.PI / 2 + (i / axes) * Math.PI * 2
    return {
      x: cx + Math.cos(angle) * radius * value * pulse,
      y: cy + Math.sin(angle) * radius * value * pulse,
      angle, color: blipColors[i], label: labels[i]
    }
  })

  // Filled polygon
  ctx.beginPath()
  points.forEach((p, i) => i === 0 ? ctx.moveTo(p.x, p.y) : ctx.lineTo(p.x, p.y))
  ctx.closePath()
  ctx.fillStyle = 'rgba(108, 204, 160, 0.10)'
  ctx.strokeStyle = 'rgba(108, 204, 160, 0.45)'
  ctx.lineWidth = 2.2
  ctx.fill()
  ctx.stroke()

  // Blip dots + labels
  points.forEach((p) => {
    // Outer glow
    ctx.fillStyle = hexToRgba(p.color, 0.25)
    ctx.beginPath(); ctx.arc(p.x, p.y, 8, 0, Math.PI * 2); ctx.fill()
    // Core dot
    ctx.fillStyle = p.color
    ctx.beginPath(); ctx.arc(p.x, p.y, 4.5, 0, Math.PI * 2); ctx.fill()
    // Label
    ctx.fillStyle = '#c8d0e0'
    ctx.font = '600 12px "PingFang SC", "Microsoft YaHei", sans-serif'
    ctx.textAlign = 'center'
    ctx.fillText(p.label, cx + Math.cos(p.angle) * (radius * 1.25), cy + Math.sin(p.angle) * (radius * 1.25) + 4)
  })

  ctx.restore()
  raf = requestAnimationFrame(draw)
}

onMounted(() => { raf = requestAnimationFrame(draw) })
onUnmounted(() => { cancelAnimationFrame(raf) })
</script>

<template>
  <canvas ref="canvasRef" class="hero-radar-canvas" aria-hidden="true"></canvas>
</template>
