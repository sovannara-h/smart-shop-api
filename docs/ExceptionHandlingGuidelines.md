# Directives de Gestion des Exceptions

## Principes généraux

1. **Ne jamais utiliser d'exceptions génériques** (`RuntimeException`, `Exception`) directement dans le code. Utilisez toujours des exceptions spécifiques qui décrivent clairement le problème.

2. **Hiérarchie des exceptions**:
   - Créer des exceptions métier spécifiques pour chaque domaine (produits, utilisateurs, commandes, etc.)
   - Étendre `RuntimeException` pour les exceptions non vérifiées
   - Inclure la cause d'origine dans les exceptions lancées

3. **Messages d'exception**:
   - Les messages doivent être clairs et descriptifs
   - Ils doivent inclure les informations nécessaires pour comprendre et résoudre le problème
   - Ne pas inclure d'informations sensibles

## Structure recommandée

```java
public class SomeSpecificException extends RuntimeException {
  public SomeSpecificException(String message) {
    super(message);
  }
  
  public SomeSpecificException(String message, Throwable cause) {
    super(message, cause);
  }
}
```

## Exceptions spécifiques existantes à utiliser

- `ProductException`: Pour les erreurs générales liées aux produits
- `ProductNotFoundException`: Quand un produit n'est pas trouvé
- `ProductVariantException`: Pour les erreurs liées aux variantes de produits
- `UserException`: Pour les erreurs générales liées aux utilisateurs
- `UserNotFoundException`: Quand un utilisateur n'est pas trouvé
- `OrderException`: Pour les erreurs générales liées aux commandes
- `SessionOperationException`: Pour les erreurs liées aux opérations sur les sessions
- `ControllerException`: Pour les erreurs spécifiques aux contrôleurs
- `ServiceOperationException`: Pour les erreurs générales dans les opérations de service
- (etc.)

## Gestionnaire global d'exceptions

L'application utilise un `GlobalExceptionHandler` centralisé pour toutes les exceptions. Ce gestionnaire:

1. Transforme les exceptions en réponses API standardisées
2. Attribue les bons codes HTTP selon le type d'exception
3. Journalise les exceptions de manière appropriée
4. Protège les informations sensibles des utilisateurs

Pour ajouter une nouvelle gestion d'exception:
1. Créez une classe d'exception spécifique si nécessaire
2. Ajoutez une méthode dédiée dans `GlobalExceptionHandler`:

```java
@ExceptionHandler(NouvelleException.class)
public ResponseEntity<ApiResponse<Object>> handleNouvelleException(
    NouvelleException ex, WebRequest request) {
  log.error("Message descriptif: {}", ex.getMessage());
  return ResponseEntity.status(HttpStatus.APPROPRIATE_STATUS)
      .body(new ApiResponse<>(false, null, null, ex.getMessage(), LocalDateTime.now()));
}
```

## Bonnes pratiques

1. **Journalisation**: Journaliser les exceptions avec le niveau de sévérité approprié
   ```java
   try {
     // code susceptible de lancer une exception
   } catch (SpecificException e) {
     log.error("Message descriptif: {}", paramètre, e);
     throw new DomainSpecificException("Message d'erreur", e);
   }
   ```

2. **Conservation du contexte**: Toujours préserver la cause d'origine
   ```java
   throw new DomainSpecificException("Message descriptif", originalException);
   ```

3. **Validation d'entrée**: Valider les entrées le plus tôt possible pour éviter les exceptions en aval

4. **Audits réguliers**: Effectuer régulièrement un audit de code pour identifier les usages d'exceptions génériques avec:
   ```bash
   grep -r "throw new RuntimeException" src/main/java
   grep -r "throw new Exception" src/main/java
   ```

## Traitement dans les contrôleurs

Dans les contrôleurs, les exceptions devraient être capturées et transformées en réponses HTTP appropriées.
Cependant, avec le `GlobalExceptionHandler`, cela est majoritairement géré automatiquement.

Si vous devez traiter une exception spécifiquement dans un contrôleur:

```java
try {
  // Code qui peut lancer une exception
} catch (SpecificException e) {
  log.error("Erreur spécifique: {}", e.getMessage());
  return ResponseEntity.status(HttpStatus.APPROPRIATE_STATUS)
      .body(new ApiResponse<>(false, null, null, e.getMessage(), LocalDateTime.now()));
}
```

## Processus d'amélioration continue

1. Lors de l'identification d'une exception générique:
   - Créer une exception spécifique si elle n'existe pas déjà
   - Remplacer l'exception générique par l'exception spécifique
   - Ajouter une journalisation appropriée
   - S'assurer que la propagation est correcte
   - Ajouter un gestionnaire dans `GlobalExceptionHandler` si nécessaire 