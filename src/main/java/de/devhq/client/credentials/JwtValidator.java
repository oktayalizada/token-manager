package de.devhq.client.credentials;

import de.devhq.TokenManagerProperties;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;

public class JwtValidator {


    private static final Logger logger = LoggerFactory.getLogger(JwtValidator.class);

    private JwtValidator() {
    }

    public static int extractUserIdFromJwt() {
        AbstractAuthenticationToken authenticationToken = (AbstractAuthenticationToken) TokenManagerProperties.getSecurityContext().getAuthentication();
        SimpleKeycloakAccount details = (SimpleKeycloakAccount) authenticationToken.getDetails();
        String value = (String) details.getKeycloakSecurityContext().getToken().getOtherClaims().get(TokenManagerProperties.getUserId());
        if (value == null) {
            logger.error("Requesting client is not an end user, hence token does not contain gitlab user id!");
            throw new ValidationException();
        }

        int userId;
        userId = Integer.parseInt(value);

        if (userId <= 0) {
            logger.error("User id may not be none positive number! API seems to be hacked! Please report this to admin");
            throw new ValidationException();
        }

        return userId;
    }

    public static boolean isInternalUser(HttpServletRequest request) {
        return request.isUserInRole(TokenManagerProperties.getMachineRole()) || request.isUserInRole(TokenManagerProperties.getAdminRole());
    }

    public static boolean isExternalUser(HttpServletRequest request) {
        return !(request.isUserInRole(TokenManagerProperties.getMachineRole()) || request.isUserInRole(TokenManagerProperties.getAdminRole()))
                && request.isUserInRole(TokenManagerProperties.getUserRole());
    }
}
