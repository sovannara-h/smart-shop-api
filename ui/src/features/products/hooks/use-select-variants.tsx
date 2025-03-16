import { Attribute } from "@/types/attribute.types";
import { useQuery } from "@tanstack/react-query";
import { attributesApi } from "../api/attributes.api";
import { GenerateVariantsDTO, variantsApi } from "../api/variants.api";



export const useSelectVariants = (formSetValue: (value: any) => void) => {

    const {data} = useQuery({
        queryKey: ["attributes-with-categories"],
        queryFn: async () => await attributesApi.findAllAttributesWithValues(),
      })
    
      const generateCombinations = async (values: GenerateVariantsDTO) => {

        try {
            const combinations = await variantsApi.generateVariants(values)
            console.log("COBMINE", combinations)
            formSetValue(combinations.data)
            return combinations
        } catch (error) {
          console.log(error)            
        }

      };
      const attributes: Attribute[] = data?.data || [];



      return {
        attributes,
        generateCombinations,
      }
}