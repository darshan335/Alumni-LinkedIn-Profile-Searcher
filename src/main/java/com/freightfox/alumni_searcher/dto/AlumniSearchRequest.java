package com.freightfox.alumni_searcher.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class AlumniSearchRequest {

    @NotBlank(message = "University name is required")
    private String university;

    @NotBlank(message = "Designation is required")
    private String designation;

    @Min(value = 1900, message = "Passout year must be valid")
    private Integer passoutYear;
}