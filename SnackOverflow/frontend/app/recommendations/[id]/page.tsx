import { redirect } from 'next/navigation'
import { cookies } from 'next/headers'
import { serverApi } from '@/lib/api/server'
import { getAccessToken } from '@/lib/auth-cookie'
import type { ApiResponse, SnackRecommendation } from '@/lib/types'
import RecommendationActions from '@/components/recommendation/RecommendationActions'

export default async function RecommendationDetailPage({
  params,
}: {
  params: { id: string }
}) {
  const res = await serverApi.get<ApiResponse<SnackRecommendation>>(
    `/api/snack-recommendations/${params.id}`,
  )
  const rec = res.data.data
  if (!rec) redirect('/recommendations')

  const store = await cookies()
  const raw = store.get('snack_user')?.value
  const me = raw ? JSON.parse(raw) : null
  const canEdit = me && (me.role === 'ADMIN' || me.id === rec.createdById)

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-zinc-900">{rec.name}</h1>
          <p className="mt-1 text-sm text-zinc-400">
            {rec.createdByNickname} · {new Date(rec.createdAt).toLocaleDateString('ko-KR')}
          </p>
        </div>
        <div className="flex gap-2 text-sm text-zinc-500">
          <span>👍 {rec.likes}</span>
          <span>👎 {rec.dislikes}</span>
        </div>
      </div>

      <p className="whitespace-pre-wrap text-zinc-700 leading-relaxed">{rec.reason}</p>

      {canEdit && (
        <RecommendationActions
          id={rec.id}
          initialName={rec.name}
          initialReason={rec.reason}
        />
      )}
    </div>
  )
}