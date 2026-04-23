import { cookies } from 'next/headers'
import Link from 'next/link'
import { serverApi } from '@/lib/api/server'
import type { ApiResponse, PageResponse, SnackPurchase } from '@/lib/types'
import SnackCard from '@/components/snack/SnackCard'

interface PageProps {
  searchParams: Promise<{ page?: string; status?: string }>
}

const STATUSES = [
  { value: '', label: '전체' },
  { value: 'IN_STOCK', label: '재고 있음' },
  { value: 'DELIVERING', label: '배송 중' },
  { value: 'OUT_OF_STOCK', label: '재고 없음' },
]

export default async function HomePage({ searchParams }: PageProps) {
  const { page: pageStr, status } = await searchParams
  const page = parseInt(pageStr ?? '0', 10) || 0

  const params = new URLSearchParams({ page: String(page), size: '5', sort: 'createdAt,desc' })
  if (status) params.set('status', status)

  const res = await serverApi.get<ApiResponse<PageResponse<SnackPurchase>>>(
    `/api/snack-purchases?${params}`,
  )
  const data = res.data.data

  const store = await cookies()
  const raw = store.get('snack_user')?.value
  const user = raw ? JSON.parse(raw) : null
  const isAuthenticated = !!store.get('snack_access')?.value
  const isAdmin = user?.role === 'ADMIN'

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-zinc-900">구매 과자 목록</h1>
        {isAdmin && (
          <Link
            href="/admin"
            className="rounded-md bg-orange-500 px-4 py-2 text-sm font-medium text-white hover:bg-orange-600"
          >
            관리자 패널
          </Link>
        )}
      </div>

      <div className="flex flex-wrap gap-2">
        {STATUSES.map(({ value, label }) => (
          <Link
            key={value}
            href={value ? `/?status=${value}` : '/'}
            className={`rounded-full px-3 py-1 text-sm font-medium transition-colors ${
              (status ?? '') === value
                ? 'bg-zinc-900 text-white'
                : 'border border-zinc-300 bg-white text-zinc-700 hover:bg-zinc-50'
            }`}
          >
            {label}
          </Link>
        ))}
      </div>

      {data && data.content.length > 0 ? (
        <div className="flex flex-col gap-3">
          {data.content.map((snack) => (
            <SnackCard
              key={snack.id}
              snack={snack}
              isAdmin={isAdmin}
              showStatusAction={isAuthenticated}
            />
          ))}
        </div>
      ) : (
        <div className="rounded-xl border border-dashed border-zinc-300 py-16 text-center text-zinc-500">
          등록된 과자가 없습니다.
        </div>
      )}

      {data && data.totalPages > 1 && (
        <div className="flex justify-center gap-2">
          {Array.from({ length: data.totalPages }, (_, i) => (
            <Link
              key={i}
              href={`/?page=${i}${status ? `&status=${status}` : ''}`}
              className={`flex h-8 w-8 items-center justify-center rounded-md text-sm font-medium ${
                data.number === i
                  ? 'bg-zinc-900 text-white'
                  : 'border border-zinc-300 text-zinc-700 hover:bg-zinc-50'
              }`}
            >
              {i + 1}
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
