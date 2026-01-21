package com.carrexa.consumer;

import com.carrexa.config.RabbitMQConfig;
import com.carrexa.dto.rabbitMq.UserRegisterEvent;
import com.carrexa.entity.UserProfile;
import com.carrexa.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegistrationConsumer {

    private final UserProfileRepository userProfileRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeUserRegistration( UserRegisterEvent event) {

        String role = String.valueOf(event.getRequest().getUserRole());

        System.out.println("RabbitMQ Worker: Received event for " + role);

        if ("user".equalsIgnoreCase(role)) {
            createCandidateProfile(event);
        }
    }

    private void createCandidateProfile(UserRegisterEvent event) {
        UserProfile profile = UserProfile.builder()
                .userId(event.getUserId())
                .email(event.getRequest().getEmail())
                .userName(event.getRequest().getUserName())
                .firstName(event.getRequest().getFirstName())
                .lastName(event.getRequest().getLastName())
                .role(event.getRequest().getUserRole())
                .build();

        userProfileRepository.save(profile);
        System.out.println("================= USER REGISTERED SUCCESSFULLY====================");

    }

}
