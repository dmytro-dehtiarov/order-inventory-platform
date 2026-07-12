# Полная архитектура order-inventory-platform (design-only)

## Context

Целостный архитектурный план всех модулей (catalog, identity, inventory, orders, shared) и правил
их взаимодействия — зафиксирован до начала имплементации, чтобы модули не проектировались
изолированно и не разошлись в паттернах. Документ — единый источник истины по архитектуре модулей;
детализирует `docs/architecture.md` и опирается на ADR-001–ADR-003.

Принятые решения:
- **Auth: stateless JWT** (Bearer-токен, без серверных сессий) — см. ADR-003.
- **Snapshot-паттерн в orders**: `OrderItem` хранит копии `productName`/`unitPrice` на момент заказа — отдельный архитектурный принцип.
- **Product delete = soft delete** (флаг `active`) — физического удаления продуктов в API нет.
- Catalog: иерархия категорий через self-referencing `parent_id`; удаление категории с зависимыми → `409 Conflict` (без каскада); пагинация/сортировка на list-эндпоинтах с самого начала.

Предварительные допущения catalog (могут быть пересмотрены по мере имплементации): `Product.category` обязателен (`NOT NULL`); без unique-констрейнтов на `name` в MVP; update-семантика — full-replace (`PUT`), `PATCH` можно добавить позже.

---

## 1. Правила межмодульного взаимодействия (фундамент)

**Прямые in-process вызовы application-сервисов, НЕ события.** Обоснование:
- Ключевой инвариант (architecture.md §7): создание заказа и резервирование инвентаря — **одна транзакция**. Прямой вызов `orders → inventory` внутри одного `@Transactional` даёт это бесплатно (один datasource, propagation `REQUIRED` — вложенный сервис присоединяется к внешней транзакции). Async-события ломают атомарность; `@TransactionalEventListener(BEFORE_COMMIT)` дал бы ту же транзакцию, но ценой косвенности без выгоды — в одном процессе нет сетевой границы, ради которой события вводят.
- События оставить как будущий паттерн для audit-модуля (fire-and-forget after-commit), где атомарность с бизнес-транзакцией как раз не нужна.

**Правила границ (dependency rules):**
- Кросс-модульный доступ — **только через application-сервисы** чужого модуля; `domain` и `infrastructure` (entities, repositories) — приватны для модуля.
- **Никаких JPA-ассоциаций между модулями**: `InventoryItem.productId` и `OrderItem.productId` — просто `Long`, не `@ManyToOne Product`. FK-констрейнты в БД остаются (целостность), но объектной навигации через границу нет — иначе модуль не извлекаем и schema-ownership фиктивен.
- Через границу возвращаются **read-model DTO, не entities** (например, catalog отдаёт `ProductSummary(id, name, price, active)`).
- Граф зависимостей (ацикличен): `orders → catalog`, `orders → inventory`; catalog и inventory ни от кого не зависят; identity ни от кого не зависит и никем не вызывается напрямую (только через Spring Security context); shared ни от кого не зависит, все зависят от shared.
- Сервисы, вызываемые из чужой транзакции, **не используют `REQUIRES_NEW`** — это правило, нарушение которого молча ломает атомарность.

**Транзакция создания заказа (кто держит границу):** `OrderService.createOrder` — единственный владелец `@Transactional`:
1. catalog `ProductService.getSummaries(ids)` — валидация существования/активности, текущие цена+имя для снапшота;
2. inventory `InventoryReservationService.reserve(lines)` — присоединяется к транзакции;
3. persist Order + OrderItems.
Любое исключение (включая `InsufficientStockException`) откатывает всё. `cancelOrder` симметрично: переход статуса + `release(lines)` в одной транзакции.

**Snapshot-принцип:** `OrderItem.productName` и `unitPrice` — копии на момент создания заказа. Обоснование: историческая целостность (изменение цены/имени в catalog не искажает задним числом оформленные заказы), согласованность с soft-delete (заказ на архивированный продукт остаётся читаемым без join к «живому» каталогу), независимость будущего reporting от текущего состояния catalog.

## 2. Модули

Пакетная схема едина для всех модулей: `com.dmytro.orderinventoryplatform.<module>.{api,application,domain,infrastructure}`.

### catalog

**Domain (`catalog.domain`):**

