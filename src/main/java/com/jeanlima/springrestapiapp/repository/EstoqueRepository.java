package com.jeanlima.springrestapiapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jeanlima.springrestapiapp.model.Estoque;

public interface EstoqueRepository extends JpaRepository<Estoque, Integer> {
    @Query("SELECT e FROM Estoque e WHERE e.produto.descricao = :productName")
    Estoque findByProdutoNome(@Param("productName") String productName);

    @Query("SELECT e FROM Estoque e WHERE e.produto.id = :id")
    Optional<Estoque> findByProdutoId(@Param("id") Integer id);
}
