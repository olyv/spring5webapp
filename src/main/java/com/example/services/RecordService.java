package com.example.services;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecordService {

    private final JdbcTemplate jdbc;

    public RecordService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 🔄 Unchecked exceptions roll back (count() == 0):
     * By default, Spring only rolls back on RuntimeException and Error.
     * It treats unchecked exceptions as unexpected system failures,
     * so it aborts the transaction and discards the INSERT.
     */
    @Transactional
    public void saveAndThrowUnchecked(String value) {
        jdbc.update("INSERT INTO records(val) VALUES (?)", value);
        throw new RuntimeException("Unchecked failure");
    }

    /**
     * ✅ Checked exceptions commit (count() == 1):
     * Following legacy EJB conventions, Spring treats checked exceptions
     * as recoverable business outcomes rather than fatal errors.
     * When saveAndThrowChecked throws Exception,
     * the proxy commits the INSERT before letting the exception propagate out.
     */
    @Transactional
    public void saveAndThrowChecked(String value) throws Exception {
        jdbc.update("INSERT INTO records(val) VALUES (?)", value);
        throw new Exception("Checked failure");
    }

    /**
     * 🔄 rollbackFor = Exception.class: Overrides Spring's default rule for checked exceptions,
     * forcing a rollback so service.count() stays 0.
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAndRollbackOnChecked(String value) throws Exception {
        jdbc.update("INSERT INTO records(val) VALUES (?)", value);
        throw new Exception("Checked failure with rollback");
    }

    /**
     * ✅ noRollbackFor = IllegalArgumentException.class: Overrides Spring's default rule for unchecked exceptions,
     * allowing the INSERT to commit so service.count() becomes 1.
     */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void saveAndCommitOnUnchecked(String value) {
        jdbc.update("INSERT INTO records(val) VALUES (?)", value);
        throw new IllegalArgumentException("Unchecked failure without rollback");
    }

    public void callPrivateTransactionalMethod(String value) {
        saveAndThrowFromPrivateMethod(value);
    }

    @Transactional
    public void callRequiresNewMethodAndThrow(String outerValue, String innerValue) {
        jdbc.update("INSERT INTO records(val) VALUES (?)", outerValue);
        saveInNewTransaction(innerValue);
        throw new RuntimeException("Outer transaction fails");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveInNewTransaction(String value) {
        jdbc.update("INSERT INTO records(val) VALUES (?)", value);
    }

    @Transactional
    private void saveAndThrowFromPrivateMethod(String value) {
        jdbc.update("INSERT INTO records(val) VALUES (?)", value);
        throw new RuntimeException("Private method failure");
    }

    public int count() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM records", Integer.class);
    }
}
