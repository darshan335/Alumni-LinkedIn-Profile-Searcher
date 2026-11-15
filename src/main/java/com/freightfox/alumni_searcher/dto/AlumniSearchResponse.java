package com.freightfox.alumni_searcher.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class AlumniSearchResponse {

    private String status;

    private List<AlumniResponse> data;
}