export function classifyApiError(error) {
  const status = error?.response?.status
  const serverMessage = error?.response?.data?.message

  if (status === 401) {
    return { action: 'login', message: '登录状态已失效，请重新登录' }
  }
  if (status === 403) {
    return { action: 'forbidden', message: '当前账号没有执行此操作的权限' }
  }
  if (status === 429) {
    return { action: 'notify', message: '操作过于频繁，请稍后重试' }
  }
  if (!error?.response) {
    return { action: 'notify', message: '网络连接失败，请检查网络后重试' }
  }
  return { action: 'notify', message: serverMessage || '请求失败，请稍后重试' }
}
