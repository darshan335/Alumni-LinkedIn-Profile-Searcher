package com.freightfox.alumni_searcher.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class AlumniResponse {

    private String name;

    private String currentRole;

    private String university;

    private String location;

    private String linkedinHeadline;

    private Integer passoutYear;
}