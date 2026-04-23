import { notFound } from 'next/navigation'
import { cookies } from 'next/headers'
import { serverApi } from '@/lib/api/server'
import type { ApiResponse, SnackPurchase } from '@/lib/types'
import StatusBadge from '@/components/snack/StatusBadge'
import StatusUpdateButton from '@/components/snack/StatusUpdateButton'
import AdminSnackActions from '@/components/admin/AdminSnackActions'

interface PageProps {
  params: Promise<{ id: string }>
}

export default async function SnackDetailPage({ params }: PageProps) {
  const { id } = await params

  let snack: SnackPurchase
  try {
    const res = await serverApi.get<ApiResponse<SnackPurchase>>(`/api/snack-purchases/${id}`)
    if (!res.data.data) return notFound()
    snack = res.data.data
  } catch {
    return notFound()
  }

  const store = await cookies()
  const raw = store.get('snack_user')?.value
  const user = raw ? JSON.parse(raw) : null
  const isAuthenticated = !!store.get('snack_access')?.value
  const isAdmin = user?.role === 'ADMIN'

  return (
    <div className="mx-auto max-w-xl">
      <div className="rounded-xl border border-zinc-200 bg-white p-6 shadow-sm">
        <div className="flex items-start justify-between gap-4">
          <h1 className="text-xl font-bold text-zinc-900">{snack.name}</h1>
          <StatusBadge status={snack.status} />
        </div>
        <div className="mt-4 flex gap-4 text-sm text-zinc-500">
          <span>👍 {snack.likes}</span>
          <span>👎 {snack.dislikes}</span>
          <span>· 등록자: {snack.createdByNickname}</span>
        </div>
        {isAuthenticated && (
          <div className="mt-4 flex flex-wrap gap-2 border-t border-zinc-100 pt-4">
            <StatusUpdateButton id={snack.id} status={snack.status} />
            {isAdmin && <AdminSnackActions id={snack.id} currentName={snack.name} />}
          </div>
        )}
      </div>
    </div>
  )
}
