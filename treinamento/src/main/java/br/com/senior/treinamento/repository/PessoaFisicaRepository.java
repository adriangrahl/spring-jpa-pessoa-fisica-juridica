package br.com.senior.treinamento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.senior.treinamento.model.PessoaFisica;

public interface PessoaFisicaRepository extends JpaRepository<PessoaFisica, Long>, JpaSpecificationExecutor<PessoaFisica> {

	

}
