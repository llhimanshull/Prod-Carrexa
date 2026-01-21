package com.carrexa.dto.rabbitMq;

import com.carrexa.dto.request.UserRegisterRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterEvent implements Serializable {

    private UserRegisterRequest request;
    private String  userId;

}
