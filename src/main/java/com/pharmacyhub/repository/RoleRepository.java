package com.pharmacyhub.repository;

import com.pharmacyhub.constants.RoleEnum;
import com.pharmacyhub.entity.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, Integer>
{
  Optional<Role> findByName(RoleEnum name);
}