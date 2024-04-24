package com.jeanlima.springrestapiapp.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jeanlima.springrestapiapp.enums.StatusPedido;
import com.jeanlima.springrestapiapp.exception.PedidoNaoEncontradoException;
import com.jeanlima.springrestapiapp.exception.RegraNegocioException;
import com.jeanlima.springrestapiapp.model.Cliente;
import com.jeanlima.springrestapiapp.model.Estoque;
import com.jeanlima.springrestapiapp.model.ItemPedido;
import com.jeanlima.springrestapiapp.model.Pedido;
import com.jeanlima.springrestapiapp.model.Produto;
import com.jeanlima.springrestapiapp.repository.ClienteRepository;
import com.jeanlima.springrestapiapp.repository.EstoqueRepository;
import com.jeanlima.springrestapiapp.repository.ItemPedidoRepository;
import com.jeanlima.springrestapiapp.repository.PedidoRepository;
import com.jeanlima.springrestapiapp.repository.ProdutoRepository;
import com.jeanlima.springrestapiapp.rest.dto.ItemPedidoDTO;
import com.jeanlima.springrestapiapp.rest.dto.PedidoDTO;
import com.jeanlima.springrestapiapp.service.PedidoService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository repository;
    private final ClienteRepository clientesRepository;
    private final ProdutoRepository produtosRepository;
    private final ItemPedidoRepository itemsPedidoRepository;
    private final EstoqueRepository estoqueRepository;

    @Override
    @Transactional
    public Pedido salvar(PedidoDTO dto) {
        Integer idCliente = dto.getCliente();
        Cliente cliente = clientesRepository
                .findById(idCliente)
                .orElseThrow(() -> new RegraNegocioException("Código de cliente inválido."));

        Pedido pedido = new Pedido();
        pedido.setDataPedido(LocalDate.now());
        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.REALIZADO);

        List<ItemPedido> itemsPedido = converterItems(pedido, dto.getItems());
        BigDecimal total = BigDecimal.valueOf(itemsPedido
                .stream()
                .mapToDouble(i -> i.getQuantidade().doubleValue() * i.getProduto().getPreco().doubleValue())
                .sum());

        pedido.setTotal(total);
        repository.save(pedido);
        itemsPedidoRepository.saveAll(itemsPedido);
        pedido.setItens(itemsPedido);
        return pedido;
    }

    private List<ItemPedido> converterItems(Pedido pedido, List<ItemPedidoDTO> items) {
        if (items.isEmpty()) {
            throw new RegraNegocioException("Não é possível realizar um pedido sem items.");
        }

        return items
                .stream()
                .map(dto -> {
                    Integer idProduto = dto.getProduto();
                    Produto produto = produtosRepository
                            .findById(idProduto)
                            .orElseThrow(
                                    () -> new RegraNegocioException(
                                            "Código de produto inválido: " + idProduto));

                    Estoque estoque = estoqueRepository
                            .findByProdutoId(idProduto)
                            .orElseThrow(() -> new RegraNegocioException("Produto sem estoque: " + idProduto));

                    if (estoque.getQuantidade() < dto.getQuantidade()) {
                        throw new RegraNegocioException("Quantidade insuficiente em estoque.");
                    }

                    estoque.setQuantidade(estoque.getQuantidade() - dto.getQuantidade());
                    estoqueRepository.save(estoque);

                    ItemPedido itemPedido = new ItemPedido();
                    itemPedido.setQuantidade(dto.getQuantidade());
                    itemPedido.setPedido(pedido);
                    itemPedido.setProduto(produto);
                    return itemPedido;
                }).collect(Collectors.toList());

    }

    @Override
    @Transactional
    public Pedido atualizar(Integer id, PedidoDTO dto) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException());

        List<ItemPedido> oldItemsPedido = new ArrayList<>(pedido.getItens());
        pedido.getItens().clear();

        for (ItemPedido oldItem : oldItemsPedido) {
            Estoque estoque = estoqueRepository.findByProdutoId(oldItem.getProduto().getId())
                    .orElseThrow(() -> new RegraNegocioException("Estoque não encontrado."));
            estoque.setQuantidade(estoque.getQuantidade() + oldItem.getQuantidade());
            itemsPedidoRepository.delete(oldItem);
            estoqueRepository.save(estoque);
        }

        if (dto.getCliente() != null) {
            Cliente cliente = clientesRepository.findById(dto.getCliente())
                    .orElseThrow(() -> new RegraNegocioException("Código de cliente inválido."));
            pedido.setCliente(cliente);
        }

        if (dto.getItems() != null) {
            List<ItemPedido> newItemsPedido = converterItems(pedido, dto.getItems());
            for (ItemPedidoDTO itemDto : dto.getItems()) {
                for (ItemPedido item : newItemsPedido) {
                    if (item.getProduto().getId().equals(itemDto.getProduto())) {
                        item.setQuantidade(itemDto.getQuantidade());
                    }
                }
            }
            BigDecimal total = BigDecimal.valueOf(newItemsPedido
                    .stream()
                    .mapToDouble(i -> i.getQuantidade().doubleValue() * i.getProduto().getPreco().doubleValue())
                    .sum());
            pedido.setTotal(total);
            pedido.setItens(newItemsPedido);
        }

        return repository.save(pedido);
    }

    @Override
    public Optional<Pedido> obterPedidoCompleto(Integer id) {
        return repository.findByIdFetchItens(id);
    }

    @Override
    public void atualizaStatus(Integer id, StatusPedido statusPedido) {
        repository
                .findById(id)
                .map(pedido -> {
                    pedido.setStatus(statusPedido);
                    return repository.save(pedido);
                }).orElseThrow(() -> new PedidoNaoEncontradoException());
    }

    public void delete(Integer id) {
        repository.findById(id)
                .map(pedido -> {
                    repository.deleteById(id);
                    return Void.TYPE;
                })
                .orElseThrow(() -> new PedidoNaoEncontradoException());
    }

}
