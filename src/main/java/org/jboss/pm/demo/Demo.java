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

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jboss.provisioning.ArtifactCoords;
import org.jboss.provisioning.ArtifactCoords.Gav;
import org.jboss.provisioning.ProvisioningManager;
import org.jboss.provisioning.config.ConfigModel;
import org.jboss.provisioning.config.FeatureConfig;
import org.jboss.provisioning.config.FeatureGroup;
import org.jboss.provisioning.config.FeaturePackConfig;
import org.jboss.provisioning.repomanager.FeaturePackRepositoryManager;
import org.jboss.provisioning.spec.FeatureId;

/**
 *
 * @author Alexey Loubyansky
 */
public class Demo implements TaskContext {

    static final Gav WEBAPP_GAV = ArtifactCoords.newGav("org.jboss.pm.demo", "webapp", "1.0.0.Final");

    static final Gav MYSQL_GAV = ArtifactCoords.newGav("org.jboss.pm.demo", "mysql-ds", "1.0.0.Final");

    static final Gav WFCORE_GAV = ArtifactCoords.newGav("org.wildfly.core:wildfly-core-feature-pack-new:4.0.0.Alpha1-SNAPSHOT");
    static final Gav WFSERVLET_GAV = ArtifactCoords.newGav("org.wildfly:wildfly-servlet-feature-pack-new:11.0.0.Final-SNAPSHOT");
    static final Gav WFLY_GAV = ArtifactCoords.newGav("org.wildfly:wildfly-feature-pack-new:11.0.0.Final-SNAPSHOT");

    private static final Path HOME = Paths.get(System.getProperty("user.home")).resolve("pm-demo");

    static Demo newInstance() {
        return new Demo();
    }

    public static void main(String[] args) throws Exception {

        Demo.newInstance()

        // install feature-packs to the local Maven repo
        .schedule(new MvnInstallMySqlFp())
        .schedule(new MvnInstallWebAppFp())

        .pmInstallFp(
                FeaturePackConfig.builder(Demo.WFSERVLET_GAV)
                .setInheritConfigs(false)
                // picking the default configs to install
                .includeDefaultConfig("standalone", "standalone.xml")
                //.includeDefaultConfig("standalone", "standalone-load-balancer.xml")
                .build())

        .pmInstallFp(
                FeaturePackConfig.builder(Demo.MYSQL_GAV)
                .addConfig(ConfigModel.builder("standalone", "standalone.xml")
                        .addFeatureGroup(FeatureGroup.builder("mysql-ds")
                                .includeFeature(FeatureId.fromString("data-source:data-source=MySqlDS"),
                                        new FeatureConfig()
                                        .setOrigin("wfly")
                                        .setParam("connection-url", "jdbc:mysql://localhost/pm_demo")
                                        .setParam("user-name", "pm")
                                        .setParam("password", "Pm_Dem0!"))
                                .build())
                        .build())
                .build())

        .installFp(WEBAPP_GAV)

        .doDemo();
    }

    private final List<Task> tasks = new ArrayList<>();
    private ProvisioningManager pm;

    Demo installFp(ArtifactCoords.Gav gav) {
        return pmInstallFp(FeaturePackConfig.forGav(gav));
    }

    Demo pmInstallFp(FeaturePackConfig fpConfig) {
        return schedule(new PmInstallFp(fpConfig));
    }

    Demo schedule(Task task) {
        tasks.add(task);
        return this;
    }

    void doDemo() throws Exception {
        for(Task task : tasks) {
            task.execute(this);
        }
        System.out.println("Complete!");
    }

    @Override
    public Path getMvnRepoPath() throws Exception {
        final Path path = Paths.get(System.getProperty("user.home"))
                .resolve(".m2")
                .resolve("repository");
        if(!Files.exists(path)) {
            throw new IllegalStateException("local maven repo does not exist");
        }
        return path;
    }

    @Override
    public Path getResource(String relativePath) throws Exception {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(relativePath);
        if(resource == null) {
            throw new IllegalStateException("Path not found " + relativePath);
        }
        try {
            return Paths.get(resource.toURI().getPath());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Failed to translate the path", e);
        }
    }

    @Override
    public Path getHome() {
        return HOME;
    }

    @Override
    public ProvisioningManager getPm() throws Exception {
        if(pm != null) {
            return pm;
        }
        pm = ProvisioningManager.builder()
                // set the artifact resolver
                .setArtifactResolver(FeaturePackRepositoryManager.newInstance(getMvnRepoPath()))
                // set the installation home dir
                .setInstallationHome(getEmptyHome()).build();
        return pm;
    }
}
