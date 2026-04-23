'use client'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useState, useTransition } from 'react'
import { signupAction } from '@/actions/auth'

const schema = z.object({
  username: z.string().min(4, '아이디는 4자 이상이어야 합니다'),
  email: z.string().email('올바른 이메일을 입력하세요'),
  password: z.string().min(6, '비밀번호는 6자 이상이어야 합니다'),
  nickname: z.string().min(2, '닉네임은 2자 이상이어야 합니다'),
})

type FormValues = z.infer<typeof schema>

const FIELDS = [
  { id: 'username' as const, label: '아이디', placeholder: '아이디 (4자 이상)' },
  { id: 'email' as const, label: '이메일', placeholder: 'example@email.com', type: 'email' },
  { id: 'password' as const, label: '비밀번호', placeholder: '6자 이상, 영문+숫자', type: 'password' },
  { id: 'nickname' as const, label: '닉네임', placeholder: '닉네임 (2자 이상)' },
]

export default function SignupForm() {
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
      const result = await signupAction(data.username, data.email, data.password, data.nickname)
      if (result?.error) setServerError(result.error)
    })
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">
      {serverError && (
        <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">{serverError}</p>
      )}
      {FIELDS.map(({ id, label, placeholder, type }) => (
        <div key={id} className="flex flex-col gap-1">
          <label htmlFor={id} className="text-sm font-medium text-zinc-700">
            {label}
          </label>
          <input
            id={id}
            type={type ?? 'text'}
            {...register(id)}
            className="rounded-md border border-zinc-300 px-3 py-2 text-sm focus:border-zinc-500 focus:outline-none"
            placeholder={placeholder}
          />
          {errors[id] && <p className="text-xs text-red-500">{errors[id]?.message}</p>}
        </div>
      ))}
      <button
        type="submit"
        disabled={isPending}
        className="rounded-md bg-zinc-900 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50"
      >
        {isPending ? '가입 중...' : '회원가입'}
      </button>
    </form>
  )
}
