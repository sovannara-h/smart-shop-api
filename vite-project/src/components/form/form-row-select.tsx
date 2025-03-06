import { cn } from '../../lib/utils';
import { FormRow, FormRowProps } from './form-row';
import { AutoSelect, AutoSelectProps } from '../ui/select';

export type FormRowSelectProps<T> = {
    className?: string;
} & AutoSelectProps &
    Omit<FormRowProps, 'child'>;

export const FormRowSelect = <T extends {}>(props: FormRowSelectProps<T>) => {
    const { className = '', label, name, options, ...restProps } = props;

    return (
        <FormRow
            className={cn('FormRowReferenceField', className)}
            label={label}
            name={name}
            required={props.required}
            child={(field) => {
                //@ts-ignore
                return (
                    <AutoSelect {...field} {...restProps} options={options} />
                );
            }}
        />
    );
};
