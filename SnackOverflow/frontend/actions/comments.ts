'use server'
import { revalidatePath } from 'next/cache'
import { serverApi } from '@/lib/api/server'
import { getAccessToken } from '@/lib/auth-cookie'
import type { ApiResponse, Comment, TargetType } from '@/lib/types'

async function authHeaders() {
  const token = await getAccessToken()
  return { Authorization: `Bearer ${token ?? ''} ` }
}

export async function getCommentsAction(
  targetType: TargetType,
  targetId: string,
): Promise<Comment[]> {
  try {
    const res = await serverApi.get<ApiResponse<Comment[]>>(
      `/api/comments?targetType=${targetType}&targetId=${targetId}`,
    )
    return res.data.data ?? []
  } catch {
    return []
  }
}

export async function createCommentAction(
  content: string,
  targetType: TargetType,
  targetId: string,
  parentId: string | null,
): Promise<{ data?: Comment; error?: string }> {
  try {
    const res = await serverApi.post<ApiResponse<Comment>>(
      '/api/comments',
      { content, targetType, targetId, parentId },
      { headers: await authHeaders() },
    )
    return { data: res.data.data ?? undefined }
  } catch {
    return { error: '댓글 작성에 실패했습니다.' }
  }
}

export async function updateCommentAction(
  id: string,
  content: string,
): Promise<{ data?: Comment; error?: string }> {
  try {
    const res = await serverApi.put<ApiResponse<Comment>>(
      `/api/comments/${id}`,
      { content },
      { headers: await authHeaders() },
    )
    return { data: res.data.data ?? undefined }
  } catch {
    return { error: '댓글 수정에 실패했습니다.' }
  }
}

export async function deleteCommentAction(id: string): Promise<{ error?: string }> {
  try {
    await serverApi.delete(`/api/comments/${id}`, { headers: await authHeaders() })
    return {}
  } catch {
    return { error: '댓글 삭제에 실패했습니다.' }
  }
}