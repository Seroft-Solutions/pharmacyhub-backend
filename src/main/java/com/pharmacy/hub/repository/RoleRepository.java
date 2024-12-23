package com.pharmacy.hub.repository;

import com.pharmacy.hub.constants.RoleEnum;
import com.pharmacy.hub.entity.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, Integer>
{
  Optional<Role> findByName(RoleEnum name);
}