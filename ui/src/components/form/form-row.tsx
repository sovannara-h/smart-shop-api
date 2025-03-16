import { cloneElement, isValidElement, ReactNode } from "react";
import {
    ControllerRenderProps,
    FieldValues,
    useFormContext,
} from "react-hook-form";
import { cn } from "../../lib/utils";
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from "../ui/form";
import { Skeleton } from "../ui/skeleton";



export const FormRowSkeleton = ({
  classNameField,
}: {
  classNameField?: string;
}) => {
  return (
    <div className="space-y-2">
      <Skeleton className="h-4 w-20" />
      <Skeleton className={cn("h-10 w-full", classNameField)} />
    </div>
  );
};

export type FormRowProps = {
  label: string;
  name: string;
  required?: boolean;
  className?: string;
  PrevIcon?: React.ElementType;
  action?: ReactNode;
  child: (field: ControllerRenderProps<FieldValues, string>) => React.ReactElement<{ className?: string }>;
};

export const FormRow = (props: FormRowProps) => {
  const { label, name, required, child, className, PrevIcon, action } = props;
  const { control } = useFormContext();

  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => {
        return (
          <FormItem className={className}>
            {label ? (
              <FormLabel className="font-semibold">
                {label}
                {required && <span className={"text-destructive"}>*</span>}
              </FormLabel>
            ) : null}
            <div className="relative">
              {PrevIcon ? (
                <PrevIcon className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
              ) : null}
              <FormControl>
                {child(field) && isValidElement(child(field))
                  ? cloneElement(child(field) as React.ReactElement<{ className: string }>, {
                      className: `${PrevIcon ? "pl-10" : ""} ${PrevIcon ? "pr-10" : ""}`,
                    })
                  : null}
              </FormControl>
              {action && isValidElement(action)
                ? cloneElement(action, {
                    className: `absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 ${props.className || ""}`,
                  } as React.HTMLAttributes<HTMLElement>)
                : action}
              <FormMessage
                className={`FormRow__message absolute top-full text-sm text-destructive font-semibold`}
              />
            </div>
          </FormItem>
        );
      }}
    />
  );
};
