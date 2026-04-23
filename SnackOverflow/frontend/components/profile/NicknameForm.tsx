'use client'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useState, useTransition } from 'react'
import { updateNicknameAction } from '@/actions/users'

const schema = z.object({
  nickname: z.string().min(2, '닉네임은 2자 이상이어야 합니다'),
})

type FormValues = z.infer<typeof schema>

export default function NicknameForm({ currentNickname }: { currentNickname: string }) {
  const [serverError, setServerError] = useState<string>()
  const [success, setSuccess] = useState(false)
  const [isPending, startTransition] = useTransition()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { nickname: currentNickname },
  })

  const onSubmit = (data: FormValues) => {
    setServerError(undefined)
    setSuccess(false)
    startTransition(async () => {
      const result = await updateNicknameAction(data.nickname)
      if (result.error) setServerError(result.error)
      else setSuccess(true)
    })
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-2">
      {serverError && <p className="text-sm text-red-600">{serverError}</p>}
      {success && <p className="text-sm text-green-600">닉네임이 변경되었습니다.</p>}
      <div className="flex gap-2">
        <input
          {...register('nickname')}
          className="flex-1 rounded-md border border-zinc-300 px-3 py-2 text-sm focus:border-zinc-500 focus:outline-none"
        />
        <button
          type="submit"
          disabled={isPending}
          className="rounded-md bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50"
        >
          변경
        </button>
      </div>
      {errors.nickname && <p className="text-xs text-red-500">{errors.nickname.message}</p>}
    </form>
  )
}
