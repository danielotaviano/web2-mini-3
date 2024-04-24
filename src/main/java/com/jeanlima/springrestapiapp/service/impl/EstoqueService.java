package com.jeanlima.springrestapiapp.service.impl;

import com.jeanlima.springrestapiapp.exception.RegraNegocioException;
import com.jeanlima.springrestapiapp.model.Estoque;
import com.jeanlima.springrestapiapp.model.Produto;
import com.jeanlima.springrestapiapp.repository.EstoqueRepository;
import com.jeanlima.springrestapiapp.repository.ProdutoRepository;
import com.jeanlima.springrestapiapp.rest.dto.EstoqueDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstoqueService {

  @Autowired
  private EstoqueRepository estoqueRepository;

  @Autowired
  private ProdutoRepository produtoRepository;

  public Estoque create(EstoqueDTO dto) {

    Produto produto = produtoRepository.findById(dto.getProdutoId())
        .orElseThrow(() -> new RegraNegocioException("Código de produto inválido."));

    if (dto.getQuantidade() < 0) {
      throw new RegraNegocioException("Quantidade inválida.");
    }

    Optional<Estoque> alreadyExists = estoqueRepository.findByProdutoId(dto.getProdutoId());

    if (alreadyExists.isPresent()) {
      throw new RegraNegocioException("Produto já cadastrado no estoque.");
    }

    Estoque estoque = new Estoque();
    estoque.setProduto(produto);
    estoque.setQuantidade(dto.getQuantidade());

    return estoqueRepository.save(estoque);
  }

  public List<Estoque> readAll() {
    return estoqueRepository.findAll();
  }

  public Estoque read(Integer id) {
    Optional<Estoque> optionalEstoque = estoqueRepository.findById(id);
    return optionalEstoque.orElse(null);
  }

  public Estoque update(Integer id, Estoque estoque) {
    Optional<Estoque> optionalEstoque = estoqueRepository.findById(id);
    if (optionalEstoque.isPresent()) {
      Estoque existingEstoque = optionalEstoque.get();
      existingEstoque.setProduto(estoque.getProduto());
      existingEstoque.setQuantidade(estoque.getQuantidade());
      return estoqueRepository.save(existingEstoque);
    }
    return null;
  }

  public void delete(Integer id) {
    estoqueRepository.deleteById(id);
  }

  public Estoque filterByProductName(String productName) {
    return estoqueRepository.findByProdutoNome(productName);
  }

  public Estoque updateQuantity(Integer id, Integer quantity) {
    Optional<Estoque> optionalEstoque = estoqueRepository.findById(id);

    if (quantity < 0) {
      throw new RegraNegocioException("Quantidade inválida.");
    }

    if (optionalEstoque.isPresent()) {
      Estoque existingEstoque = optionalEstoque.get();
      existingEstoque.setQuantidade(quantity);
      return estoqueRepository.save(existingEstoque);
    }
    return null;
  }
}