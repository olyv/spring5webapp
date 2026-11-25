package com.example.services;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RecordServiceTest {

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    RecordService service;

    @BeforeEach
    void resetTable() {
        jdbc.execute("DROP TABLE IF EXISTS records");
        jdbc.execute("CREATE TABLE records(val VARCHAR(50))");
    }

    @Test
    void testUncheckedException() {
        assertThrows(RuntimeException.class, () -> service.saveAndThrowUnchecked("A"));
        System.out.println("Count after unchecked: " + service.count());
    }

    @Test
    void testCheckedException() {
        assertThrows(Exception.class, () -> service.saveAndThrowChecked("B"));
        System.out.println("Count after checked: " + service.count());
    }

    @Test
    void testRollbackForCheckedException() {
        assertThrows(Exception.class, () -> service.saveAndRollbackOnChecked("C"));
        assertEquals(0, service.count());
    }

    @Test
    void testNoRollbackForUncheckedException() {
        assertThrows(IllegalArgumentException.class, () -> service.saveAndCommitOnUnchecked("D"));
        assertEquals(1, service.count());
    }

    @Test
    void testTransactionalAnnotationOnPrivateMethodIsNotApplied() {
        assertThrows(RuntimeException.class, () -> service.callPrivateTransactionalMethod("E"));
        assertEquals(1, service.count());
    }

    @Test
    void testSelfInvocationDoesNotApplyRequiresNewPropagation() {
        assertThrows(RuntimeException.class,
                () -> service.callRequiresNewMethodAndThrow("outer", "inner"));
        assertEquals(0, service.count());
    }
}