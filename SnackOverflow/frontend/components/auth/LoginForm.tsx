'use client'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useState, useTransition } from 'react'
import { loginAction } from '@/actions/auth'

const schema = z.object({
  username: z.string().min(1, '아이디를 입력하세요'),
  password: z.string().min(1, '비밀번호를 입력하세요'),
})

type FormValues = z.infer<typeof schema>

export default function LoginForm() {
  const [serverError, setServerError] = useState<string>()
  const [isPending, startTransition] = useTransition()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const onSubmit = (data: FormValues) => {
    setServerError(undefined)
    startTransition(async () => {
      const result = await loginAction(data.username, data.password)
      if (result?.error) setServerError(result.error)
    })
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
      {serverError && (
        <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{serverError}</p>
      )}
      <div className="flex flex-col gap-1">
        <label htmlFor="username" className="text-sm font-medium text-zinc-700">
          아이디
        </label>
        <input
          id="username"
          {...register('username')}
          className="rounded-md border border-zinc-300 px-3 py-2 text-sm focus:border-zinc-500 focus:outline-none"
          placeholder="아이디"
        />
        {errors.username && <p className="text-xs text-red-500">{errors.username.message}</p>}
      </div>
      <div className="flex flex-col gap-1">
        <label htmlFor="password" className="text-sm font-medium text-zinc-700">
          비밀번호
        </label>
        <input
          id="password"
          type="password"
          {...register('password')}
          className="rounded-md border border-zinc-300 px-3 py-2 text-sm focus:border-zinc-500 focus:outline-none"
          placeholder="비밀번호"
        />
        {errors.password && <p className="text-xs text-red-500">{errors.password.message}</p>}
      </div>
      <button
        type="submit"
        disabled={isPending}
        className="rounded-md bg-zinc-900 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50"
      >
        {isPending ? '로그인 중...' : '로그인'}
      </button>
    </form>
  )
}
