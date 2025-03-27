import { zodResolver } from "@hookform/resolvers/zod";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { editSessionApi } from "../api/edit-session.api";
import { productsApi } from "../api/product.api";
import { variantsApi } from "../api/variants.api";

const step1Schema = z.object({
    categories: z.array(z.number()).min(1, "Sélectionnez au moins une catégorie"),
  })
  
  const step2Schema = z.object({
    name: z.string().min(2, "Le nom doit contenir au moins 2 caractères"),
    description: z.string().optional(),
    basePrice: z.number().or(z.string()),
    baseStock: z.number().or(z.string()),
  })
  
  const step3Schema = z.object({
    hasVariants: z.boolean().default(false),
    variants: z.array(z.object({
      sku: z.string(),
      attributeValues: z.any().array(),
      price: z.number(),
      stockQuantity: z.number()
    })).optional()
  }).array()
  

  const productImageSchema = z.object({
    image_id: z.string(),
    filename: z.string().optional(),
    variant: z.string().array().optional()
  })
  
  const step4Schema = productImageSchema.array();

  export type ProductImageData = z.infer<typeof productImageSchema>
  
  const productVariantSchema = z.object({
    sku: z.string(),
    name: z.string(),
    attributeValues: z.record(z.string(), z.string()).array(),
    price: z.number(),
    stockQuantity: z.number()
  })

  export type ProductVariantData = z.infer<typeof productVariantSchema>

  const productSchema = z.object({
    id: z.number().optional(),
    // Étape 1
    categories: z.array(z.number()).min(1, "Sélectionnez au moins une catégorie"),
    
    // Étape 2
    name: z.string().min(2, "Le nom doit contenir au moins 2 caractères"),
    description: z.string().optional(),
    basePrice: z.number().min(0, "Le prix ne peut pas être négatif"),
    baseStock: z.number().min(0, "Le stock ne peut pas être négatif"),
    
    // Étape 3
    hasVariants: z.boolean().default(false),
    variants: productVariantSchema.array(),

    images: productImageSchema.array()
  })
  
  
  
export type ProductFormData = z.infer<typeof productSchema>

export const useProductForm = () => {
    const [currentStep, setCurrentStep] = useState(0);
    const [errors, setErrors] = useState<string[]>([]);
    const [loadingSubmitProduct, setLoadingSubmitProduct] = useState(false);
    const [sessionId, setSessionId] = useState<string | null>(null);

    const form = useForm<ProductFormData>({
        resolver: zodResolver(productSchema),
        defaultValues: {
          hasVariants: false,
          variants: [],
          images: []
        }
      })


    const productId = form.getValues('id');
      
    const validateStep = async (stepData: any) => {
        try {
          switch (currentStep) {
            case 0:
              await step1Schema.parseAsync(stepData)
              break
            case 1:
              return await handleProductCreation(stepData)
            case 2: 
            console.log("CASE 2", stepData, form.formState.errors, step3Schema.parseAsync(stepData))
              await step3Schema.parseAsync(stepData.variants);
              break
            case 3:
              console.log("CASE 3")
              return await submitProductVariants(stepData)
            case 4:
              console.log("CASE 4")
              return await submitProductImages(stepData)
            case 5:
              return await confirmProduct();
          }
          return true
        } catch (error) {
          if (error instanceof z.ZodError) {
            setErrors(error.errors.map(e => e.message))
          }
          return false
        }
    }

    const confirmProduct = async () => {
      try {
        
      } catch (error) {
        console.error(error)
      }
    }

    const handleProductCreation = async (stepData: any) => {
      setLoadingSubmitProduct(true)
      try {
        await step2Schema.parseAsync(stepData)
        const sessionId = await editSessionApi.startCreateSession({entityType: "PRODUCT"});
        const response = await productsApi.createProduct(stepData, sessionId)
        setSessionId( sessionId)
        if (response.data && sessionId) {
          
          form.reset({
            ...form.getValues(),
            ...response.data
          })
          setLoadingSubmitProduct(false)
          return true
        }

        setLoadingSubmitProduct(false)
        return false
      } catch (error) {
        console.error("Erreur lors de la création du produit:", error)
        setErrors(["Erreur lors de la création du produit"])
        setLoadingSubmitProduct(false)
        return false
      }
    }

    const submitProductVariants = async (stepData: any) => {
      try {
        await step3Schema.parseAsync(stepData.variants);
        const variants = form.getValues('variants')
        const response = await variantsApi.createProductVariants(productId, sessionId, variants);
        return true
      } catch (error) {
        console.error(error)
        return false
      }
    }

    const submitProductImages = async (stepData: any) => {
      try {
        await step4Schema.parseAsync(stepData.images);
        const formImages = form.getValues('images') || [];
        
        if (!formImages.length) {
          return false;
        }

        const productImages = formImages.map(img => ({
          imageId: img.image_id,
          filename: img.filename || '',
          variant: img.variant || []
        }));

        const response = await productsApi.upsertProductImages(productId, sessionId, productImages);
        return true;
      } catch (error) {
        console.error(error);
        return false;
      }
    }

    const nextStep = async (stepData: any) => {
        // const isValid = await form.trigger()
        const isValid = await validateStep(stepData);
        console.log("valid step", isValid)
        if(isValid) {
          setCurrentStep(s => s + 1)
        }
      }

      
      const prevStep = () => {
        setCurrentStep(s => s - 1)
        setErrors([])
      }

      return {
        form,
        currentStep,
        setCurrentStep,
        nextStep,
        prevStep,
        loadingSubmitProduct
      }

}