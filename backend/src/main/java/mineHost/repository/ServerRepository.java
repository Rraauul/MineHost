package mineHost.repository;

import org.springframework.data.repository.CrudRepository;

import mineHost.model.Server;

public interface ServerRepository extends CrudRepository<Server, Integer> {
}
