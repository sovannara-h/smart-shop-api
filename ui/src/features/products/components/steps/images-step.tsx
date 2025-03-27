import { ImageUploader } from "@/components/ui/image-uploader";
import { ReactNode, useCallback } from "react";
import { UseFormReturn } from "react-hook-form";
import { ProductFormData } from "../../hooks/use-product-form";

export type ImagesStepProps = {
    form: UseFormReturn<ProductFormData>
    navigation: ReactNode
}

export const ImagesStep = (props: ImagesStepProps) => {
    const {form, navigation} = props;
    const images = form.watch("images") || [];

    const handleImageUpload = useCallback((newImages: {uuid: string, filename: string}[]) => {
        form.setValue("images", [
            ...images,
            ...newImages.map(img => ({
                image_id: img.uuid,
                filename: img.filename
            }))
        ], { shouldDirty: true });
    }, [form, images]);

    const handleImageDelete = useCallback((imageId: string) => {
        form.setValue(
            "images",
            images.filter(img => img.image_id !== imageId),
            { shouldDirty: true }
        );
    }, [form, images]);

    return (
      <div className="ImagesStep space-y-4">
        <ImageUploader 
            ctxName="product-form" 
            cb={handleImageUpload}
        />
        {images.length > 0 && (
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {images.map((image) => (
                    <div key={image.image_id} className="relative group">
                        <img 
                            src={`https://ucarecdn.com/${image.image_id}/`} 
                            alt={image.filename}
                            className="w-full h-32 object-cover rounded-lg"
                        />
                        <button
                            onClick={() => handleImageDelete(image.image_id)}
                            className="absolute top-2 right-2 p-1 bg-red-500 rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
                        >
                            <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>
                ))}
            </div>
        )}
        {navigation}
      </div>
    );
}