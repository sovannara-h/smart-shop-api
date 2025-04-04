package com.ecommerce.service.interfaces;

/** Service for sending notifications */
public interface NotificationService {
    /**
     * Sends an email notification
     * 
     * @param message content of the email
     */
    void sendEmail(String message);
    
    /**
     * Sends a notification to all administrators
     * 
     * @param message content of the notification
     */
    void notifyAdmins(String message);
}