
package com.freightfox.alumni_searcher.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alumni_profiles", uniqueConstraints = {
		@UniqueConstraint(name = "uk_alumni_linkedin_url", columnNames = "linkedin_profile_url") })
@Getter
@Setter
@NoArgsConstructor
public class Alumni {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(name = "current_designation")
	private String currentRole;

	private String university;
	private String location;

	@Column(length = 1000)
	private String linkedinHeadline;

	private Integer passoutYear;

	@Column(name = "linkedin_profile_url", unique = true)
	private String linkedinProfileUrl;
}
