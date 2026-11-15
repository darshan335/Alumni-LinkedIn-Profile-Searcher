
package com.freightfox.alumni_searcher.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tools.jackson.databind.ObjectMapper;

import com.freightfox.alumni_searcher.client.PhantomBusterClient;
import com.freightfox.alumni_searcher.dto.AlumniSearchRequest;
import com.freightfox.alumni_searcher.dto.AlumniSearchResponse;
import com.freightfox.alumni_searcher.entity.Alumni;
import com.freightfox.alumni_searcher.repository.AlumniRepository;

@ExtendWith(MockitoExtension.class)
class AlumniServiceTest {

	@Mock
	private AlumniRepository alumniRepository;

	@Mock
	private PhantomBusterClient phantomBusterClient;

	private AlumniService alumniService;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		alumniService = new AlumniService(alumniRepository, phantomBusterClient, objectMapper);
	}

	@Test
	void shouldSaveMatchingAlumni() {
		String json = """
				[
				  {
				    "fullName": "Test User",
				    "jobTitle": "Software Engineer",
				    "headline": "Software Engineer",
				    "school": "Sambhram Institute of Technology",
				    "schoolDateRange": "2016 - 2020",
				    "location": "Bengaluru",
				    "linkedinProfileUrl": "https://www.linkedin.com/in/test-user"
				  }
				]
				""";

		when(phantomBusterClient.fetchAgentOutput()).thenReturn(json);
		when(alumniRepository.existsByLinkedinProfileUrl(anyString())).thenReturn(false);
		when(alumniRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

		AlumniSearchRequest request = new AlumniSearchRequest();
		request.setUniversity("Sambhram Institute of Technology");
		request.setDesignation("Software Engineer");

		AlumniSearchResponse response = alumniService.searchAlumni(request);

		assertEquals("success", response.getStatus());
		assertEquals(1, response.getData().size());
		assertEquals("Test User", response.getData().get(0).getName());
		assertEquals(2020, response.getData().get(0).getPassoutYear());

		verify(alumniRepository).saveAll(anyList());
	}

	@Test
	void shouldSkipExistingAlumni() {
		String json = """
				[
				  {
				    "fullName": "Test User",
				    "jobTitle": "Software Engineer",
				    "headline": "Software Engineer",
				    "school": "Sambhram Institute of Technology",
				    "schoolDateRange": "2016 - 2020",
				    "linkedinProfileUrl": "https://www.linkedin.com/in/test-user"
				  }
				]
				""";

		when(phantomBusterClient.fetchAgentOutput()).thenReturn(json);
		when(alumniRepository.existsByLinkedinProfileUrl(anyString())).thenReturn(true);
		when(alumniRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

		AlumniSearchRequest request = new AlumniSearchRequest();
		request.setUniversity("Sambhram Institute of Technology");
		request.setDesignation("Software Engineer");

		AlumniSearchResponse response = alumniService.searchAlumni(request);

		assertEquals("success", response.getStatus());
		assertTrue(response.getData().isEmpty());

		verify(alumniRepository).saveAll(anyList());
	}

	@Test
	void shouldFilterByPassoutYear() {
		String json = """
				[
				  {
				    "fullName": "Test User",
				    "jobTitle": "Software Engineer",
				    "headline": "Software Engineer",
				    "school": "Sambhram Institute of Technology",
				    "schoolDateRange": "2016 - 2020",
				    "linkedinProfileUrl": "https://www.linkedin.com/in/test-user"
				  }
				]
				""";

		when(phantomBusterClient.fetchAgentOutput()).thenReturn(json);
		when(alumniRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

		AlumniSearchRequest request = new AlumniSearchRequest();
		request.setUniversity("Sambhram Institute of Technology");
		request.setDesignation("Software Engineer");
		request.setPassoutYear(2022);

		AlumniSearchResponse response = alumniService.searchAlumni(request);

		assertTrue(response.getData().isEmpty());
		verify(alumniRepository).saveAll(anyList());
	}

	@Test
	void shouldRejectInvalidPhantomBusterResponse() {
		when(phantomBusterClient.fetchAgentOutput()).thenReturn("{}");

		AlumniSearchRequest request = new AlumniSearchRequest();
		request.setUniversity("Sambhram Institute of Technology");
		request.setDesignation("Software Engineer");

		assertThrows(RuntimeException.class, () -> alumniService.searchAlumni(request));

		verify(alumniRepository, never()).saveAll(anyList());
	}

	@Test
	void shouldReturnAllSavedAlumni() {
		when(alumniRepository.findAll()).thenReturn(List.of(new Alumni()));

		List<Alumni> result = alumniService.getAllAlumni();

		assertEquals(1, result.size());
		verify(alumniRepository).findAll();
	}
}
