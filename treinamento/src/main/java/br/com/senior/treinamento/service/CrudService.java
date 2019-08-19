package br.com.senior.treinamento.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.senior.treinamento.model.BaseEntity;

public abstract class CrudService<M extends BaseEntity, R extends JpaRepository<M, Long> & JpaSpecificationExecutor<M>> {
	
	protected R repository;
	
	public CrudService(R repository) {
		this.repository = repository;
	}
	
	public Page<M> list(Pageable pageable) {
		return this.repository.findAll(pageable);
	}
	
	public Page<M> filter (Specification<M> specification, Pageable pageable) { 		
		return this.repository.findAll(specification, pageable);
	}
	
	public M create(M entity) throws Exception {
		return this.repository.save(entity);
	}
	
	public M read(Long id) {
		return this.repository.findById(id).get();
	}
	
	public M update(M entity) throws Exception {
		if (entity.getId() == null)
			throw new Exception("Entidade ainda não persistida. Utilize o recurso de criação.");
		
		return this.create(entity);
	}
	

	public void delete(Long id) throws Exception {
		this.repository.deleteById(id);
	}
	
	public abstract List<Order> getDefaultSort();
}
