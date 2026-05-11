package REST.repository;

import org.springframework.data.repository.CrudRepository;

import REST.model.Server;

public interface ServerRepository extends CrudRepository<Server, Integer> {
}
