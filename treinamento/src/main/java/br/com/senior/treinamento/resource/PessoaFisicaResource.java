package br.com.senior.treinamento.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.senior.treinamento.model.PessoaFisica;
import br.com.senior.treinamento.repository.PessoaFisicaRepository;
import br.com.senior.treinamento.service.PessoaFisicaService;

@RestController
@RequestMapping("/pessoa_fisica")
public class PessoaFisicaResource extends CrudResource<PessoaFisica, PessoaFisicaRepository, PessoaFisicaService> {

	public PessoaFisicaResource(PessoaFisicaService service) {
		super(service);
	}

}
