import { cn } from "@/lib/utils";
import { FormControl } from "../ui/form";
import { InputDecimal, InputDecimalProps } from "../ui/input-decimal";
import { FormRow, FormRowProps } from "./form-row";

export type FormRowInputDecimalProps = {
    className?: string;
    isView?: boolean;
    funcConvertValue?: any;
} & Omit<FormRowProps, 'child'> & InputDecimalProps;

export const FormRowInputDecimal = (props: FormRowInputDecimalProps) => {
    const {
        className = '',
        label,
        isView,
        name,
        funcConvertValue,
        PrevIcon,
        action,
        decimalPlaces,
        ...restProps
    } = props;

    return (
        <FormRow 
            className={cn(`FormRowInputDecimal`, className)} 
            label={label}
            name={name}
            required={restProps.required}
            PrevIcon={PrevIcon}
            action={action}
            child={(field) => {
                return (
                    <FormControl>
                        <InputDecimal {...field} {...restProps} decimalPlaces={decimalPlaces} />
                    </FormControl>
                )
            }}
        />
    );
} 