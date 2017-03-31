package com.developer.drodriguez;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by Daniel on 3/31/17.
 *
 * This configuration allows URLs with dots ("."). Without this, Spring automatically removes any dots from the URL.
 *
 */
@Configuration
class MvcConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(false);
    }
}