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

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.pm.demo.web.DemoServlet;
import org.jboss.provisioning.config.ConfigModel;
import org.jboss.provisioning.config.FeatureGroup;
import org.jboss.provisioning.config.FeaturePackConfig;
import org.jboss.provisioning.repomanager.FeaturePackRepositoryManager;
import org.jboss.provisioning.util.IoUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author Alexey Loubyansky
 *
 */
public class MvnInstallWebAppFp extends Task {

    @Override
    protected String logAs() {
        return "mvn install " + Demo.WEBAPP_GAV;
    }

    @Override
    protected void doExecute(TaskContext ctx) throws Exception {

        final Path tmpDir = IoUtils.createRandomTmpDir();
        try {
            installFp(ctx, tmpDir);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to install webapp feature-pack", e);
        } finally {
            IoUtils.recursiveDelete(tmpDir);
        }
    }

    private void installFp(TaskContext ctx, Path workDir) throws Exception {

        final WebArchive war = ShrinkWrap.create(WebArchive.class, "pm-demo.war");
        war.addClasses(DemoServlet.class);
        final Path warPath = workDir.resolve("pm-demo.war");
        try(OutputStream out = Files.newOutputStream(warPath)) {
            war.as(ZipExporter.class).exportTo(out);
        }

        FeaturePackRepositoryManager.newInstance(ctx.getMvnRepoPath()).
        installer().

            // FEATURE-PACK
            newFeaturePack(Demo.WEBAPP_GAV)

                // DEPENDENCIES ON OTHER FEATURE-PACKS
                .addDependency("mysql-jdbc", Demo.MYSQL_GAV)
                .addDependency("wfservlet", FeaturePackConfig.builder(Demo.WFSERVLET_GAV)
                        .setInheritConfigs(false)
                        .setInheritPackages(false)
                        .build())
                .addDependency("wfcore", FeaturePackConfig.builder(Demo.WFCORE_GAV)
                        .setInheritConfigs(false)
                        .setInheritPackages(false)
                        .build())

                // DRIVER PACKAGE (BINARIES)
                .newPackage("org.jboss.pm.demo.webapp.main", true)
                    // Package content
                    .addPath("standalone/deployments/" + warPath.getFileName(), warPath, true)
                    .getFeaturePack()

                .addConfig(ConfigModel.builder("standalone", "standalone.xml")
                        .setProperty("config-name", "standalone.xml")
                        .addFeatureGroup(FeatureGroup.forGroup("wfservlet", "web-support"))
                        .build())
                .getInstaller()
        .install();
    }
}
