package com.ecommerce.service.interfaces;

public interface NotificationService {
    void sendEmail(String message);
    void notifyAdmins(String message);
}