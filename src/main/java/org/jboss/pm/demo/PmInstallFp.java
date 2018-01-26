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

import org.jboss.provisioning.ArtifactCoords;
import org.jboss.provisioning.config.FeaturePackConfig;

/**
 *
 * @author Alexey Loubyansky
 */
public class PmInstallFp extends Task {

    private final FeaturePackConfig fpConfig;

    public PmInstallFp(ArtifactCoords.Gav gav) {
        this(FeaturePackConfig.forGav(gav));
    }

    public PmInstallFp(FeaturePackConfig fpConfig) {
        this.fpConfig = fpConfig;
    }

    @Override
    protected String logAs() {
        return "pm install " + fpConfig.getGav();
    }

    @Override
    public void doExecute(TaskContext ctx) throws Exception {
        ctx.getPm().install(fpConfig);
    }
}
