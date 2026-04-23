'use server'
import { redirect } from 'next/navigation'
import { serverApi } from '@/lib/api/server'
import { setTokens, clearTokens, getRefreshCookieHeader } from '@/lib/auth-cookie'
import type { ApiResponse, UserProfile } from '@/lib/types'

export async function loginAction(username: string, password: string): Promise<{ error?: string }> {
  try {
    const res = await serverApi.post<ApiResponse<{ accessToken: string }>>('/api/auth/login', {
      username,
      password,
    })
    const accessToken = res.data.data?.accessToken
    if (!accessToken) return { error: '로그인에 실패했습니다.' }

    const profileRes = await serverApi.get<ApiResponse<UserProfile>>('/api/users/me', {
      headers: { Authorization: `Bearer ${accessToken}` },
    })
    const profile = profileRes.data.data
    if (!profile) return { error: '사용자 정보를 불러올 수 없습니다.' }

    await setTokens(accessToken, {
      username: profile.username,
      nickname: profile.nickname,
      role: profile.role,
    })
  } catch {
    return { error: '아이디 또는 비밀번호가 올바르지 않습니다.' }
  }
  redirect('/')
}

export async function signupAction(
  username: string,
  email: string,
  password: string,
  nickname: string,
): Promise<{ error?: string }> {
  try {
    await serverApi.post('/api/auth/signup', { username, email, password, nickname })
  } catch {
    return { error: '회원가입에 실패했습니다. 이미 사용 중인 아이디 또는 이메일일 수 있습니다.' }
  }
  redirect('/login')
}

export async function logoutAction(): Promise<void> {
  try {
    const refreshHeader = await getRefreshCookieHeader()
    await serverApi.post('/api/auth/logout', null, {
      headers: { Cookie: refreshHeader ?? '' },
    })
  } catch {
    // 로그아웃 실패해도 쿠키는 삭제
  }
  await clearTokens()
  redirect('/login')
}
