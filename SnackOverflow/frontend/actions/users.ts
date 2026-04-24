'use server'
import { redirect } from 'next/navigation'
import { serverApi } from '@/lib/api/server'
import { getAccessToken, clearTokens } from '@/lib/auth-cookie'
import type { ApiResponse, AdminUser, PageResponse, UserStatus, SnackRecommendation, Comment } from '@/lib/types'

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

export async function getMyPostsAction(): Promise<SnackRecommendation[]> {
  try {
    const res = await serverApi.get<ApiResponse<SnackRecommendation[]>>('/api/users/me/posts', {
      headers: await authHeaders(),
    })
    return res.data.data ?? []
  } catch {
    return []
  }
}

export async function getMyCommentsAction(): Promise<Comment[]> {
  try {
    const res = await serverApi.get<ApiResponse<Comment[]>>('/api/users/me/comments', {
      headers: await authHeaders(),
    })
    return res.data.data ?? []
  } catch {
    return []
  }
}

export async function adminGetUsersAction(
  page = 0,
): Promise<{ data?: PageResponse<AdminUser>; error?: string }> {
  try {
    const res = await serverApi.get<ApiResponse<PageResponse<AdminUser>>>(
      `/api/users/admin?page=${page}&size=10`,
      { headers: await authHeaders() },
    )
    return { data: res.data.data ?? undefined }
  } catch {
    return { error: '유저 목록을 불러오지 못했습니다.' }
  }
}

export async function adminChangeUserStatusAction(
  userId: string,
  status: UserStatus,
): Promise<{ error?: string }> {
  try {
    await serverApi.patch(
      `/api/users/admin/${userId}/status`,
      { status },
      { headers: await authHeaders() },
    )
    return {}
  } catch {
    return { error: '상태 변경에 실패했습니다.' }
  }
}

export async function adminResetPasswordAction(
  userId: string,
  newPassword: string,
): Promise<{ error?: string }> {
  try {
    await serverApi.post(
      `/api/users/admin/${userId}/reset-password`,
      { newPassword },
      { headers: await authHeaders() },
    )
    return {}
  } catch {
    return { error: '비밀번호 초기화에 실패했습니다.' }
  }
}