| Класс | Ответственность |
|---|---|
| `Category` | JPA-entity: `id`, `name`, `description`, self-referencing `parentCategory` (`@ManyToOne`, nullable), `createdAt`/`updatedAt` (`@CreationTimestamp`/`@UpdateTimestamp`). |
| `Product` | JPA-entity: `id`, `name`, `description`, `price` (`BigDecimal`), `category` (`@ManyToOne`, required), **`active` (boolean, default true)**, `createdAt`/`updatedAt`. |
| `CategoryNotFoundException` / `ProductNotFoundException` | Lookup по id не нашёл строку; наследуют `shared.domain.ResourceNotFoundException` (→ 404). |
| `CategoryInUseException` | Удаление категории с дочерними категориями или продуктами — инвариант «no orphaning»; наследует `ConflictException` (→ 409). |
| `CategoryCycleException` | `parentCategoryId` сделал бы категорию собственным предком — инвариант «hierarchy acyclic»; наследует `ConflictException` (→ 409). |

Entities без Bean Validation и без Spring/repository-awareness — только данные + инварианты, верные независимо от способа конструирования (rationale — раздел 4, «валидация по слоям»).

**Infrastructure (`catalog.infrastructure`):**

| Класс | Ответственность |
|---|---|
| `CategoryRepository` | `JpaRepository<Category, Long>` + `existsByParentId(Long)` (delete-guard) + `findAll(Pageable)`. |
| `ProductRepository` | `JpaRepository<Product, Long>` + `existsByCategoryId(Long)` (delete-guard) + выборка с фильтром по `active` для листинга. |

Кастомных реализаций нет — Spring Data выводит запросы из имён методов.

**Application (`catalog.application`):**

| Класс | Ответственность |
|---|---|
| `CategoryService` | create (валидация существования parent + ацикличности), get-by-id, list (paginated), update, delete (guard: `existsByParentId` / `existsByCategoryId` → `CategoryInUseException`). `@Transactional` здесь. |
| `ProductService` | create/update (валидация `categoryId` через `CategoryRepository`), get-by-id, list (paginated, **по умолчанию только `active=true`**), **deactivate вместо delete** (`active=false`; повторная деактивация идемпотентна). Плюс кросс-модульный read-API: `getSummaries(List<Long> ids) → List<ProductSummary>` для orders. |

Интерфейсы сервисов не вводятся — по одной реализации, интерфейс был бы церемонией (architecture.md требует только владения транзакциями и оркестрацией).

**API (`catalog.api`):**

| Класс | Ответственность |
|---|---|
| `CategoryController` | `/api/v1/catalog/categories` — `POST`, `GET /{id}`, `GET` (paginated), `PUT /{id}`, `DELETE /{id}` (hard delete c 409-guard). |
| `ProductController` | `/api/v1/catalog/products` — `POST`, `GET /{id}`, `GET` (paginated), `PUT /{id}`; физического `DELETE` **нет** — вместо него **`PATCH /{id}/deactivate`** (204; продукт остаётся читаемым по id с `active=false`, из листинга по умолчанию исчезает). |
| `CategoryRequest`/`CategoryResponse`, `ProductRequest`/`ProductResponse` | Java-records; request несёт Bean Validation (`@NotBlank name`, `@Size`, `@NotNull @DecimalMin("0.0") price`, `@NotNull categoryId`); `ProductResponse` включает `active`. |
| `CategoryMapper` / `ProductMapper` | Ручные `@Component`-мапперы Entity ↔ DTO. |

Call flow: `Controller → Mapper → Service → Repository(-ies) → Mapper`; `ProductService` читает `CategoryRepository` для FK-валидации — единственная кросс-сущностная зависимость, целиком внутри модуля catalog.

### identity

- **Domain**: `User` (id, username, passwordHash, `role` — **enum ADMIN/MANAGER колонкой + CHECK**, не отдельная таблица ролей: две статические роли не оправдывают join-таблицу; отступление от "users/roles" в architecture.md — флагуется), enabled, timestamps. `UserNotFoundException`, `UsernameAlreadyExistsException` (409).
- **Infrastructure**: `UserRepository`; `SecurityConfig` (SecurityFilterChain: `/api/v1/auth/**` permitAll, остальное authenticated + JWT-фильтр); JWT через **Spring Security oauth2-resource-server (Nimbus, symmetric key из env)** — идиоматично для Spring, без сторонней jjwt; `BCryptPasswordEncoder`.
- **Application**: `AuthService.login(username, password) → token`; `UserService` (CRUD пользователей, ADMIN-only).
- **API**: `AuthController` (`POST /api/v1/auth/login`), `UserController` (`/api/v1/identity/users`).
- Новые зависимости: `spring-boot-starter-security`, `spring-boot-starter-oauth2-resource-server` (+ test).

