import { serverApi } from '@/lib/api/server'
import { getAccessToken } from '@/lib/auth-cookie'
import type { ApiResponse, PageResponse, SnackRecommendation } from '@/lib/types'
import RecommendationCard from '@/components/recommendation/RecommendationCard'
import CreateRecommendationForm from '@/components/recommendation/CreateRecommendationForm'

export default async function RecommendationsPage() {
  const res = await serverApi.get<ApiResponse<PageResponse<SnackRecommendation>>>(
    '/api/snack-recommendations?page=0&size=5&sort=createdAt,desc',
  )
  const data = res.data.data
  const token = await getAccessToken()

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold text-zinc-900">과자 추천</h1>

      {token && <CreateRecommendationForm />}

      <section className="flex flex-col gap-3">
        {data && data.content.length > 0 ? (
          data.content.map((rec) => <RecommendationCard key={rec.id} rec={rec} />)
        ) : (
          <p className="text-sm text-zinc-500">아직 추천된 과자가 없습니다. 첫 번째로 추천해보세요!</p>
        )}
      </section>

      {data && data.totalPages > 1 && (
        <p className="text-xs text-zinc-400 text-center">
          {data.number + 1} / {data.totalPages} 페이지
        </p>
      )}
    </div>
  )
}