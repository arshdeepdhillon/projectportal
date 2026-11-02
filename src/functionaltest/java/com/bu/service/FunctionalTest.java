package com.bu.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FunctionalTest
{

    @Test
    void testSample()
    {
        assertThat( true ).isEqualTo( true );
    }
}
