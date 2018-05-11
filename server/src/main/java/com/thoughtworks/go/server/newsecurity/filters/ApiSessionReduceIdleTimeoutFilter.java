/*
 * Copyright 2018 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.go.server.newsecurity.filters;

import com.thoughtworks.go.util.SystemEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/* API requests should not start long-lived session. */
@Component
public class ApiSessionReduceIdleTimeoutFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiSessionReduceIdleTimeoutFilter.class);
    private final int maxInactiveInterval;

    @Autowired
    public ApiSessionReduceIdleTimeoutFilter(SystemEnvironment systemEnvironment) {
        maxInactiveInterval = systemEnvironment.get(SystemEnvironment.API_REQUEST_IDLE_TIMEOUT_IN_SECONDS);
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        maybeSetSessionIdleTimeout(request, response, chain);
    }

    private void maybeSetSessionIdleTimeout(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain) throws IOException, ServletException {
        boolean hadNoSessionBeforeStarting = request.getSession(false) == null;
        try {
            chain.doFilter(request, response);
        } finally {
            HttpSession session = request.getSession(false);
            boolean hasSessionNow = session != null;

            if (hadNoSessionBeforeStarting && hasSessionNow) {
                LOGGER.debug("Setting max inactive interval for request: {} to {}.", request.getRequestURI(), maxInactiveInterval);
                session.setMaxInactiveInterval(maxInactiveInterval);
            }
        }
    }

}
