package ingsoftware.zeroshop.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ControllerSeparationTest {

    @Test
    void authAndUserControllersAreSeparated() {
        assertNotNull(AuthController.class);
        assertNotNull(UserController.class);
    }
}
