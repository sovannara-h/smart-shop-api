"use client";

import { forwardRef } from "react";
import { Input, InputProps } from "./input";

export type InputDecimalProps = InputProps & {
  decimalPlaces?: number;
};

const extractNumber = (input: string): number => {
  const digits = input.replace(/\D/g, "");
  return parseInt(digits, 10);
};

const convertMinNum = (input: string) => {
  if (input.length < 3) return input.padEnd(3, "0");
  return input;
};
export const InputDecimal = forwardRef<HTMLInputElement, InputDecimalProps>(
  ({ decimalPlaces = 2, ...props }, ref) => {
    const formatValue = (value: number) => {
      if (!value) return "0,00";
      const stringValue = value.toString();
      const integerPart = stringValue.slice(0, -2);
      const decimalPart = stringValue.slice(-2);
      return `${integerPart},${decimalPart}`;
    };

    return (
      <Input
        ref={ref}
        {...props}
        type="text"
        onChange={(e) => {
          const { value } = e.target;

          if (props.onChange) {
            const syntheticEvent = {
              target: { value: String(extractNumber(value)) }
            } as React.ChangeEvent<HTMLInputElement>;
            
            props.onChange(syntheticEvent);
          }
        }}
        defaultValue={Number("000")}
        value={formatValue(Number(props.value))}
      />
    );
  },
);
InputDecimal.displayName = "InputDecimal";
