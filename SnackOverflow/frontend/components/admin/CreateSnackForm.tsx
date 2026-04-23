'use client'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useState, useTransition } from 'react'
import { createSnackAction } from '@/actions/snacks'

const schema = z.object({
  name: z.string().min(1, '과자 이름을 입력하세요'),
})

type FormValues = z.infer<typeof schema>

export default function CreateSnackForm() {
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
      const result = await createSnackAction(data.name)
      if (result.error) {
        setServerError(result.error)
      } else {
        reset()
        setSuccess(true)
      }
    })
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-2">
      {serverError && <p className="text-sm text-red-600">{serverError}</p>}
      {success && <p className="text-sm text-green-600">과자가 등록되었습니다.</p>}
      <div className="flex gap-2">
        <input
          {...register('name')}
          className="flex-1 rounded-md border border-zinc-300 px-3 py-2 text-sm focus:border-zinc-500 focus:outline-none"
          placeholder="과자 이름"
        />
        <button
          type="submit"
          disabled={isPending}
          className="rounded-md bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50"
        >
          {isPending ? '등록 중...' : '등록'}
        </button>
      </div>
      {errors.name && <p className="text-xs text-red-500">{errors.name.message}</p>}
    </form>
  )
}
