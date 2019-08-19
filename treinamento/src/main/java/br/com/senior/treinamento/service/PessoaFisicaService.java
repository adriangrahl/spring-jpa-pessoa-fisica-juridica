package br.com.senior.treinamento.service;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import br.com.senior.treinamento.model.PessoaFisica;
import br.com.senior.treinamento.repository.PessoaFisicaRepository;

@Service
public class PessoaFisicaService extends CrudService<PessoaFisica, PessoaFisicaRepository> {

	public PessoaFisicaService(PessoaFisicaRepository repository) {
		super(repository);
	}

	@Override
	public List<Order> getDefaultSort() {
		return Collections.singletonList(Order.asc("cpf"));
	}
}
