
import { cn } from "@/lib/utils";
import { Select, SelectProps } from "../ui/select";
import { FormRow, FormRowProps } from "./form-row";

export type  FormRowSelectProps = {
    className?: string;
} & SelectProps &
Omit<FormRowProps, 'child'>;

export const FormRowSelect = (props: FormRowSelectProps) => {

    const { className = '', label, name, options, ...restProps } = props;

    return (
        <FormRow
        className={cn('FormRowSelect', className)}
        label={label}
        name={name}
        required={props.required}
        child={(field) => {
            return (
                <Select {...field} {...restProps} options={options} />
            );
        }}
    />
    );
}