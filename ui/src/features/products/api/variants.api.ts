export interface GenerateVariantsDTO {
  attributes: number[];
  productId: number, basePrice: string | number, baseStock: string | number
}

export const variantsApi = {
  generateVariants: async (data: GenerateVariantsDTO) => {
    const response = await fetch(
      `http://localhost:8080/api/v1/products/${data.productId}/variants/generate`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      },
    );

    if (!response.ok)
      throw new Error("Erreur lors de la génération des variantes");
    return response.json();
  },
  createProductVariants: async (productId: number | undefined, sessionId: string | null, data: any) => {
    if(!productId || !sessionId) return;
    console.log("DATA", data)
    const response = await fetch(
      `http://localhost:8080/api/v1/products/${productId}/variants`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          'X-Session-Id': sessionId
        },
        body: JSON.stringify({productVariants: data.map(v => ({...v, attributeValues: v.attributeValues.map(v => v.id)}))}),
      },
    );
    if (!response.ok)
    throw new Error("Erreur lors de la ctreation des variants");
    return response.json();
  }
};
