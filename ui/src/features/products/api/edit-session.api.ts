

export const editSessionApi = {
    startSession: async ({entityType, entityId}: {entityType: string, entityId: string}) => {
        console.log("sessions start", entityType, entityId)
        const resSessionStart = await fetch(`http://localhost:8080/api/v1/sessions/start`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({
              entityType, 
              entityId: entityId || null,
            })
          });

          const data = await resSessionStart.json();
          console.log(data)
          return data.sessionId;
    },
    startCreateSession: async ({entityType}: {entityType: string}) => {
      const resSessionStart = await fetch(`http://localhost:8080/api/v1/sessions/start-create`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            entityType
          })
        });

        const data = await resSessionStart.json();
        console.log(data)
        return data.sessionId;
    },
    cancelSession: async ({sessionId}: {sessionId: string}) => {
      const response = await fetch(`http://localhost:8080/api/v1/sessions/${sessionId}/cancel`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
      });
      if (!response.ok) throw new Error('Erreur lors de l\'annulation de la session')
        return response.json()
    },
    confirmSession: async ({sessionId}: {sessionId: string}) => {
      const response = await fetch(`http://localhost:8080/api/v1/sessions/${sessionId}/confirm`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
      });
      if (!response.ok) throw new Error('Erreur lors de la confirmation de la session')
        return response.json()
    },
}