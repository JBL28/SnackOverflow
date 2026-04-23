'use client'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useState, useTransition } from 'react'
import { changePasswordAction } from '@/actions/users'

const schema = z.object({
  currentPassword: z.string().min(1, '현재 비밀번호를 입력하세요'),
  newPassword: z.string().min(6, '새 비밀번호는 6자 이상이어야 합니다'),
})

type FormValues = z.infer<typeof schema>

export default function PasswordForm() {
  const [serverError, setServerError] = useState<string>()
  const [success, setSuccess] = useState(false)
  const [isPending, startTransition] = useTransition()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const onSubmit = (data: FormValues) => {
    setServerError(undefined)
    setSuccess(false)
    startTransition(async () => {
      const result = await changePasswordAction(data.currentPassword, data.newPassword)
      if (result.error) setServerError(result.error)
      else { reset(); setSuccess(true) }
    })
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3">
      {serverError && <p className="text-sm text-red-600">{serverError}</p>}
      {success && <p className="text-sm text-green-600">비밀번호가 변경되었습니다.</p>}
      <div className="flex flex-col gap-1">
        <input
          type="password"
          {...register('currentPassword')}
          className="rounded-md border border-zinc-300 px-3 py-2 text-sm focus:border-zinc-500 focus:outline-none"
          placeholder="현재 비밀번호"
        />
        {errors.currentPassword && (
          <p className="text-xs text-red-500">{errors.currentPassword.message}</p>
        )}
      </div>
      <div className="flex flex-col gap-1">
        <input
          type="password"
          {...register('newPassword')}
          className="rounded-md border border-zinc-300 px-3 py-2 text-sm focus:border-zinc-500 focus:outline-none"
          placeholder="새 비밀번호 (6자 이상)"
        />
        {errors.newPassword && (
          <p className="text-xs text-red-500">{errors.newPassword.message}</p>
        )}
      </div>
      <button
        type="submit"
        disabled={isPending}
        className="rounded-md bg-zinc-900 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50"
      >
        {isPending ? '변경 중...' : '비밀번호 변경'}
      </button>
    </form>
  )
}
