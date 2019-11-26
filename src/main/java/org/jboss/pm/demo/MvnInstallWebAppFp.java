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

import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.util.IoUtils;
import org.jboss.pm.demo.web.DemoServlet;
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

        FeaturePackCreator.getInstance().

            // FEATURE-PACK
            newFeaturePack().setFPID(Demo.WEBAPP_GAV.getFPID())

                // DEPENDENCIES ON OTHER FEATURE-PACKS
                // MySql driver and config
                .addDependency("mysql-jdbc", Demo.MYSQL_GAV)
                // Default Web server
                .addDependency(FeaturePackConfig.builder(Demo.WFSERVLET_GAV)
                        .setInheritConfigs(false)
                        .setInheritPackages(false)
        				.addConfig(ConfigModel.builder("standalone", "standalone.xml")
        						.includeLayer("web-server")
        						.build())
                        .build())

                // THE WEB APP PACKAGE (BINARIES)
                .newPackage("org.jboss.pm.demo.webapp", true)
                    // Package content
                    .addPath("standalone/deployments/" + warPath.getFileName(), warPath, true)
                    .getFeaturePack()
				.getCreator()
            .install();
    }
}
