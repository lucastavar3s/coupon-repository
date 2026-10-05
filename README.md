# Coupon API

API de cupons em Spring Boot, no padrão MVC. O projeto nasce do [Spring Initializr](https://start.spring.io/) e implementa os endpoints de cadastro, consulta e exclusão lógica.

## Stack

- Java 21
- Spring Boot 4.1 (Web MVC, Validation, Data JPA)
- Lombok
- H2 em memória
- SpringDoc OpenAPI
- Docker e Docker Compose

## Regras de negócio

O código informado pode conter caracteres especiais. Antes de gravar e de responder, a API remove tudo o que não for letra ou número. O resultado precisa ter exatamente 6 caracteres. `ABC-123` vira `ABC123`.

O desconto é um saldo absoluto, sem moeda. O mínimo é `0,5` e não há teto.

A data de expiração não pode estar no passado. Um cupom pode nascer já publicado. Sem o campo `published`, ele fica despublicado.

O status é calculado na leitura:

- `ACTIVE` na criação e enquanto a expiração não passou, publicado ou não
- `INACTIVE` quando a expiração já passou
- `DELETED` depois da exclusão lógica

A exclusão é lógica: o registro permanece no banco. Consultar o id depois da exclusão devolve o cupom com status `DELETED`. Excluir de novo responde `409`.

O controller só adapta HTTP. O service coordena a transação. As regras ficam nos objetos de domínio, e a entidade JPA só persiste o estado. O código e o desconto são objetos de valor: `CouponCode` e `DiscountValue`.

A descrição tem no máximo 4000 caracteres, o mesmo limite da coluna. Acima disso a API responde `400`, sem estourar o banco.

## Decisões

O enunciado não fecha três pontos. A API trata assim:

- O código não é único. Dois cupons podem ser cadastrados com o mesmo código.
- `published` não muda o status. O cupom nasce `ACTIVE` e só fica `INACTIVE` quando a expiração passa.
- Um cupom excluído continua consultável. O `GET` devolve os dados originais com status `DELETED`, porque a exclusão é lógica e esse status faz parte do contrato.

## Executar

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Console H2: `http://localhost:8080/h2-console` (`jdbc:h2:mem:coupon`, usuário `sa`, senha vazia)

## Docker

```bash
docker compose up --build
```

## Testes

```bash
./mvnw verify
```

`CouponTest` cobre as regras de domínio sem subir o Spring. `CouponApiIntegrationTest` sobe o contexto completo, grava no H2 e exercita o único controller `/coupon`, inclusive os status `201`, `200`, `204`, `400`, `404`, `409` e `422`. O build falha se a cobertura de linhas ficar abaixo de 80%.

## Endpoints

| Método | Caminho | Sucesso |
| --- | --- | --- |
| `POST` | `/coupon` | `201` |
| `GET` | `/coupon/{id}` | `200` |
| `DELETE` | `/coupon/{id}` | `204` |

Erros de payload respondem `400`. Regras de negócio respondem `422`. Cupom inexistente responde `404`. Segunda exclusão responde `409`.
