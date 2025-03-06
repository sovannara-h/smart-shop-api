import { FC, ReactNode } from 'react';
import { cn } from '../../lib/utils';
import { FormDescription, FormLabel } from './form';
import { Separator } from './separator';
import { Skeleton } from './skeleton';

type FormSectionProps = {
    title: string;
    description: string;
    children: ReactNode;
    addButton?: ReactNode;
    className?: string;
    'data-cy'?: string;
};

export const FormSectionSkeleton = ({ children }: { children: ReactNode }) => {
    return (
        <div>
            <div className="mt-4 mb-4 flex items-center justify-between gap-10">
                <div className="h-full w-full gap-2 space-y-1">
                    <Skeleton className="h-6 w-[30%]" />
                    <Skeleton className="h-4 w-full" />
                </div>
                <Skeleton className="h-10 w-40" />
            </div>
            <div className="space-y-4">
                <Separator />
                {children}
            </div>
        </div>
    );
};

export const FormSection: FC<FormSectionProps> = ({
    title,
    description,
    children,
    addButton = null,
    ...props
}) => {
    return (
        <div data-cy={props['data-cy'] || ''}>
            <div
                className={cn(
                    'mt-4 mb-4 flex items-center justify-between gap-4',
                    props.className,
                )}
            >
                <div className="space-y-2">
                    <FormLabel className="FormSectionLabel font-bold text-lg">
                        {title}
                    </FormLabel>
                    <FormDescription>{description}</FormDescription>
                    {/* <Text as={"h3"} size={"xl"} weight={"semibold"}>
            {title}
          </Text>
          <Text color={"muted"} size={"sm"}>
            {description}
          </Text> */}
                </div>
                {addButton}
            </div>
            <div className="space-y-5">
                <Separator />
                {children}
            </div>
        </div>
    );
};
