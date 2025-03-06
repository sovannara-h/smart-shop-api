import { FC } from 'react';
import { cn } from '../../lib/utils';
import { FormControl } from '../ui/form';
import { Input, InputProps } from '../ui/input';
import { InputDecimal } from '../ui/input-decimal';
import { Textarea } from '../ui/textarea';
import { FormRow, FormRowProps } from './form-row';

export type FormRowTextFieldProps = {
    className?: string;
    isView?: boolean;
    funcConvertValue?: any;
    textarea?: boolean;
    decimal?: boolean;
} & Omit<FormRowProps, 'child'> &
    InputProps;

export const FormRowTextField: FC<FormRowTextFieldProps> = ({
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
}) => {
    //@ts-ignore
    const dataCy = restProps['data-cy'];
    return (
        <FormRow
            className={cn('FormRowTextField', className)}
            label={label}
            name={name}
            required={restProps.required}
            data-cy={dataCy}
            PrevIcon={PrevIcon}
            action={action}
            child={(field) => {
                if (isView)
                    return (
                        <p className={'text-right'}>
                            {funcConvertValue
                                ? funcConvertValue(field.value)
                                : field.value || '---'}
                        </p>
                    );

                const textArea = (
                    <Textarea
                        {...field}
                        {...(restProps as React.TextareaHTMLAttributes<HTMLTextAreaElement>)}
                    />
                );

                if (decimal) {
                    return (
                        <FormControl>
                            <InputDecimal {...field} {...restProps} />
                        </FormControl>
                    );
                } else {
                    return (
                        <FormControl>
                            {textarea ? (
                                textArea
                            ) : (
                                <Input {...field} {...restProps} />
                            )}
                        </FormControl>
                    );
                }
            }}
        />
    );
};
