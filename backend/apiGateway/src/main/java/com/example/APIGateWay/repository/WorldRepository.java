package com.example.APIGateWay.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.APIGateWay.model.World;

public interface WorldRepository extends CrudRepository<World, Integer> {
    // findByFkUser
    List<World> findByFkUser(Integer fkUser);
}
