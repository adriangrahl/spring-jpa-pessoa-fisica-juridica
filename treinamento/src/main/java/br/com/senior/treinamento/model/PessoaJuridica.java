package br.com.senior.treinamento.model;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

@Entity(name = "PESSOA_JURIDICA")
public class PessoaJuridica extends Pessoa {
	
	@NotBlank
	private String cnpj;

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}
}
