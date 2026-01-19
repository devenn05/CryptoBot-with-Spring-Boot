package com.learning.cryptobot.repository;

import com.learning.cryptobot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
