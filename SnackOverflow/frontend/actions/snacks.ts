'use server'
import { revalidatePath } from 'next/cache'
import { serverApi } from '@/lib/api/server'
import { getAccessToken } from '@/lib/auth-cookie'

async function authHeaders() {
  const token = await getAccessToken()
  return { Authorization: `Bearer ${token ?? ''}` }
}

export async function createSnackAction(name: string): Promise<{ error?: string }> {
  try {
    await serverApi.post('/api/snack-purchases', { name }, { headers: await authHeaders() })
    revalidatePath('/')
    revalidatePath('/admin')
    return {}
  } catch {
    return { error: '과자 등록에 실패했습니다.' }
  }
}

export async function updateSnackNameAction(id: string, name: string): Promise<{ error?: string }> {
  try {
    await serverApi.patch(`/api/snack-purchases/${id}`, { name }, { headers: await authHeaders() })
    revalidatePath('/')
    revalidatePath('/admin')
    return {}
  } catch {
    return { error: '이름 수정에 실패했습니다.' }
  }
}

export async function updateSnackStatusAction(id: string, status: string): Promise<{ error?: string }> {
  try {
    await serverApi.patch(
      `/api/snack-purchases/${id}/status`,
      { status },
      { headers: await authHeaders() },
    )
    revalidatePath('/')
    revalidatePath(`/snacks/${id}`)
    return {}
  } catch {
    return { error: '상태 변경에 실패했습니다.' }
  }
}

export async function deleteSnackAction(id: string): Promise<{ error?: string }> {
  try {
    await serverApi.delete(`/api/snack-purchases/${id}`, { headers: await authHeaders() })
    revalidatePath('/')
    revalidatePath('/admin')
    return {}
  } catch {
    return { error: '과자 삭제에 실패했습니다.' }
  }
}
