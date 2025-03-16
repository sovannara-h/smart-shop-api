

export const attributesApi = {
    findAllAttributesWithValues: async () => {
        const response = await fetch(
            `http://localhost:8080/api/v1/attributes/with-values`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                  }
            }
        )

        if (!response.ok)
            throw new Error("Erreur lors de la génération des variantes");
          return await  response.json();
    }
}