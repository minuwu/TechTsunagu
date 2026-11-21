import { queryOptions } from '@tanstack/react-query'
import { getHealth } from '../api'

export const healthQuery = () =>
  queryOptions({ queryKey: ['health'], queryFn: getHealth })
