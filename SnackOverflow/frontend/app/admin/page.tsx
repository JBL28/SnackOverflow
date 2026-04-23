import { redirect } from 'next/navigation'
import { cookies } from 'next/headers'
import { getAccessToken } from '@/lib/auth-cookie'
import { serverApi } from '@/lib/api/server'
import type { ApiResponse, PageResponse, SnackPurchase } from '@/lib/types'
import CreateSnackForm from '@/components/admin/CreateSnackForm'
import SnackCard from '@/components/snack/SnackCard'

export default async function AdminPage() {
  const token = await getAccessToken()
  if (!token) redirect('/login')

  const store = await cookies()
  const raw = store.get('snack_user')?.value
  const user = raw ? JSON.parse(raw) : null
  if (user?.role !== 'ADMIN') redirect('/')

  const res = await serverApi.get<ApiResponse<PageResponse<SnackPurchase>>>(
    '/api/snack-purchases?size=20&sort=createdAt,desc',
    { headers: { Authorization: `Bearer ${token}` } },
  )
  const data = res.data.data

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold text-zinc-900">관리자 패널</h1>

      <section>
        <h2 className="mb-3 text-base font-semibold text-zinc-800">과자 등록</h2>
        <CreateSnackForm />
      </section>

      <section>
        <h2 className="mb-3 text-base font-semibold text-zinc-800">과자 관리</h2>
        {data && data.content.length > 0 ? (
          <div className="flex flex-col gap-3">
            {data.content.map((snack) => (
              <SnackCard key={snack.id} snack={snack} isAdmin showStatusAction />
            ))}
          </div>
        ) : (
          <p className="text-zinc-500">등록된 과자가 없습니다.</p>
        )}
      </section>
    </div>
  )
}
