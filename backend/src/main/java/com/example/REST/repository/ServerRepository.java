package com.example.REST.repository;

import org.springframework.data.repository.CrudRepository;

import com.example.REST.model.Server;

public interface ServerRepository extends CrudRepository<Server, Integer> {
}
