import { cn } from "@/lib/utils";
import { FormControl } from "../ui/form";
import { Input, InputProps } from "../ui/input";
import { FormRow, FormRowProps } from "./form-row";

export type  FormRowInputProps = {
    className?: string;
    isView?: boolean;
    funcConvertValue?: any;
    textarea?: boolean;
    decimal?: boolean;
}& Omit<FormRowProps, 'child'> &
InputProps;

export const FormRowInput = (props: FormRowInputProps) => {

    const {
        className = '',
        label,
        isView,
        name,
        textarea,
        decimal,
        funcConvertValue,
        PrevIcon,
        action,
        ...restProps
    } = props;

    return (
      <FormRow 
        className={cn(`FormRowInput`, className)} 
        label={label}
        name={name}
        required={restProps.required}
        PrevIcon={PrevIcon}
        action={action}
        child={(field) => {
            return (
                <FormControl>
                    <Input {...field} {...restProps}/>
                </FormControl>
            )
        }}
      />

    );
}