package com.example.APIGateWay.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.APIGateWay.model.Server;

public interface ServerRepository extends CrudRepository<Server, Integer> {
}
