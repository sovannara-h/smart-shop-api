
import { useEffect } from "react";

export const usePreventNavigation = (shouldPrevent: boolean) => {

  const handleBeforeUnload = (event: BeforeUnloadEvent) => {
    if (shouldPrevent) {
      event.preventDefault();
      event.returnValue = ""; // Nécessaire pour Chrome
    }
  };

  const handlePopState = (event: PopStateEvent) => {
    if (
      shouldPrevent &&
      !window.confirm(
        "Vous avez des modifications non enregistrées. Voulez-vous vraiment quitter cette page ?",
      )
    ) {
      event.preventDefault();
      window.history.pushState(null, "", window.location.href);
    }
  };

  useEffect(() => {
    window.addEventListener("beforeunload", handleBeforeUnload);
    window.addEventListener("popstate", handlePopState);

    return () => {
      window.removeEventListener("beforeunload", handleBeforeUnload);
      window.removeEventListener("popstate", handlePopState);
    };
  }, [shouldPrevent]);
};
