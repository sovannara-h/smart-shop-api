"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import * as LabelPrimitive from "@radix-ui/react-label";
import { Slot } from "@radix-ui/react-slot";
import * as React from "react";
import { RefObject, useEffect, useRef } from "react";
import {
  Controller,
  ControllerProps,
  FieldErrors,
  FieldPath,
  FieldValues,
  FormProvider,
  useForm,
  useFormContext,
} from "react-hook-form";

import * as z from "zod";
import { usePreventNavigation } from "../../hooks/use-prevent-navigation";
import { cn } from "../../lib/utils";
import { Alert } from "./alert";
import { Button } from "./button";
import { Label } from "./label";
import { MultiStepLoader } from "./multi-step-loader";


type FormFieldContextValue<
  TFieldValues extends FieldValues = FieldValues,
  TName extends FieldPath<TFieldValues> = FieldPath<TFieldValues>,
> = {
  name: TName;
};

const FormFieldContext = React.createContext<FormFieldContextValue>(
  {} as FormFieldContextValue,
);

const FormField = <
  TFieldValues extends FieldValues = FieldValues,
  TName extends FieldPath<TFieldValues> = FieldPath<TFieldValues>,
>({
  ...props
}: ControllerProps<TFieldValues, TName>) => {
  return (
    <FormFieldContext.Provider value={{ name: props.name }}>
      <Controller {...props} />
    </FormFieldContext.Provider>
  );
};

const useFormField = () => {
  const fieldContext = React.useContext(FormFieldContext);
  const itemContext = React.useContext(FormItemContext);
  const { getFieldState, formState } = useFormContext();

  const fieldState = getFieldState(fieldContext.name, formState);

  if (!fieldContext) {
    throw new Error("useFormField should be used within <FormField>");
  }

  const { id } = itemContext;

  return {
    id,
    name: fieldContext.name,
    formItemId: `${id}-form-item`,
    formDescriptionId: `${id}-form-item-description`,
    formMessageId: `${id}-form-item-message`,
    ...fieldState,
  };
};

type FormItemContextValue = {
  id: string;
};

const FormItemContext = React.createContext<FormItemContextValue>(
  {} as FormItemContextValue,
);

const FormItem = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => {
  const id = React.useId();

  return (
    <FormItemContext.Provider value={{ id }}>
      <div ref={ref} className={cn("space-y-2", className)} {...props} />
    </FormItemContext.Provider>
  );
});
FormItem.displayName = "FormItem";

const FormLabel = React.forwardRef<
  React.ElementRef<typeof LabelPrimitive.Root>,
  React.ComponentPropsWithoutRef<typeof LabelPrimitive.Root>
>(({ className, ...props }, ref) => {
  const { error, formItemId } = useFormField();

  return (
    <Label
      ref={ref}
      className={cn(error && "text-destructive", className)}
      htmlFor={formItemId}
      {...props}
    />
  );
});
FormLabel.displayName = "FormLabel";

const FormControl = React.forwardRef<
  React.ElementRef<typeof Slot>,
  React.ComponentPropsWithoutRef<typeof Slot>
>(({ ...props }, ref) => {
  const { error, formItemId, formDescriptionId, formMessageId } =
    useFormField();

  return (
    <Slot
      ref={ref}
      id={formItemId}
      aria-describedby={
        !error
          ? `${formDescriptionId}`
          : `${formDescriptionId} ${formMessageId}`
      }
      aria-invalid={!!error}
      {...props}
    />
  );
});
FormControl.displayName = "FormControl";

const FormDescription = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => {
  const { formDescriptionId } = useFormField();

  return (
    <p
      ref={ref}
      id={formDescriptionId}
      className={cn("text-[0.8rem] text-muted-foreground", className)}
      {...props}
    />
  );
});
FormDescription.displayName = "FormDescription";

const FormMessage = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLParagraphElement>
>(({ className, children, ...props }, ref) => {
  const { error, formMessageId } = useFormField();
  const body = error ? String(error?.message) : children;

  if (!body) {
    return null;
  }

  return (
    <p
      ref={ref}
      id={formMessageId}
      className={cn("text-[0.8rem] font-medium text-destructive", className)}
      {...props}
    >
      {body}
    </p>
  );
});
FormMessage.displayName = "FormMessage";

type FormUnsavedProps = {
  isDirty: boolean;
  errors: FieldErrors<{ [x: string]: any }>;
  formRef: RefObject<HTMLFormElement | null>;
};

const FormUnsaved = ({ isDirty, errors, formRef }: FormUnsavedProps) => {
  usePreventNavigation(isDirty);

  useEffect(() => {
    const down = (e: KeyboardEvent) => {
      if (e.key === "s" && (e.metaKey || e.ctrlKey)) {
        e.preventDefault();
        formRef.current?.requestSubmit();
      }
    };

    document.addEventListener("keydown", down);
    return () => document.removeEventListener("keydown", down);
  }, []);

  return isDirty && Object.keys(errors).length === 0 ? (
    <Alert
      className="flex items-center gap-2 fixed bottom-5 right-5 w-fit"
      title={"Vous avez des modifications!"}
      action={
        <Button type="submit" className="gap-3">
          Sauvergarder
          <kbd className="pointer-events-none inline-flex h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium text-muted-foreground opacity-100">
            <span className="text-xs">⌘</span>S
          </kbd>
        </Button>
      }
    />
  ) : null;
};

type FormProps = {
  children: React.ReactNode;
  zodSchema: z.ZodSchema;
  initialData: any;
  className?: string;
  withoutUnsaved?: boolean;
  dataID?: string;
  dataCy?: string;
  handleSubmit: (values: any) => void;
};

const Form = (props: FormProps) => {
  const {
    zodSchema,
    children,
    initialData,
    handleSubmit,
    className = "",
    dataCy,
    dataID,
    withoutUnsaved = false,
  } = props;



  const formRef = useRef<HTMLFormElement | null>(null);

  const form = useForm<z.infer<typeof zodSchema>>({
    resolver: zodResolver(zodSchema),
    defaultValues: initialData,
  });

  const { isDirty, errors } = form.formState;

  const onSubmit = async (values: z.infer<typeof zodSchema>) => {
    try {
      if (isDirty) await handleSubmit(values);
    } catch (e) {
      console.error(e);
    }
  };

  const loadingStates = [
    { text: "Chargement du formulaire..." },
    { text: "Validation des champs..." },
    { text: "Préparation des données..." },
    { text: "Envoi du formulaire..." },
    { text: "Traitement de la réponse..." },
  ];


  return (
    <React.Fragment>
      <MultiStepLoader
        loadingStates={loadingStates}
        loading={form.formState.isLoading || form.formState.isSubmitting}
      />
      <FormProvider {...form}>
        <form
          data-cy={dataCy}
          data-id={dataID}
          ref={formRef}
          onSubmit={form.handleSubmit(onSubmit)}
          className={className}
        >
          {children}
          {/* {!withoutUnsaved ? (
            <FormUnsaved isDirty={isDirty} formRef={formRef} errors={errors} />
          ) : null} */}
        </form>
      </FormProvider>
    </React.Fragment>
  );
};

export {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  FormProvider,
  useFormField
};

