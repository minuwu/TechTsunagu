import { useMutation, useQueryClient } from '@tanstack/react-query'

/** A write that refreshes every query when it succeeds. */
export function useAction<A, R>(fn: (arg: A) => Promise<R>) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: () => qc.invalidateQueries(),
  })
}