**Граница авторизации:** аутентификация — JWT-фильтр (middleware, заполняет SecurityContext); coarse-grained — route-правила в SecurityFilterChain; fine-grained — `@PreAuthorize("hasRole(...)")` **на application-сервисах** (architecture.md §4: "authorization checks at the use-case level"; защищает и будущие не-HTTP входы). Ролевая матрица: ADMIN — всё + управление пользователями; MANAGER — CRUD catalog, корректировка inventory, create/cancel orders; чтение — обе роли.

### inventory

- **Domain**: `InventoryItem` (id, `productId Long` unique, available, reserved, timestamps) с методами `reserve(qty)`/`release(qty)`/`adjust(qty)`, бросающими `InsufficientStockException` — инварианты `available >= 0`, `reserved >= 0` живут в домене + CHECK-констрейнты в БД как backstop. `InventoryItemNotFoundException`.
- **Infrastructure**: `InventoryItemRepository` + метод с `@Lock(PESSIMISTIC_WRITE)` для выборки по списку productId **с сортировкой по productId** (детерминированный порядок захвата блокировок = защита от deadlock при конкурирующих заказах). Пессимистичная блокировка, не optimistic-version: резервирование — короткая горячая операция, retry-loop оптимистики сложнее и хуже под контеншном.
- **Application**: `InventoryService` (просмотр/корректировка остатков — публичный API модуля для REST), `InventoryReservationService` (`reserve`/`release` — публичный API для orders).
- **API**: `InventoryController` `/api/v1/inventory/items` (список/по продукту/корректировка).

### orders

- **Domain**: `Order` (id, `status` enum CREATED/CANCELLED + метод `cancel()` со state-machine-проверкой, customerName/customerEmail — снапшот-поля, вводимые менеджером (сущности Customer нет — минимальный скоуп под роли ADMIN/MANAGER; при необходимости выделяется в отдельный справочник позже), `createdBy` (userId Long), totalAmount, timestamps); `OrderItem` (id, order FK, productId Long, productName, unitPrice, quantity, lineTotal). `OrderNotFoundException`, `InvalidOrderStateException` (409), `EmptyOrderException`/`InactiveProductException` (400/409).
- **Application**: `OrderService` — транзакционные границы (раздел 1).
- **API**: `OrderController` `/api/v1/orders` — `POST`, `GET /{id}`, `GET` (paginated), **`POST /{id}/cancel`** (action-endpoint, не DELETE: отмена — бизнес-переход статуса с сайд-эффектом release, а не удаление ресурса).

### shared

- `shared.domain`: базовые исключения `ResourceNotFoundException` (→ 404), `ConflictException` (→ 409) — модульные исключения их расширяют, чтобы shared-хендлер маппил по базовому типу и **не зависел от модулей**.
- `shared.api`: `GlobalExceptionHandler` extends `ResponseEntityExceptionHandler`: `ResourceNotFoundException` → 404 `ProblemDetail`, `ConflictException` → 409 `ProblemDetail`, `MethodArgumentNotValidException` → 400 `ProblemDetail`. Spring-овский `ProblemDetail` (RFC 7807) вместо самодельного error-DTO — architecture.md §8 просит "problem-like response", Spring Boot уже поставляет ровно этот тип.

## 3. Порядок реализации и миграции

1. **shared + catalog** — нет зависимостей.
   - `V1__create_categories_table.sql`: `categories` (`id NUMBER(19) GENERATED BY DEFAULT AS IDENTITY PK`, `name VARCHAR2(150) NOT NULL`, `description VARCHAR2(1000)`, `parent_id NUMBER(19) REFERENCES categories(id)`, timestamps) + индекс на `parent_id`.
   - `V2__create_products_table.sql`: `products` (`id IDENTITY PK`, `name VARCHAR2(200) NOT NULL`, `description VARCHAR2(2000)`, `price NUMBER(12,2) NOT NULL CHECK (price >= 0)`, `category_id NUMBER(19) NOT NULL REFERENCES categories(id)`, **`active NUMBER(1) DEFAULT 1 NOT NULL CHECK (active IN (0,1))`**, timestamps) + индекс на `category_id`. Две миграции, не одна: FK `products → categories` требует порядка V1 → V2. `IDENTITY`-колонки (не sequence+trigger) — Oracle 12c+ поддерживает нативно.
