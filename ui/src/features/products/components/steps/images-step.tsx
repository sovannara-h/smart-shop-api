import { ImageUploader } from "@/components/ui/image-uploader";
import { ReactNode } from "react";
import { UseFormReturn } from "react-hook-form";
import { ProductFormData } from "../../hooks/use-product-form";

export type  ImagesStepProps = {
    form: UseFormReturn<ProductFormData>
    navigation: ReactNode
}

export const ImagesStep = (props: ImagesStepProps) => {

    const {form, navigation} = props;


    console.log(form.getValues())
    return (
      <div className={`ImagesStep`}>
        <ImageUploader ctxName={"product-form"} cb={(images: {uuid: string, filename: string}[]) => {
            form.setValue("images", [...form.getValues("images"), ...images.map(i => ({image_id: i.uuid, filename: i.filename}))])
        }} />
        <div>
            {form.watch("images").map(image => {
                return <img src={`https://ucarecdn.com/${image.image_id}/`} alt={''}/>
            })}
        </div>
        {navigation}
      </div>
    );
}