import { cn } from "@/lib/utils";
import { Checkbox } from "../ui/checkbox";
import { FormRow, FormRowProps } from "./form-row";

export type FormRowCheckboxProps = {
    className?: string;
} & Omit<FormRowProps, 'child'>;

export const FormRowCheckbox = (props: FormRowCheckboxProps) => {
    const { className = '', label, name, ...restProps } = props;

    return (
        <FormRow
            className={cn('FormRowCheckbox flex items-center space-x-2', className)}
            label={label}
            name={name}
            required={restProps.required}
            child={(field) => {
                return (
                    <Checkbox
                        checked={field.value}
                        onCheckedChange={field.onChange}
                    />
                );
            }}
        />
    );
} 