import { redirect } from 'next/navigation'
import { cookies } from 'next/headers'
import { getAccessToken } from '@/lib/auth-cookie'
import { serverApi } from '@/lib/api/server'
import type { ApiResponse, PageResponse, SnackPurchase, AdminUser } from '@/lib/types'
import CreateSnackForm from '@/components/admin/CreateSnackForm'
import SnackCard from '@/components/snack/SnackCard'
import UserManagement from '@/components/admin/UserManagement'

export default async function AdminPage() {
  const token = await getAccessToken()
  if (!token) redirect('/login')

  const store = await cookies()
  const raw = store.get('snack_user')?.value
  const user = raw ? JSON.parse(raw) : null
  if (user?.role !== 'ADMIN') redirect('/')

  const [snacksRes, usersRes] = await Promise.all([
    serverApi.get<ApiResponse<PageResponse<SnackPurchase>>>(
      '/api/snack-purchases?size=20&sort=createdAt,desc',
      { headers: { Authorization: `Bearer ${token}` } },
    ),
    serverApi.get<ApiResponse<PageResponse<AdminUser>>>(
      '/api/users/admin?page=0&size=10',
      { headers: { Authorization: `Bearer ${token}` } },
    ),
  ])

  const snackData = snacksRes.data.data
  const userData = usersRes.data.data

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold text-zinc-900">관리자 패널</h1>

      <section>
        <h2 className="mb-3 text-base font-semibold text-zinc-800">과자 등록</h2>
        <CreateSnackForm />
      </section>

      <section>
        <h2 className="mb-3 text-base font-semibold text-zinc-800">과자 관리</h2>
        {snackData && snackData.content.length > 0 ? (
          <div className="flex flex-col gap-3">
            {snackData.content.map((snack) => (
              <SnackCard key={snack.id} snack={snack} isAdmin showStatusAction />
            ))}
          </div>
        ) : (
          <p className="text-sm text-zinc-500">등록된 과자가 없습니다.</p>
        )}
      </section>

      <section>
        <h2 className="mb-3 text-base font-semibold text-zinc-800">유저 관리</h2>
        <UserManagement
          initialUsers={userData?.content ?? []}
          totalPages={userData?.totalPages ?? 0}
        />
      </section>
    </div>
  )
}
