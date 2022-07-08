package com.lgyar.likegivingyourselfaraise;

import com.lgyar.domain.User;
import com.lgyar.domain.UserRole;
import com.lgyar.repositories.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@DataMongoTest
@RunWith(SpringRunner.class)
class UserDatabaseTests {
    @Autowired
    UserRepository repository;

    @Before
    public void setup() {
        repository.deleteAll();
    }

    @After
    public void tearDown() {
        repository.deleteAll();
    }

    @Test
    void userInsertedInDB_canBeRetrieved() {
        String username = "john";
        User user = new User(username, "passHash", UserRole.ROLE_USER, null, null);
        repository.save(user);

        Optional<User> retrieved = repository.findById(username);
        if (retrieved.isPresent()) {
            String retrievedUsername = retrieved.get().getUsername();
            assertEquals(username, retrievedUsername);
        }
        else {
            fail();
        }
    }
}
