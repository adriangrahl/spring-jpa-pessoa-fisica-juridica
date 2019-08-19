package br.com.senior.treinamento.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import br.com.senior.treinamento.model.BaseEntity;

/**
 * 
 * Responsável pela decodificação do filtro passado na URL para um predicado
 * 
 * O o filtro deve ser passado na URL através de um parâmetro "q", por exemplo:
 * http://domain/path?q=id:10
 * 
 * 
 * Operadores
 * 
 * Os operadores provêm formas de comparação de valores, conforme a especificação abaixo:
 * 
 * Nome do Operador     Operador         Função
 * EQUAL                EQ ou suprimido  Verificar se os valores são iguais
 * CONTAINS             CT               Verifica se um texto contém determinado texto em seu conteúdo
 * LESSTHAN             LT               Verifica se é menor que determinado valor
 * LESSTHANOREQUALTO    LTE              Verifica se é menor ou igual a determinado valor
 * GREATERTHAN          GT               Verifica se é maior que determinado valor
 * GREATERTHANOREQUALTO GTE              Verifica se é maior ou igual a determinado valor
 * BETWEEN              BT               Verifica se está entre dois valores
 * IN                   IN               Verifica se está entre uma lista de valores
 * 
 * Sintaxe
 * 
 * Os operadores devem ser colocados entre colchetes logo após o nome do atributo:
 * q=nome[EQ]:João da Silva ou q=nome:João da Silva 
 * q=nome[CT]:João
 * q=valor[LT]:100
 * q=valor[LTE]:100
 * q=valor[GT]:100
 * q=valor[GTE]:100
 * q=valor[BT]:100-200
 * q=valor[IN]:100,120,150
 * 
 * AND/OR
 * 
 * Os operadores AND (e) e OR (ou) podem ser usados da forma habitual em consultas SQL,
 * incluindo aninhamentos:
 * 
 * q=nome:João da Silva AND (valor[LT]:100 OR valor[GT]:500)
 * 
 * @author Paulo Alonso
 *
 */
public class QueryDecoder<M extends BaseEntity> implements Specification<M> {

    private static final long serialVersionUID = 1L;
	
	private Group g;
	
	public QueryDecoder(String query) {
		this.g = new Group(query);
	}

	@Override
	public Predicate toPredicate(Root<M> from, CriteriaQuery<?> query, CriteriaBuilder cb) {
		return g.decode(cb, from);
	}

	public Predicate decode(CriteriaBuilder cb, Root<M> from) {
        return g.decode(cb, from);
    }
    
    private interface Decoder {
        public Predicate decode(CriteriaBuilder cb, Root<?> from);
        public LogicalOperator getLogicalOperator();
        public Decoder next();
        public void parse(String query);
    }
    
    /**
     * Abstrai agrupamentos de comparações feitos por parênteses
     * 
     */
    private class Group implements Decoder {
        private Decoder decoder;
        private LogicalOperator logicalOperator;
        private Decoder next;
        
        public Group(String query) {
            parse(query);
        }
        
        @Override
        public final void parse(String query) {
            int open = 0;
            int close = 0;
            StringBuilder sb = new StringBuilder();
            
            // Passa os operadores lógicos para maiúsculas
            query = query.replace(" and ", " AND ").replace(" or ", " OR ");
            
            // Se for um grupo
            if (query.startsWith("(")) {
                // Itera os caracteres
                for (int i = 0; i < query.length(); i++) {
                    // Armazena os caracteres percorridos
                    sb.append(query.charAt(i));

                    // Se abrir parênteses, incrementa open
                    if (query.charAt(i) == '(')
                        open++;
                    // Se fechar parênteses, incrementa close
                    else if (query.charAt(i) == ')')
                        close++;

                    // Ao fechar o grupo, armazena o operador lógico
                    if (open == close) {
                        if (sb.toString().endsWith(" AND "))
                            this.logicalOperator = LogicalOperator.AND;
                        else if (sb.toString().endsWith(" OR "))
                            this.logicalOperator = LogicalOperator.OR;
                    }
                        
                    // Se houver a mesma quantidade de parênteses abertos e fechados
                    // e se o operador lógico foi encontrado ou se chegou ao final da string
                    // cria um grupo
                    if (open == close && (this.logicalOperator != null || i == query.length() - 1)) {
                        this.decoder = 
                                new Group(
                                        query.substring(
                                                1, 
                                                this.logicalOperator.equals(LogicalOperator.AND) ? 
                                                    i - 5
                                                    :
                                                    this.logicalOperator.equals(LogicalOperator.OR) ?
                                                        i - 4
                                                        :
                                                        i
                                        )
                                );

                        // Retira a parte já processada da string
                        query = query.substring(i);
                        
                        if (!query.trim().isEmpty()) {
                            if (query.startsWith("("))
                                this.next = new Group(query);
                            else
                                this.next = new Expression(query);
                        }
                    }
                }
            }
            // Se não for um grupo
            else                
                this.decoder = new Expression(query);
        }
        
        @Override
        public Predicate decode(CriteriaBuilder cb, Root<?> from) {
            Predicate p = this.decoder.decode(cb, from);
            
            if (next != null) {
                if (logicalOperator.equals(LogicalOperator.AND))
                    cb.and(
                            p,
                            next.decode(cb, from)
                    );
            }

            return p;
        }
        
        @Override
        public LogicalOperator getLogicalOperator() {
            return this.logicalOperator;
        }
        
        @Override
        public Decoder next() {
            return this.next;
        }
        
