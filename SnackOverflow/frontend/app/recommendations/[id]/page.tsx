import { redirect } from 'next/navigation'
import { cookies } from 'next/headers'
import { serverApi } from '@/lib/api/server'
import { getCommentsAction } from '@/actions/comments'
import type { ApiResponse, SnackRecommendation } from '@/lib/types'
import RecommendationActions from '@/components/recommendation/RecommendationActions'
import CommentSection from '@/components/comment/CommentSection'
import ReactionButtons from '@/components/reaction/ReactionButtons'

export default async function RecommendationDetailPage({
  params,
}: {
  params: Promise<{ id: string }>
}) {
  const { id } = await params

  const res = await serverApi.get<ApiResponse<SnackRecommendation>>(
    `/api/snack-recommendations/${id}`,
  )
  const rec = res.data.data
  if (!rec) redirect('/recommendations')

  const store = await cookies()
  const raw = store.get('snack_user')?.value
  const me = raw ? JSON.parse(raw) : null
  const isAuthenticated = !!store.get('snack_access')?.value
  const canEdit = me && (me.role === 'ADMIN' || me.id === rec.createdById)

  const comments = await getCommentsAction('RECOMMENDATION', id)

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-zinc-900">{rec.name}</h1>
          <p className="mt-1 text-sm text-zinc-400">
            {rec.createdByNickname} · {new Date(rec.createdAt).toLocaleDateString('ko-KR')}
          </p>
        </div>
        <ReactionButtons
          targetType="RECOMMENDATION"
          targetId={rec.id}
          initialLikes={rec.likes}
          initialDislikes={rec.dislikes}
          initialMyReaction={null}
          isAuthenticated={isAuthenticated}
        />
      </div>

      <p className="whitespace-pre-wrap leading-relaxed text-zinc-700">{rec.reason}</p>

      {canEdit && (
        <RecommendationActions
          id={rec.id}
          initialName={rec.name}
          initialReason={rec.reason}
        />
      )}

      <CommentSection
        initialComments={comments}
        targetType="RECOMMENDATION"
        targetId={id}
        currentUserId={me?.id ?? null}
        isAdmin={me?.role === 'ADMIN'}
      />
    </div>
  )
}