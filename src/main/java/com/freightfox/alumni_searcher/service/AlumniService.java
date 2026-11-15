
package com.freightfox.alumni_searcher.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.freightfox.alumni_searcher.client.PhantomBusterClient;
import com.freightfox.alumni_searcher.dto.AlumniResponse;
import com.freightfox.alumni_searcher.dto.AlumniSearchRequest;
import com.freightfox.alumni_searcher.dto.AlumniSearchResponse;
import com.freightfox.alumni_searcher.entity.Alumni;
import com.freightfox.alumni_searcher.repository.AlumniRepository;

@Service
public class AlumniService {

	private static final Pattern YEAR_PATTERN = Pattern.compile("(19|20)\\d{2}");

	private final ObjectMapper objectMapper;
	private final AlumniRepository alumniRepository;
	private final PhantomBusterClient phantomBusterClient;

	public AlumniService(AlumniRepository alumniRepository, PhantomBusterClient phantomBusterClient,
			ObjectMapper objectMapper) {

		this.alumniRepository = alumniRepository;
		this.phantomBusterClient = phantomBusterClient;
		this.objectMapper = objectMapper;
	}

	/**
	 * Retrieves all saved alumni records.
	 */
	@Transactional(readOnly = true)
	public List<Alumni> getAllAlumni() {
		return alumniRepository.findAll();
	}

	/**
	 * Searches PhantomBuster output and saves matching alumni.
	 */
	@Transactional
	public AlumniSearchResponse searchAlumni(AlumniSearchRequest request) {

		String json = phantomBusterClient.fetchAgentOutput();

		JsonNode profiles;

		try {
			profiles = objectMapper.readTree(json);
		} catch (JacksonException ex) {
			throw new IllegalStateException("Unable to parse PhantomBuster JSON response.", ex);
		}

		if (profiles == null || !profiles.isArray()) {
			throw new IllegalStateException("PhantomBuster response is not a valid JSON array.");
		}

		String university = request.getUniversity().trim();
		String designation = request.getDesignation().trim();

		List<Alumni> newAlumni = new ArrayList<>();

		for (JsonNode profile : profiles) {

			if (!matchesUniversity(profile, university)) {
				continue;
			}

			String currentRole = extractCurrentRole(profile);

			if (!matchesDesignation(currentRole, designation)) {
				continue;
			}

			Integer passoutYear = extractPassoutYear(getSchoolDateRange(profile, university));

			if (request.getPassoutYear() != null && !request.getPassoutYear().equals(passoutYear)) {
				continue;
			}

			String profileUrl = extractProfileUrl(profile);

			if (profileUrl.isBlank()) {
				continue;
			}

			if (alumniRepository.existsByLinkedinProfileUrl(profileUrl)) {
				continue;
			}

			Alumni alumni = mapToEntity(profile, university, currentRole, passoutYear, profileUrl);

			newAlumni.add(alumni);
		}

		List<Alumni> savedRecords = alumniRepository.saveAll(newAlumni);

		return buildSearchResponse(savedRecords);
	}

	/**
	 * Checks whether either education field matches the university.
	 */
	private boolean matchesUniversity(JsonNode profile, String university) {

		String school = getText(profile, "school");
		String school2 = getText(profile, "school2");

		return containsIgnoreCase(school, university) || containsIgnoreCase(school2, university);
	}

	/**
	 * Checks whether the current designation matches the requested role.
	 */
	private boolean matchesDesignation(String currentRole, String designation) {

		return containsIgnoreCase(currentRole, designation);
	}

	/**
	 * Extracts the current job title, falling back to the headline.
	 */
	private String extractCurrentRole(JsonNode profile) {

		String jobTitle = getText(profile, "jobTitle");

		if (!jobTitle.isBlank()) {
			return jobTitle;
		}

		return getText(profile, "headline");
	}

	/**
	 * Selects the date range associated with the matching university.
	 */
	private String getSchoolDateRange(JsonNode profile, String university) {

		String school = getText(profile, "school");

		if (containsIgnoreCase(school, university)) {
			return getText(profile, "schoolDateRange");
		}

		return getText(profile, "schoolDateRange2");
	}

	/**
	 * Extracts the last year from an education date range.
	 */
	private Integer extractPassoutYear(String dateRange) {

		if (dateRange == null || dateRange.isBlank()) {
			return null;
		}

		if (dateRange.toLowerCase(Locale.ROOT).contains("present")) {
			return null;
		}

		Matcher matcher = YEAR_PATTERN.matcher(dateRange);

		Integer lastYear = null;

		while (matcher.find()) {
			lastYear = Integer.parseInt(matcher.group());
		}

		return lastYear;
	}

	/**
	 * Extracts the LinkedIn profile URL.
	 */
	private String extractProfileUrl(JsonNode profile) {

		String linkedinUrl = getText(profile, "linkedinProfileUrl");

		if (!linkedinUrl.isBlank()) {
			return linkedinUrl.trim();
		}

		return getText(profile, "profileUrl").trim();
	}

	/**
	 * Creates an Alumni entity from a PhantomBuster profile.
	 */
	private Alumni mapToEntity(JsonNode profile, String university, String currentRole, Integer passoutYear,
			String profileUrl) {

		Alumni alumni = new Alumni();

		alumni.setName(getText(profile, "fullName"));
		alumni.setCurrentRole(currentRole);
		alumni.setUniversity(university);
		alumni.setLocation(getText(profile, "location"));
		alumni.setLinkedinHeadline(getText(profile, "headline"));
		alumni.setPassoutYear(passoutYear);
		alumni.setLinkedinProfileUrl(profileUrl);

		return alumni;
	}

	/**
	 * Converts saved Alumni entities into API response objects.
	 */
	private AlumniSearchResponse buildSearchResponse(List<Alumni> savedRecords) {

		List<AlumniResponse> responses = savedRecords.stream().map(this::mapToResponse).toList();

		AlumniSearchResponse result = new AlumniSearchResponse();

		result.setStatus("success");
		result.setData(responses);

		return result;
	}

	/**
	 * Maps an Alumni entity to its response DTO.
	 */
	private AlumniResponse mapToResponse(Alumni alumni) {

		AlumniResponse response = new AlumniResponse();

		response.setName(alumni.getName());
		response.setCurrentRole(alumni.getCurrentRole());
		response.setUniversity(alumni.getUniversity());
		response.setLocation(alumni.getLocation());
		response.setLinkedinHeadline(alumni.getLinkedinHeadline());
		response.setPassoutYear(alumni.getPassoutYear());

		return response;
	}

	/**
	 * Safely retrieves text from a JSON field.
	 */
	private String getText(JsonNode node, String fieldName) {
		return node.path(fieldName).asText("");
	}

	/**
	 * Performs case-insensitive text matching.
	 */
	private boolean containsIgnoreCase(String value, String searchText) {

		return value != null && searchText != null
				&& value.toLowerCase(Locale.ROOT).contains(searchText.toLowerCase(Locale.ROOT));
	}
}
