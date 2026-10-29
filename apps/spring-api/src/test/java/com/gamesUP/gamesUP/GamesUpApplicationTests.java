package com.gamesUP.gamesUP;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GamesUpApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void connectsToTheDedicatedTestDatabase() {
		String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
		assertThat(database).isEqualTo("gamesup_test");
	}

}
