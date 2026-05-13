package com.cobip.infra.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

	@Bean
	@ConditionalOnExpression("'${cloud.aws.region.static:}' != ''")
	public S3Client s3Client(
		@Value("${cloud.aws.region.static}") String region,
		@Value("${cloud.aws.credentials.access-key:}") String accessKey,
		@Value("${cloud.aws.credentials.secret-key:}") String secretKey
	) {
		return S3Client.builder()
			.region(Region.of(region))
			.credentialsProvider(credentialsProvider(accessKey, secretKey))
			.build();
	}

	AwsCredentialsProvider credentialsProvider(String accessKey, String secretKey) {
		if (hasText(accessKey) && hasText(secretKey)) {
			return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
		}
		return DefaultCredentialsProvider.create();
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
