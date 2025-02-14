/*
 * Copyright 2023 Thoughtworks, Inc.
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
package com.thoughtworks.go.server.service.result;

import com.thoughtworks.go.serverhealth.HealthStateType;
import com.thoughtworks.go.serverhealth.ServerHealthState;

/**
 * Understands the current status of a given task.
 */
public interface OperationResult {

    ServerHealthState success(HealthStateType healthStateType);

    ServerHealthState error(String message, String description, HealthStateType type);

    ServerHealthState warning(String message, String description, HealthStateType type);

    ServerHealthState getServerHealthState();

    boolean canContinue();

    ServerHealthState forbidden(String message, String description, HealthStateType id);

    void conflict(String message, String description, HealthStateType healthStateType);

    void notFound(String message, String description, HealthStateType healthStateType);

    void accepted(String message, String description, HealthStateType healthStateType);

    void ok(String message);

    void notAcceptable(String message, final HealthStateType type);

    void internalServerError(String message, final HealthStateType type);

    void insufficientStorage(String message, String description, HealthStateType type);

    void badRequest(String message, String description, HealthStateType healthStateType);

    void notAcceptable(String message, String description, HealthStateType type);

    void unprocessableEntity(String message, String description, HealthStateType healthStateType);

}
