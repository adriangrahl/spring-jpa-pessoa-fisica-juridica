package br.com.senior.treinamento.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.senior.treinamento.model.Pessoa;
import br.com.senior.treinamento.repository.PessoaRepository;
import br.com.senior.treinamento.service.PessoaService;

@RestController
@RequestMapping("/pessoa")
public class PessoaResource extends CrudResource<Pessoa, PessoaRepository, PessoaService> {

	@Autowired
	public PessoaResource(PessoaService service) {
		super(service);
	}
}
