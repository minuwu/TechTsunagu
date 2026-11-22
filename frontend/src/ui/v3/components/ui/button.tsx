import * as React from 'react'
import { cva, type VariantProps } from 'class-variance-authority'
import { Slot } from 'radix-ui'
import { cn } from '@/ui/v3/lib/utils'

// shadcn button, tuned: 44px minimum touch target (F-24), visible focus ring (G-43),
// pressed and busy feedback within 100ms (F-30)
const buttonVariants = cva(
  "relative inline-flex shrink-0 cursor-pointer items-center justify-center gap-2 rounded-md text-base font-bold transition-[background-color,border-color,box-shadow,transform] duration-150 outline-none focus-visible:ring-[3px] focus-visible:ring-ring/40 active:scale-[0.98] disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-5",
  {
    variants: {
      variant: {
        default:
          'bg-primary text-primary-foreground shadow-[var(--shadow-sm)] hover:bg-primary/90 hover:shadow-[var(--shadow-md)]',
        outline:
          'border border-border bg-card text-foreground hover:border-primary hover:bg-accent hover:text-accent-foreground',
        ghost: 'text-foreground hover:bg-accent hover:text-accent-foreground',
        danger:
          'bg-destructive text-background shadow-[var(--shadow-sm)] hover:bg-destructive/90',
      },
      size: {
        default: 'min-h-11 px-5 py-2',
        sm: 'min-h-11 px-3',
        lg: 'min-h-14 px-6 text-lg',
      },
    },
    defaultVariants: { variant: 'default', size: 'default' },
  },
)

function Spinner() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" className="spin size-5">
      <circle
        cx="12"
        cy="12"
        r="9"
        fill="none"
        stroke="currentColor"
        strokeWidth="2.5"
        opacity="0.25"
      />
      <path
        d="M21 12a9 9 0 0 0-9-9"
        fill="none"
        stroke="currentColor"
        strokeWidth="2.5"
        strokeLinecap="round"
      />
    </svg>
  )
}

function Button({
  className,
  variant = 'default',
  size = 'default',
  asChild = false,
  loading = false,
  disabled,
  children,
  ...props
}: React.ComponentProps<'button'> &
  VariantProps<typeof buttonVariants> & {
    asChild?: boolean
    /** Busy: shows a spinner, blocks a second press, and tells assistive tech. */
    loading?: boolean
  }) {
  const Comp = asChild ? Slot.Root : 'button'
  return (
    <Comp
      data-slot="button"
      className={cn(buttonVariants({ variant, size, className }))}
      disabled={disabled || loading}
      aria-busy={loading || undefined}
      {...props}
    >
      {asChild ? (
        children
      ) : (
        <>
          {loading && <Spinner />}
          {children}
        </>
      )}
    </Comp>
  )
}

export { Button, buttonVariants }
