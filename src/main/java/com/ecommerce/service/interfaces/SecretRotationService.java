package com.ecommerce.service.interfaces;

public interface SecretRotationService {
    
    /**
     * Génère une chaîne de caractères aléatoire et sécurisée pour être utilisée comme secret
     * @return la chaîne de caractères générée
     */
    String generateSecureSecret();
    
    /**
     * Effectue la rotation du secret JWT selon la planification configurée
     * Remplace l'ancien secret par un nouveau et met à jour le fournisseur de token JWT
     */
    void rotateJwtSecret();
}