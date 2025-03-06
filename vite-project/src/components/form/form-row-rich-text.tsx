import { FC } from 'react';
import { cn } from '../../lib/utils';
import { FormControl } from '../ui/form';
import { InputProps } from '../ui/input';
import { RichText } from '../ui/rich-text';
import { FormRow, FormRowProps } from './form-row';

export type FormRowTextFieldProps = {
    className?: string;
    isView?: boolean;
    funcConvertValue?: any;
} & Omit<FormRowProps, 'child'> &
    InputProps;

export const FormRowRichText: FC<FormRowTextFieldProps> = ({
    className = '',
    label,
    isView,
    name,
    funcConvertValue,
    ...restProps
}) => {
    return (
        <FormRow
            className={cn('FormRowTextField', className)}
            label={label}
            name={name}
            required={restProps.required}
            child={(field) => {
                if (isView)
                    return (
                        <p className={'text-right'}>
                            {funcConvertValue
                                ? funcConvertValue(field.value)
                                : field.value || '---'}
                        </p>
                    );

                return (
                    <FormControl>
                        <RichText
                            onChange={(value) => field.onChange(value)}
                            value={field.value}
                            {...restProps}
                        />
                    </FormControl>
                );
            }}
        />
    );
};
