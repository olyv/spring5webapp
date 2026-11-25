# Test setup

`RecordServiceTest` uses `@SpringBootTest`, so Spring Boot starts the application
context before running the test methods. The H2 runtime dependency in `pom.xml`
allows Boot to auto-configure an embedded, in-memory `DataSource` when no
datasource URL is configured. H2 creates the in-memory database when the
`DataSource` first opens a connection; in this test, that happens when the
`JdbcTemplate` is first used.

The database and the `records` table are separate things. The test creates the
table itself in `resetTable()`, annotated with `@BeforeEach`: it drops any
existing `records` table and creates a fresh one before each test. The tests
therefore use the database supplied by Spring Boot, while their setup method
prepares the table they need.

## Proxy limitations

Spring applies `@Transactional` through a proxy around the service. Calls that
do not pass through that proxy do not get transactional behavior:

- `testTransactionalAnnotationOnPrivateMethodIsNotApplied` calls a public
  method that invokes a private `@Transactional` method. The private method
  inserts a row and throws, but the row remains because the annotation is not
  intercepted and the JDBC insert runs without a Spring transaction.
- `testSelfInvocationDoesNotApplyRequiresNewPropagation` starts a transaction
  and then calls a public `REQUIRES_NEW` method on `this`. That internal call
  bypasses the proxy, so it joins the existing transaction instead of starting
  a new one. The outer method's exception rolls back both inserts.

For comparison, calling a public `@Transactional` method from another Spring
bean passes through the proxy, allowing Spring to apply its transaction
settings, including propagation.
