'use server'
import { redirect } from 'next/navigation'
import { serverApi } from '@/lib/api/server'
import { getAccessToken, clearTokens } from '@/lib/auth-cookie'

async function authHeaders() {
  const token = await getAccessToken()
  return { Authorization: `Bearer ${token ?? ''}` }
}

export async function updateNicknameAction(nickname: string): Promise<{ error?: string }> {
  try {
    await serverApi.patch('/api/users/me/nickname', { nickname }, { headers: await authHeaders() })
    return {}
  } catch {
    return { error: '닉네임 변경에 실패했습니다.' }
  }
}

export async function changePasswordAction(
  currentPassword: string,
  newPassword: string,
): Promise<{ error?: string }> {
  try {
    await serverApi.patch(
      '/api/users/me/password',
      { currentPassword, newPassword },
      { headers: await authHeaders() },
    )
    return {}
  } catch {
    return { error: '비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인하세요.' }
  }
}

export async function withdrawAction(): Promise<void> {
  try {
    await serverApi.delete('/api/users/me', { headers: await authHeaders() })
  } catch {
    // 실패해도 로컬 쿠키 삭제
  }
  await clearTokens()
  redirect('/login')
}
