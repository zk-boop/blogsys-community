// 日期格式化
export function formatDate(date, fmt = 'yyyy-MM-dd hh:mm:ss') {
  if (!date) return ''
  if (typeof date === 'string') {
    date = new Date(date)
  }

  const o = {
    'M+': date.getMonth() + 1, // 月份
    'd+': date.getDate(), // 日
    'h+': date.getHours(), // 小时
    'm+': date.getMinutes(), // 分
    's+': date.getSeconds(), // 秒
    'q+': Math.floor((date.getMonth() + 3) / 3), // 季度
    'S': date.getMilliseconds() // 毫秒
  }

  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length))
  }

  for (const k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(RegExp.$1, (RegExp.$1.length === 1) ? (o[k]) : (('00' + o[k]).substr(('' + o[k]).length)))
    }
  }

  return fmt
}

// 计算阅读时间
export function readingTime(content) {
  if (!content) return '< 1 分钟'
  const wordsPerMinute = 400
  const textLength = content.trim().split(/\s+/).length
  if (textLength <= 0) return '< 1 分钟'
  const time = Math.ceil(textLength / wordsPerMinute)
  return time + ' 分钟'
}

// 文本截断
export function truncateText(text, length = 100) {
  if (!text) return ''
  if (text.length <= length) return text
  return text.substring(0, length) + '...'
}

// 将HTML内容转换为纯文本
export function htmlToText(html) {
  if (!html) return ''
  const temp = document.createElement('div')
  temp.innerHTML = html
  return temp.textContent || temp.innerText || ''
}

// 获取随机颜色
export function getRandomColor() {
  const letters = 'A0B1C2D3E4F59687'
  let color = '#'
  for (let i = 0; i < 6; i++) {
    color += letters[Math.floor(Math.random() * 16)]
  }
  return color
}

// 防抖函数
export function debounce(fn, delay) {
  let timer = null
  return function() {
    const context = this
    const args = arguments
    clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(context, args)
    }, delay)
  }
}

// 节流函数
export function throttle(fn, interval) {
  let last = 0
  return function() {
    const context = this
    const args = arguments
    const now = Date.now()
    if (now - last >= interval) {
      fn.apply(context, args)
      last = now
    }
  }
}

// 获取图片的主色调
export function getDominantColor(imageUrl, callback) {
  const img = new Image()
  img.crossOrigin = 'Anonymous'
  img.src = imageUrl

  img.onload = () => {
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    canvas.width = img.width
    canvas.height = img.height

    ctx.drawImage(img, 0, 0, img.width, img.height)

    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
    const pixels = imageData.data
    const pixelCount = canvas.width * canvas.height

    const colorCount = {}

    for (let i = 0; i < pixelCount; i++) {
      const offset = i * 4
      const r = pixels[offset]
      const g = pixels[offset + 1]
      const b = pixels[offset + 2]

      const rgb = `rgb(${r},${g},${b})`

      if (colorCount[rgb]) {
        colorCount[rgb]++
      } else {
        colorCount[rgb] = 1
      }
    }

    let dominantColor = ''
    let maxCount = 0

    for (const color in colorCount) {
      if (colorCount[color] > maxCount) {
        maxCount = colorCount[color]
        dominantColor = color
      }
    }

    callback(dominantColor)
  }
}

// 将数字格式化为友好的字符串（如：1000 -> 1k）
export function formatNumber(num) {
  if (num === null || num === undefined) return '0'
  if (num < 1000) return num.toString()
  if (num < 1000000) return Math.floor(num / 1000) + 'k'
  return Math.floor(num / 1000000) + 'M'
}

// 根据两个日期计算相对时间
export function timeAgo(date) {
  if (!date) return ''
  if (typeof date === 'string') {
    date = new Date(date)
  }

  const now = new Date()
  const diff = now - date

  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(months / 12)

  if (years > 0) return years + ' 年前'
  if (months > 0) return months + ' 个月前'
  if (days > 0) return days + ' 天前'
  if (hours > 0) return hours + ' 小时前'
  if (minutes > 0) return minutes + ' 分钟前'
  return '刚刚'
}

// 根据背景色计算文字颜色（深色背景用白字，浅色背景用黑字）
export function getContrastTextColor(backgroundColor) {
  // 如果没有提供背景色，默认返回黑色
  if (!backgroundColor) return '#000000';

  // 如果背景色是以#开头的十六进制颜色
  let r, g, b;
  if (backgroundColor.startsWith('#')) {
    // 将十六进制颜色转换为RGB
    const hex = backgroundColor.substring(1);
    // 处理简写形式 #RGB
    if (hex.length === 3) {
      r = parseInt(hex.charAt(0) + hex.charAt(0), 16);
      g = parseInt(hex.charAt(1) + hex.charAt(1), 16);
      b = parseInt(hex.charAt(2) + hex.charAt(2), 16);
    }
    // 处理完整形式 #RRGGBB
    else if (hex.length === 6) {
      r = parseInt(hex.substring(0, 2), 16);
      g = parseInt(hex.substring(2, 4), 16);
      b = parseInt(hex.substring(4, 6), 16);
    } else {
      return '#000000'; // 无效的十六进制颜色，返回黑色
    }
  }
  // 如果背景色是rgb(r,g,b)或rgba(r,g,b,a)格式
  else if (backgroundColor.startsWith('rgb')) {
    const rgbValues = backgroundColor.match(/\d+/g);
    if (rgbValues && rgbValues.length >= 3) {
      r = parseInt(rgbValues[0]);
      g = parseInt(rgbValues[1]);
      b = parseInt(rgbValues[2]);
    } else {
      return '#000000'; // 无效的RGB颜色，返回黑色
    }
  } else {
    return '#000000'; // 不支持的颜色格式，返回黑色
  }

  // 计算亮度 (根据YIQ公式: https://24ways.org/2010/calculating-color-contrast/)
  const yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;

  // 亮度大于128返回黑色，否则返回白色
  return (yiq >= 128) ? '#000000' : '#ffffff';
}
