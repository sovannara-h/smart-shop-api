import { cn } from "@/lib/utils";
import { FormControl } from "../ui/form";
import { Textarea, TextareaProps } from "../ui/textarea";
import { FormRow, FormRowProps } from "./form-row";

export type FormRowTextareaProps = {
    className?: string;
    isView?: boolean;
    funcConvertValue?: any;
} & Omit<FormRowProps, 'child'> & TextareaProps;

export const FormRowTextarea = (props: FormRowTextareaProps) => {
    const {
        className = '',
        label,
        isView,
        name,
        funcConvertValue,
        PrevIcon,
        action,
        ...restProps
    } = props;

    return (
        <FormRow 
            className={cn(`FormRowTextarea`, className)} 
            label={label}
            name={name}
            required={restProps.required}
            PrevIcon={PrevIcon}
            action={action}
            child={(field) => {
                return (
                    <FormControl>
                        <Textarea {...field} {...restProps} />
                    </FormControl>
                )
            }}
        />
    );
} 