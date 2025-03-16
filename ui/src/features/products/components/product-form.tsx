import { Button } from "@/components/ui/button";
import { Form } from "@/components/ui/form";
import { Stepper } from "@/components/ui/stepper";
import { ProductFormData, useProductForm } from "../hooks/use-product-form";
import { CategoriesStep } from "./steps/categories-step";
import { CombinationsStep } from "./steps/combinations-step";
import { GeneralStep } from "./steps/general-step";
import { ImagesStep } from "./steps/images-step";
import { ResumeStep } from "./steps/resume-step";
import { VariantsStep } from "./steps/variants-step";

export const PRODUCT_FORM_STEPS = {
  CATEGORIES: 0,
  GENERAL: 1,
  VARIANTS: 2,
  COMBINATIONS: 3,
  IMAGES: 4,
  RESUME: 5
} as const;

type StepNavigationProps = {
  currentStep: number;
  onPrevious: () => void;
  onNext: () => void;
  isNextDisabled?: boolean;
  isPreviousDisabled?: boolean;
};

const StepNavigation = ({
  currentStep,
  onPrevious,
  onNext,
  isNextDisabled,
  isPreviousDisabled,
}: StepNavigationProps) => (
  <div className="flex justify-between pt-4 w-fit gap-4">
    <Button
      type="button"
      variant="outline"
      onClick={onPrevious}
      disabled={isPreviousDisabled}
    >
      Previous
    </Button>
    <Button type="button" onClick={onNext} disabled={isNextDisabled}>
      Next
    </Button>
  </div>
);

interface Attribute {
  name: string;
  values: string[];
}


export const ProductForm = () => {
  const { form, currentStep, setCurrentStep, nextStep, prevStep, loadingSubmitProduct } =
    useProductForm();


  const onSubmit = (data: ProductFormData) => {
    console.log(data);
  };

  const handleNextStep = async () => {
    await nextStep(form.getValues());
  };

  const renderCurrentStep = () => {
    switch (currentStep) {
      case PRODUCT_FORM_STEPS.CATEGORIES:
        return (
          <CategoriesStep
            form={form}
            navigation={
              <StepNavigation
                currentStep={currentStep}
                onPrevious={() => setCurrentStep((s) => s - 1)}
                onNext={handleNextStep}
                isPreviousDisabled={currentStep === 0}
                isNextDisabled={form.getValues("categories")?.length === 0}
              />
            }
          />
        );

      case PRODUCT_FORM_STEPS.GENERAL:
        return (
          <GeneralStep
            form={form}
            navigation={
              <StepNavigation
                currentStep={currentStep}
                onPrevious={() => setCurrentStep((s) => s - 1)}
                onNext={handleNextStep}
                isNextDisabled={loadingSubmitProduct}
              />
            }
          />
        );

      case PRODUCT_FORM_STEPS.VARIANTS:
        return (
          <VariantsStep
            form={form}
            onNext={handleNextStep}
            onPrevious={() => setCurrentStep((s) => s - 1)}
          />
        );

      case PRODUCT_FORM_STEPS.COMBINATIONS:
        return <CombinationsStep form={form} navigation={
          <StepNavigation
                currentStep={currentStep}
                onPrevious={() => setCurrentStep((s) => s - 1)}
                onNext={handleNextStep}
              />
        } />;

      case PRODUCT_FORM_STEPS.IMAGES:
        return <ImagesStep 
          form={form}
          navigation={
            <StepNavigation
                  currentStep={currentStep}
                  onPrevious={() => setCurrentStep((s) => s - 1)}
                  onNext={handleNextStep}
                />
          }
        />
      case PRODUCT_FORM_STEPS.RESUME:
        return <ResumeStep />
      default:
        return null;
    }
  };

  return (
    <div className="flex gap-8 flex-1">
      <Stepper currentStep={currentStep} />
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6 flex-1">
          {renderCurrentStep()}
        </form>
      </Form>
    </div>
  );
};









// import React, { useState } from 'react';
// import axios from 'axios';

// const ProductForm = ({ product, sessionId, onSave }) => {
//   const [formData, setFormData] = useState(product);
  
//   const handleChange = (e) => {
//     const { name, value } = e.target;
//     setFormData({
//       ...formData,
//       [name]: value
//     });
//   };

//   const handleSubmit = async (e) => {
//     e.preventDefault();
//     try {
//       // Envoyer les modifications avec l'ID de session
//       await axios.put(`/api/products/${product.id}`, formData, {
//         headers: { 'X-Session-Id': sessionId }
//       });
//       onSave();
//     } catch (error) {
//       console.error('Erreur lors de la mise à jour:', error);
//     }
//   };

//   return (
//     <form onSubmit={handleSubmit}>
//       <div className="form-group">
//         <label>Nom</label>
//         <input
//           type="text"
//           name="name"
//           value={formData.name}
//           onChange={handleChange}
//           className="form-control"
//         />
//       </div>
//       <div className="form-group">
//         <label>Description</label>
//         <textarea
//           name="description"
//           value={formData.description}
//           onChange={handleChange}
//           className="form-control"
//         />
//       </div>
//       <button type="submit" className="btn btn-primary">
//         Enregistrer les modifications
//       </button>
//     </form>
//   );
// };