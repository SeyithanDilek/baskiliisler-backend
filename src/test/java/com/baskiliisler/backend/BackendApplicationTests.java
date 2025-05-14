package com.baskiliisler.backend;

import com.baskiliisler.backend.config.JwtUtil;
import com.baskiliisler.backend.common.Role;
import com.baskiliisler.backend.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@SpringBootTest
@ActiveProfiles("default")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class BackendApplicationTests {

	@Autowired
	private JwtUtil jwtUtil;

	@Test
	@DisplayName("Uygulama context'i başarıyla yükleniyor")
	void contextLoads() {
		assertThat(jwtUtil).isNotNull();
	}

	@Test
	@DisplayName("JWT token üretimi ve doğrulaması çalışıyor")
	void jwtTokenGenerationAndValidation() {
		// given
		User testUser = User.builder()
				.id(1L)
				.email("test@example.com")
				.name("Test User")
				.role(Role.REP)
				.build();

		// when
		String token = jwtUtil.generateToken(testUser);
		var claims = jwtUtil.parse(token);

		// then
		assertThat(claims).isNotNull();
		assertThat(claims.getSubject()).isEqualTo(testUser.getId().toString());
		assertThat(claims.get("role", String.class)).isEqualTo(testUser.getRole().name());
	}

}
