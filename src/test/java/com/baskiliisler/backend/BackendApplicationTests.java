package com.baskiliisler.backend;

import com.baskiliisler.backend.config.TestConfig;
import com.baskiliisler.backend.security.JwtService;
import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestConfig.class)
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	private JwtService jwtService;

	@Test
	void jwtUtilShouldBeConfigured() {
		assertThat(jwtService).isNotNull();
	}

	@Test
	void jwtTokenGenerationShouldWork() {
		User testUser = User.builder()
				.id(1L)
				.name("Test User")
				.email("test@example.com")
				.phoneNumber("+90 555 123 45 67")
				.passwordHash("hashedPassword")
				.role(Role.SUPER_ADMIN)
				.build();

		String token = jwtService.generateToken(testUser);
		var claims = jwtService.parse(token);

		assertThat(token).isNotNull();
		assertThat(claims.getSubject()).isEqualTo("1");
		assertThat(claims.get("role")).isEqualTo("SUPER_ADMIN");
	}
}
