package com.freightfox.alumni_searcher.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.freightfox.alumni_searcher.entity.Alumni;

public interface AlumniRepository extends JpaRepository<Alumni, Long> {
	List<Alumni> findByUniversityAndCurrentRole(String university, String currentRole);

	List<Alumni> findByUniversityAndCurrentRoleAndPassoutYear(String university, String currentRole,
			Integer passoutYear);

	boolean existsByLinkedinProfileUrl(String linkedinProfileUrl);
}