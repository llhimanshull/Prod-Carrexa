package com.carrexa.carrexa.service;

import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.ws.rs.core.Response;
import java.util.*;

@Service
public class AuthService {

    @Value("${keycloak.server-url}")
    private String serverUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.client-id}")
    private String clientId; // e.g., "carrexa.ai"
    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public String registerUser(String username, String password, String email, String roleName) {
        // 1. Connect to Keycloak
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType("client_credentials")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();

        // 2. Prepare User Data
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(username);
        user.setEmail(email);
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(Collections.singletonList(credential));

        // 3. Create User
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        Response response = usersResource.create(user);

        if (response.getStatus() == 201) {
            String userId = CreatedResponseUtil.getCreatedId(response);

            // --- CHANGED LOGIC FOR CLIENT ROLES ---

            // A. Find the internal UUID of the Client (e.g., "carrexa.ai" -> "a1b2-c3d4...")
            String clientUuid = realmResource.clients().findByClientId(clientId).get(0).getId();

            // B. Fetch the specific Client Role (user, recruiter, or admin)
            // We use the input 'roleName' so you can dynamically switch between "user" and "recruiter"
            RoleRepresentation clientRole = realmResource.clients().get(clientUuid)
                    .roles().get(roleName).toRepresentation();

            // C. Assign the Role at the CLIENT LEVEL (not Realm Level)
            usersResource.get(userId).roles().clientLevel(clientUuid).add(Arrays.asList(clientRole));

            return "User created and Client Role '" + roleName + "' assigned successfully.";
        } else {
            return "Error: " + response.getStatus();
        }
    }
}