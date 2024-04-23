package com.jeanlima.springrestapiapp.rest.controllers;

import com.jeanlima.springrestapiapp.model.Estoque;
import com.jeanlima.springrestapiapp.rest.dto.EstoqueDTO;
import com.jeanlima.springrestapiapp.service.impl.EstoqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

  @Autowired
  private EstoqueService estoqueService;

  @PostMapping
  public Estoque create(@RequestBody EstoqueDTO estoque) {
    return estoqueService.create(estoque);
  }

  @GetMapping
  public List<Estoque> readAll() {
    return estoqueService.readAll();
  }

  @GetMapping("/{id}")
  public Estoque read(@PathVariable Integer id) {
    return estoqueService.read(id);
  }

  @PutMapping("/{id}")
  public Estoque update(@PathVariable Integer id, @RequestBody Estoque estoque) {
    return estoqueService.update(id, estoque);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Integer id) {
    estoqueService.delete(id);
  }

  @GetMapping("/filter/{productName}")
  public Estoque filterByProductName(@PathVariable String productName) {
    System.out.println("productName: " + productName);
    return estoqueService.filterByProductName(productName);
  }

  @PatchMapping("/{id}")
  public Estoque updateQuantity(@PathVariable Integer id, @RequestBody Estoque estoque) {
    return estoqueService.updateQuantity(id, estoque.getQuantidade());
  }
}