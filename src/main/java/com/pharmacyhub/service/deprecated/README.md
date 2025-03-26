# Deprecated Files

This directory contains deprecated files that were created during the authentication flow enhancement but are no longer needed.

## Backend Deprecated Files

- `EmailService.java.new` - Earlier version of the enhanced email service with login notification capability
- `AuthService.java.login-notification` - Partial implementation of login notification feature
- `AuthService.java.login-notification-completion` - Completion of login notification feature implementation

These files were moved here as part of the cleanup process when implementing the feature-based architecture for the authentication flow enhancement. The actual implementation has been integrated into the main service files.

The login notification feature adds security by:
1. Detecting logins from new devices
2. Sending notification emails with device details
3. Providing security action links

This helps users monitor unauthorized access to their accounts.
