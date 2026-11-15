
package com.freightfox.alumni_searcher.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.freightfox.alumni_searcher.dto.AlumniSearchRequest;
import com.freightfox.alumni_searcher.dto.AlumniSearchResponse;
import com.freightfox.alumni_searcher.entity.Alumni;
import com.freightfox.alumni_searcher.service.AlumniService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/alumni")
public class AlumniController {

	private final AlumniService alumniService;

	public AlumniController(AlumniService alumniService) {
		this.alumniService = alumniService;
	}

	@GetMapping("/all")
	public List<Alumni> getAllAlumni() {
		return alumniService.getAllAlumni();
	}

	@PostMapping("/search")
	public AlumniSearchResponse searchAlumni(@Valid @RequestBody AlumniSearchRequest request) {
		return alumniService.searchAlumni(request);
	}
}
