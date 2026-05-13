package com.cobip.infra.aws;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

class S3ConfigTest {

    private final S3Config s3Config = new S3Config();

    @Test
    void credentialsProviderUsesStaticCredentialsWhenAccessKeyAndSecretKeyAreProvided() {
        assertThat(s3Config.credentialsProvider("access-key", "secret-key"))
                .isInstanceOf(StaticCredentialsProvider.class);
    }

    @Test
    void credentialsProviderUsesDefaultCredentialsWhenStaticCredentialsAreMissing() {
        assertThat(s3Config.credentialsProvider("", ""))
                .isInstanceOf(DefaultCredentialsProvider.class);
    }
}
