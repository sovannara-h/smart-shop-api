#!/bin/bash

echo "===== Vérification des exceptions génériques dans le code ====="

# Vérifier RuntimeException
echo "== Recherche des 'throw new RuntimeException' =="
grep -r "throw new RuntimeException" --include="*.java" src/main/java
RUNTIME_COUNT=$?

# Vérifier Exception
echo "== Recherche des 'throw new Exception' =="
grep -r "throw new Exception" --include="*.java" src/main/java
EXCEPTION_COUNT=$?

# Vérifier les catch génériques
echo "== Recherche des 'catch (Exception' sans relancer d'exception spécifique =="
grep -r -A 3 "catch (Exception" --include="*.java" src/main/java | grep -v "throw new [A-Z]"
CATCH_COUNT=$?

# Résultats
echo "===== Résultats ====="
if [ $RUNTIME_COUNT -eq 0 ] || [ $EXCEPTION_COUNT -eq 0 ] || [ $CATCH_COUNT -eq 0 ]; then
  echo "Des exceptions génériques ont été trouvées. Veuillez les remplacer par des exceptions spécifiques."
  exit 1
else
  echo "Aucune exception générique trouvée. Bon travail !"
  exit 0
fi 