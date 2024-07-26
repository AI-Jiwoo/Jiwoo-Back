package org.jiwoo.back.user.repository;

import org.jiwoo.back.user.aggregate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
