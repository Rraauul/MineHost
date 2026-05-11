package mineHost.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import mineHost.model.Utilisateur;

// This will be AUTO IMPLEMENTED by Spring into a Bean called SkieurRepository
// CRUD refers Create, Read, Update, Delete

public interface UtilisateurRepository extends CrudRepository<Utilisateur, Integer> {
    Optional<Utilisateur> findByName(String name);
}