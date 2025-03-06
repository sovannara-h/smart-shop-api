import { cva, type VariantProps } from 'class-variance-authority';
import * as React from 'react';
import { cn } from '../../lib/utils';

const alertVariants = cva(
    'relative w-full rounded-lg border px-4 py-3 text-sm [&>svg+div]:translate-y-[-3px] [&>svg]:absolute [&>svg]:left-4 [&>svg]:top-4 [&>svg]:text-foreground [&>svg~*]:pl-7',
    {
        variants: {
            variant: {
                default: 'bg-background text-foreground',
                destructive:
                    'border-destructive/50 text-destructive dark:border-destructive [&>svg]:text-destructive',
            },
        },
        defaultVariants: {
            variant: 'default',
        },
    },
);

const AlertRoot = React.forwardRef<
    HTMLDivElement,
    React.HTMLAttributes<HTMLDivElement> & VariantProps<typeof alertVariants>
>(({ className, variant, ...props }, ref) => (
    <div
        ref={ref}
        role="alert"
        className={cn(alertVariants({ variant }), className)}
        {...props}
    />
));
AlertRoot.displayName = 'AlertRoot';

const AlertTitle = React.forwardRef<
    HTMLParagraphElement,
    React.HTMLAttributes<HTMLHeadingElement>
>(({ className, ...props }, ref) => (
    <h5
        ref={ref}
        className={cn(
            'mb-1 font-medium leading-none tracking-tight',
            className,
        )}
        {...props}
    />
));
AlertTitle.displayName = 'AlertTitle';

const AlertDescription = React.forwardRef<
    HTMLParagraphElement,
    React.HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => (
    <div
        ref={ref}
        className={cn('text-sm [&_p]:leading-relaxed', className)}
        {...props}
    />
));
AlertDescription.displayName = 'AlertDescription';

export type AlertProps = {
    icon?: React.ReactNode;
    title?: React.ReactNode;
    description?: React.ReactNode;
    action?: React.ReactNode;
    className?: string;
};

export const Alert = ({
    icon,
    title,
    description,
    action,
    className = '',
}: AlertProps) => {
    return (
        <AlertRoot className={className}>
            {icon ? icon : null}
            {title ? <AlertTitle>{title}</AlertTitle> : null}
            {description ? (
                <AlertDescription>{description}</AlertDescription>
            ) : null}
            {action ? action : null}
        </AlertRoot>
    );
};
export { AlertDescription, AlertRoot, AlertTitle };
