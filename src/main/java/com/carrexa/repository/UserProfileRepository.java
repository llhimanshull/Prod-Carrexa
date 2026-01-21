package com.carrexa.repository;

import com.carrexa.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile , String> {
}
