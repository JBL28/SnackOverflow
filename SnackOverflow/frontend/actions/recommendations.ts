'use server'
import { revalidatePath } from 'next/cache'
import { serverApi } from '@/lib/api/server'
import { getAccessToken } from '@/lib/auth-cookie'
import type { ApiResponse, PageResponse, SnackRecommendation } from '@/lib/types'

async function authHeaders() {
  const token = await getAccessToken()
  return { Authorization: `Bearer ${token ?? ''}` }
}

export async function getRecommendationsAction(
  page = 0,
): Promise<{ data?: PageResponse<SnackRecommendation>; error?: string }> {
  try {
    const res = await serverApi.get<ApiResponse<PageResponse<SnackRecommendation>>>(
      `/api/snack-recommendations?page=${page}&size=5&sort=createdAt,desc`,
    )
    return { data: res.data.data ?? undefined }
  } catch {
    return { error: '추천 목록을 불러오지 못했습니다.' }
  }
}

export async function createRecommendationAction(
  name: string,
  reason: string,
): Promise<{ data?: SnackRecommendation; error?: string }> {
  try {
    const res = await serverApi.post<ApiResponse<SnackRecommendation>>(
      '/api/snack-recommendations',
      { name, reason },
      { headers: await authHeaders() },
    )
    revalidatePath('/recommendations')
    return { data: res.data.data ?? undefined }
  } catch {
    return { error: '추천 게시글 작성에 실패했습니다.' }
  }
}

export async function updateRecommendationAction(
  id: string,
  name: string,
  reason: string,
): Promise<{ data?: SnackRecommendation; error?: string }> {
  try {
    const res = await serverApi.put<ApiResponse<SnackRecommendation>>(
      `/api/snack-recommendations/${id}`,
      { name, reason },
      { headers: await authHeaders() },
    )
    revalidatePath('/recommendations')
    revalidatePath(`/recommendations/${id}`)
    return { data: res.data.data ?? undefined }
  } catch {
    return { error: '수정에 실패했습니다.' }
  }
}

export async function deleteRecommendationAction(id: string): Promise<{ error?: string }> {
  try {
    await serverApi.delete(`/api/snack-recommendations/${id}`, {
      headers: await authHeaders(),
    })
    revalidatePath('/recommendations')
    return {}
  } catch {
    return { error: '삭제에 실패했습니다.' }
  }
}
