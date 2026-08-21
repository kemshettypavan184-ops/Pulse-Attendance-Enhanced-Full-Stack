package com.pulse.attendance.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
