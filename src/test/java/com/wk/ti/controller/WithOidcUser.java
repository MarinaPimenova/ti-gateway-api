package com.wk.ti.controller;

import org.springframework.core.annotation.AliasFor;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.*;

import static com.wk.ti.security.service.UserDetailExtractorStrategy.USER_ROLE;

@SuppressWarnings("unused")
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@WithSecurityContext(factory = WithOidcUserSecurityContextFactory.class)
public @interface WithOidcUser {

    String email() default "john.doe@test.com";

    String givenName() default "John";

    String familyName() default "Doe";

    String subject() default "google";

    String[] authorities() default {USER_ROLE};

    @AliasFor(annotation = WithSecurityContext.class)
    TestExecutionEvent setupBefore() default TestExecutionEvent.TEST_METHOD;
}