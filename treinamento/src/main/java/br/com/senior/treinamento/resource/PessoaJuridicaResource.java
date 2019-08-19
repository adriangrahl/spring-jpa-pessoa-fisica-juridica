package br.com.senior.treinamento.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.senior.treinamento.model.PessoaJuridica;
import br.com.senior.treinamento.repository.PessoaJuridicaRepository;
import br.com.senior.treinamento.service.PessoaJuridicaService;

@RestController
@RequestMapping("/pessoa_juridica")
public class PessoaJuridicaResource
		extends CrudResource<PessoaJuridica, PessoaJuridicaRepository, PessoaJuridicaService> {

	public PessoaJuridicaResource(PessoaJuridicaService service) {
		super(service);
	}

}
