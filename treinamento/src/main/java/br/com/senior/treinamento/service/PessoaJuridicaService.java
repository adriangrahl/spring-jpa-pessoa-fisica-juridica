package br.com.senior.treinamento.service;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import br.com.senior.treinamento.model.PessoaJuridica;
import br.com.senior.treinamento.repository.PessoaJuridicaRepository;

@Service
public class PessoaJuridicaService extends CrudService<PessoaJuridica, PessoaJuridicaRepository> {

	public PessoaJuridicaService(PessoaJuridicaRepository repository) {
		super(repository);
	}

	@Override
	public List<Order> getDefaultSort() {
		return Collections.singletonList(Order.asc("cnpj"));
	}
}
