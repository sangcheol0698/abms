import type { VariantProps } from "class-variance-authority"
import { cva } from "class-variance-authority"

export { default as Badge } from "./Badge.vue"

export const badgeVariants = cva(
  "inline-flex items-center justify-center rounded-md border border-border/60 bg-muted/50 px-2 py-0.5 text-xs font-medium text-foreground w-fit whitespace-nowrap shrink-0 gap-1 [&>svg]:size-3 [&>svg]:pointer-events-none transition-[color,box-shadow] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/40 focus-visible:ring-offset-1 focus-visible:ring-offset-background aria-invalid:border-destructive aria-invalid:ring-destructive/30 dark:border-slate-700 dark:bg-slate-900/60 dark:text-slate-100 overflow-hidden",
  {
    variants: {
      variant: {
        default:
          "border-primary/60 bg-primary text-primary-foreground [a&]:hover:bg-primary/90",
        secondary:
          "border-secondary/60 bg-secondary/90 text-secondary-foreground [a&]:hover:bg-secondary/80 dark:bg-secondary/40",
        destructive:
          "border-transparent bg-destructive text-white [a&]:hover:bg-destructive/90 focus-visible:ring-destructive/20 dark:focus-visible:ring-destructive/40 dark:bg-destructive/70",
        outline:
          "border-border/70 bg-transparent text-muted-foreground [a&]:hover:bg-accent [a&]:hover:text-accent-foreground",
        ghost:
          "border-transparent bg-transparent text-muted-foreground",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  },
)
export type BadgeVariants = VariantProps<typeof badgeVariants>
