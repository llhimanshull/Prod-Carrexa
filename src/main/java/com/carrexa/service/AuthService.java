package com.carrexa.service;

import com.carrexa.config.RabbitMQConfig;
import com.carrexa.dto.rabbitMq.UserRegisterEvent;
import com.carrexa.dto.request.UserRegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.ws.rs.core.Response;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${keycloak.server-url}")
    private String serverUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.client-id}")
    private String clientId; // e.g., "carrexa.ai"
    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final RabbitTemplate rabbitTemplate;

    public ResponseEntity<?> registerUser(UserRegisterRequest request) {
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
        user.setUsername(request.getUserName());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmailVerified(false);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);
        user.setCredentials(Collections.singletonList(credential));

        // 3. Create User
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        Response response = usersResource.create(user);


        if (response.getStatus() == 201) {
            try {
                String userId = CreatedResponseUtil.getCreatedId(response);
                String clientUuid = realmResource.clients().findByClientId(clientId).get(0).getId();

                //ASSIGN ROLE
                RoleRepresentation clientRole = realmResource.clients().get(clientUuid)
                        .roles().get(String.valueOf(request.getUserRole())).toRepresentation();
                usersResource.get(userId).roles().clientLevel(clientUuid).add(Arrays.asList(clientRole));

                //USER REGISTER EVENT
                UserRegisterEvent event = new UserRegisterEvent(request , userId);
                log.info("================== SENDIGN MESSAGE TO RABBIT MQ ==================");
                log.info(String.valueOf(event));
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.ROUTING_KEY,
                        event
                );

                return ResponseEntity.status(HttpStatus.CREATED).body("User registered. Profile creation started in background.");
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }else{
            String errorBody = response.readEntity(String.class);
            String status = response.getStatusInfo().getReasonPhrase();

            log.error("Keycloak Creation Failed. Status: {}, Error: {}", response.getStatus(), errorBody);

            // Return the specific error to Postman/Frontend
            return ResponseEntity.status(response.getStatus())
                    .body("Failed to create user: " + status + " - " + errorBody);
        }
    }
}