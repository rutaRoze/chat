package com.ignitis.chat.persistance;

import com.ignitis.chat.persistance.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query(
            value = "SELECT * FROM role WHERE role_name = :roleName",
            nativeQuery = true
    )
    Optional<Role> findRoleByName(String roleName);
}
