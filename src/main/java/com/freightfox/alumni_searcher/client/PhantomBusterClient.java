
package com.freightfox.alumni_searcher.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PhantomBusterClient {

	private static final Pattern RESULT_URL_PATTERN = Pattern.compile("https://[^\\s\\\"\\\\]+result\\.json");

	private final RestClient restClient;
	private final RestClient downloadClient;

	@Value("${phantombuster.agent-id}")
	private String agentId;

	public PhantomBusterClient(RestClient restClient) {
		this.restClient = restClient;

		// Separate client without the PhantomBuster API key header.
		this.downloadClient = RestClient.create();
	}

	/**
	 * Fetches the latest PhantomBuster agent output.
	 */
	public String fetchAgentOutput() {

		try {
			String response = restClient.get()
					.uri(uriBuilder -> uriBuilder.path("/agents/fetch-output").queryParam("id", agentId).build())
					.retrieve().body(String.class);

			if (response == null || response.isBlank()) {
				throw new IllegalStateException("PhantomBuster returned an empty response.");
			}

			String resultUrl = extractResultUrl(response);

			return downloadResult(resultUrl);

		} catch (RestClientException ex) {
			throw new IllegalStateException("Failed to communicate with PhantomBuster.", ex);
		}
	}

	/**
	 * Extracts the JSON result URL from PhantomBuster's response.
	 */
	private String extractResultUrl(String response) {

		Matcher matcher = RESULT_URL_PATTERN.matcher(response);

		if (!matcher.find()) {
			throw new IllegalStateException("PhantomBuster JSON result URL not found.");
		}

		return matcher.group();
	}

	/**
	 * Downloads the JSON result using a separate HTTP client.
	 */
	private String downloadResult(String resultUrl) {

		try {
			String result = downloadClient.get().uri(resultUrl).retrieve().body(String.class);

			if (result == null || result.isBlank()) {
				throw new IllegalStateException("PhantomBuster result file is empty.");
			}

			return result;

		} catch (RestClientException ex) {
			throw new IllegalStateException("Failed to download PhantomBuster result.", ex);
		}
	}
}
