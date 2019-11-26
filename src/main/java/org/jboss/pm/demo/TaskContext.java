/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pm.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.galleon.ProvisioningManager;


/**
 *
 * @author Alexe Loubyansky
 */
public interface TaskContext {

    Path getMvnRepoPath() throws Exception;

    Path getResource(String relativePath) throws Exception;

    Path getHome();

    default Path getEmptyHome() throws Exception {
        final Path home = getHome();
        if(Files.exists(home)) {
            org.jboss.galleon.util.IoUtils.recursiveDelete(home);
        }
        try {
            Files.createDirectories(home);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create home dir");
        }
        return home;
    }

    ProvisioningManager getPm() throws Exception;
}
