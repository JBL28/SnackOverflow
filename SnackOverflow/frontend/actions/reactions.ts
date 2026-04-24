'use server'
import { serverApi } from '@/lib/api/server'
import { getAccessToken } from '@/lib/auth-cookie'
import type { ApiResponse, ReactionTargetType, ReactionType, ToggleReactionResponse } from '@/lib/types'

async function authHeaders() {
  const token = await getAccessToken()
  return { Authorization: `Bearer ${token ?? ''}` }
}

export async function toggleReactionAction(
  targetType: ReactionTargetType,
  targetId: string,
  type: ReactionType,
): Promise<{ data?: ToggleReactionResponse; error?: string }> {
  try {
    const res = await serverApi.post<ApiResponse<ToggleReactionResponse>>(
      '/api/reactions',
      { targetType, targetId, type },
      { headers: await authHeaders() },
    )
    return { data: res.data.data ?? undefined }
  } catch {
    return { error: '반응 처리에 실패했습니다.' }
  }
}

export async function getVotersAction(
  targetType: ReactionTargetType,
  targetId: string,
  type: ReactionType,
): Promise<string[]> {
  try {
    const res = await serverApi.get<ApiResponse<{ nicknames: string[] }>>(
      `/api/reactions/voters?targetType=${targetType}&targetId=${targetId}&type=${type}`,
    )
    return res.data.data?.nicknames ?? []
  } catch {
    return []
  }
}