import { classifyApiError } from '@/api/errorPolicy'

describe('classifyApiError', () => {
  test('requests login after a 401 response', () => {
    expect(classifyApiError({ response: { status: 401 } })).toEqual({
      action: 'login',
      message: '登录状态已失效，请重新登录'
    })
  })

  test('keeps forbidden and rate-limit outcomes distinct', () => {
    expect(classifyApiError({ response: { status: 403 } })).toEqual({
      action: 'forbidden',
      message: '当前账号没有执行此操作的权限'
    })
    expect(classifyApiError({ response: { status: 429 } })).toEqual({
      action: 'notify',
      message: '操作过于频繁，请稍后重试'
    })
  })

  test('uses server messages for other failures when available', () => {
    expect(classifyApiError({ response: { status: 422, data: { message: '内容格式有误' } } })).toEqual({
      action: 'notify',
      message: '内容格式有误'
    })
    expect(classifyApiError({ message: 'Network Error' })).toEqual({
      action: 'notify',
      message: '网络连接失败，请检查网络后重试'
    })
  })
})