2. **inventory** — `V3__create_inventory_items_table.sql` (FK на `products`, CHECK `available >= 0`, `reserved >= 0`, unique на `product_id`).
3. **orders** — `V4__create_orders_table.sql`, `V5__create_order_items_table.sql` (FK на `orders` и `products`; снапшот-колонки `product_name`, `unit_price`). Ядро системы с главными инвариантами; требует catalog и inventory.
4. **identity + прогон авторизации по всем модулям** — `V6__create_users_table.sql`. Последним: домены разрабатываются без auth-трения, security включается одним проходом по стабильной поверхности API. Смягчение: тестовый security-config/`@WithMockUser` закладывается в общий тестовый базовый класс заранее, чтобы включение security не переломало тесты.

Flyway-нумерация = порядок реализации (V1–V6).

## 4. Сквозные паттерны

- **Ошибки**: ProblemDetail (RFC 7807) через единый `GlobalExceptionHandler` — системный стандарт для всех модулей.
- **Валидация по слоям** (принцип, заложенный в catalog, — распространяется на все модули):
  - *shape-валидация* (`@NotBlank`, `@DecimalMin`…) — на request-DTO в API-слое: это правила REST-контракта; на entity они бы ошибочно связали загрузку из БД с правилами HTTP-входа;
  - *referential/бизнес-проверки, требующие БД* (FK существует? хватает остатка? статус позволяет переход?) — в application-слое, бросаются доменными исключениями;
  - *всегда-истинные доменные инварианты без БД* (`price >= 0`, `available >= 0`) — в domain-слое как defense-in-depth backstop (+ CHECK в БД); первую линию для пользователя всё равно держит API-слой (чистый 400 вместо доменного исключения).
- **DTO отделены от entities** (все модули): стабильность контракта независимо от persistence-графа; защита от lazy-loading/рекурсии при сериализации JPA-ассоциаций (DTO плоско отдаёт `parentCategoryId: Long`); это ровно то, что architecture.md §4 закрепляет за API-слоем.
- **DTO/мапперы**: ручные `@Component`-мапперы. ~8 сущностей на MVP не оправдывают MapStruct (annotation processor, магия в билде); пересмотреть, если мапперов станет >10 или пойдёт churn.
- **Пагинация**: `Pageable` (`@PageableDefault(size = 20)`) + возврат `PagedModel<T>` (не сырой `Page` — у `PagedModel` стабильная документированная JSON-форма, сериализация `PageImpl` явно не рекомендована Spring Data) — на все list-эндпоинты.
- **Семантика 4xx**: `400` — неверная форма запроса (Bean Validation); `404` — ресурс не найден; `409` — попытка привести данные в состояние, противоречащее *существующим* данным (delete-guard, цикл в иерархии, нехватка остатка, неверный переход статуса).
- **Тесты**: единый абстрактный базовый класс `AbstractIntegrationTest` в test-sources (`@SpringBootTest`, профиль `test` → тестовая схема Oracle VM per ADR-002; изоляция — `@Transactional`-rollback на тест + Flyway clean/migrate на прогон). Все модульные IT наследуются — сетап не дублируется. Юнит-тесты — на инварианты сервисов (reserve/release, status-переходы, delete-guard, ацикличность) без Spring-контекста.

## 5. Согласованность с остальной документацией

Дизайн согласован с `docs/architecture.md` и ADR-001–ADR-003. Отдельно стоит подчеркнуть две
намеренные связки:

- **Тестовая стратегия следует ADR-002**: интеграционные тесты идут против выделенной тестовой схемы на Oracle VM, не через Testcontainers (architecture.md §9).
- **Мутабельная цена продукта в catalog допустима только в паре со snapshot-принципом в orders** (раздел 1) — если snapshot-паттерн когда-либо пересматривается, политику изменения цен нужно пересматривать вместе с ним.

## Отложено (вне MVP)

- Tree-эндпоинт для категорий («дерево»/«дети категории») — добавляется, когда nesting реально используется; для базового CRUD достаточно плоского листинга с `parentCategoryId`.
- Swagger/OpenAPI-зависимость — кросс-модульная задача, добавляется отдельным изменением.
- `PATCH`-семантика частичного обновления — `PUT` (full-replace) её не блокирует.
- Refresh-token flow и ротация JWT-ключа — см. follow-ups в ADR-003.