        @Override
        public String toString() {
            String result;
            
            if (decoder instanceof QueryDecoder.Group)
                result = String.format("(%s)", decoder);
            else
                result = decoder.toString();

            if (next != null) {
                String model;

                if (next instanceof QueryDecoder.Group)
                    model = " %s (%s)"; 
                else
                    model = " %s %s";

                result = result.concat(String.format(model, logicalOperator, next));
            }
            
            return result;
        }
        
        private class Expression implements Decoder {
            private String field;
            private String value;
            private MatchType matchType;
            private LogicalOperator logicalOperator;
            private Decoder next;

            public Expression(String expression) {
                parse(expression);
            }

            @Override
            public final void parse(String query) {
                // Armazena o índice dos operadores lógicos
                int and = query.indexOf(" AND ");
                int or  = query.indexOf(" OR ");
                
                // Se o índice for menor que zero, o operador não existe na expressão 
                // e por isso recebe o tamanho da string para ser o valor máximo
                and = and < 0 ? query.length() : and;
                or  = or < 0 ? query.length() : or;
                
                // Armazena o índice do primeiro operador lógico
                // Caso não exista operador lógico na expressão, armazena o comprimento da string
                int i;
                
                if (and < or) {
                    i = and;
                    this.logicalOperator = LogicalOperator.AND;
                } else if (or < and) {
                    i = or;
                    this.logicalOperator = LogicalOperator.OR;
                } else
                    i = query.length();
                
                // Isola a expressão atual
                String expression = query.substring(0, i).trim();
                
                // Retira a expressão da string
                if (and < or)
                    query = query.substring(i + 5);
                else if (or < and)
                    query = query.substring(i + 4);
                else
                    query = query.substring(i);

                // Obtém o atributo
                Pattern p = Pattern.compile("((\\w.*(?=:))(?<!\\]))|(\\w.*(?=\\[))");
                Matcher m = p.matcher(expression);

                if (m.find())
                    this.field = m.group();

                // Obtém o operador
                String matchTypes = 
                        Arrays.asList(MatchType.values())
                            .stream()
                            .map(mt -> mt.toString())
                            .collect(Collectors.joining("|"));

                p = Pattern.compile("(?<=\\[)".concat(matchTypes).concat("(?=\\])"));
                m = p.matcher(expression);

                if (m.find())
                    this.matchType = MatchType.valueOf(m.group());
                else // Caso não tenha sido informado, ou o informado não seja válido, utiliza EQUALS
                    this.matchType = MatchType.EQ;

                // Obtém o valor
                p = Pattern.compile("(?<=:).*");
                m = p.matcher(expression);

                if (m.find())
                    this.value = m.group();
                
                // Verifica se existem mais expressões ou grupos a serem criados
                if (!query.trim().isEmpty()) {
                    if (query.startsWith("("))
                        this.next = new Group(query);
                    else
                        this.next = new Expression(query);
                }
            }

			@Override
            public Predicate decode(CriteriaBuilder cb, Root<?> from) {
                Predicate p;
                Path e = this.getPath(from, new ArrayList<String>(Arrays.asList(this.field.split("\\."))));
                
                switch (matchType) {
                    case BT : {                    	
                    	if (e.getJavaType().equals(Long.class)) {
                    		Long v1 = Long.valueOf(this.value.split("-")[0]);
                    		Long v2 = Long.valueOf(this.value.split("-")[1]);
                    		p = cb.between(e, v1, v2);
                    	} else
                    		p = cb.between(e, this.value.split("-")[0], this.value.split("-")[1]);
                    		 
                    	break;
                    }
                    case CT : p = cb.like(e, "%".concat(this.value).concat("%")); break;
                    case EQ : p = cb.equal(e, this.value); break;
                    case GT : p = cb.gt(e, new BigDecimal(this.value)); break;
                    case GTE: p = cb.greaterThanOrEqualTo(e, new BigDecimal(this.value)); break;
                    case LT : p = cb.le(e, new BigDecimal(this.value)); break;
                    case LTE: p = cb.lessThanOrEqualTo(e, new BigDecimal(this.value)); break;
                    case IN : p = e.in(Arrays.asList(this.value.split(","))); break;
                    default : p = cb.equal(e, this.value);
                }
                
                if (next != null)
                	switch (logicalOperator) {
                		case AND: p = cb.and(p, next.decode(cb, from)); break;
                		case OR : p = cb.or( p, next.decode(cb, from)); break;
                	}
                
                return p;
            }
            
            private Path<?> getPath(Path<?> parent, List<String> props) {

                String prop = props.remove(0);

                Path<?> path = parent.get(prop);
            	
            	if (!props.isEmpty())
            		path = getPath(path, props);
            	
            	return path;
            }

            @Override
            public LogicalOperator getLogicalOperator() {
                return this.logicalOperator;
            }

            @Override
            public Decoder next() {
                return this.next;
            }
            
            @Override
            public String toString() {
                String result = String.format("%s[%s]:%s", field, matchType, value);
                
                if (next != null) {
                    String model;
                    
                    if (next instanceof QueryDecoder.Group)
                        model = " %s (%s)"; 
                    else
                        model = " %s %s";
                    
                    result = result.concat(String.format(model, logicalOperator, next));
                }
                
                return result;
            }
        }
    }
    
    private enum MatchType {
        EQ,
		CT,
		LT,
		LTE,
		GT,
		GTE,
		BT,
		IN;
    }
    
    private enum LogicalOperator {
        AND,
        OR
    }

}
