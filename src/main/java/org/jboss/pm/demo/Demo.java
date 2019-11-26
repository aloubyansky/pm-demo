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

import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;


/**
 *
 * @author Alexey Loubyansky
 */
public class Demo implements TaskContext {

    public static final FeaturePackLocation WEBAPP_GAV = FeaturePackLocation.fromString("org.jboss.pm.demo:webapp:1.0.0.Final");

    public static final FeaturePackLocation MYSQL_GAV = FeaturePackLocation.fromString("org.jboss.pm.demo:mysql-ds:1.0.0.Final");

    public static final FeaturePackLocation WFCORE_GAV = FeaturePackLocation.fromString("org.wildfly.core:wildfly-core-galleon-pack:12.0.0.Final");
    public static final FeaturePackLocation WFSERVLET_GAV = FeaturePackLocation.fromString("org.wildfly:wildfly-servlet-galleon-pack:18.0.1.Final");
    public static final FeaturePackLocation WFLY_GAV = FeaturePackLocation.fromString("org.wildfly:wildfly-galleon-pack:18.0.1.Final");

    private static final Path HOME = Paths.get(System.getProperty("user.home")).resolve("pm-demo");

    public static Demo newInstance() {
        return new Demo();
    }

    public static void main(String[] args) throws Exception {

        Demo.newInstance()

        // install feature-packs to the local Maven repo
        .schedule(new MvnInstallMySqlFp())
        .schedule(new MvnInstallWebAppFp())

        .pmInstallFp(FeaturePackConfig.builder(WEBAPP_GAV)
                .addConfig(ConfigModel.builder("standalone", "standalone.xml")
                        .addFeatureGroup(FeatureGroup.builder("mysql-ds")
                                .includeFeature(FeatureId.fromString("subsystem.datasources.data-source:data-source=MySqlDS"),
                                        new FeatureConfig()
                                        .setParam("connection-url", "jdbc:mysql://localhost/pm_demo")
                                        .setParam("user-name", "pm")
                                        .setParam("password", "Pm_Dem0!"))
                                .build())
                        .build())
                .build())

        .doDemo();
    }

    private final List<Task> tasks = new ArrayList<>();
    private ProvisioningManager pm;

    public Demo installFp(FeaturePackLocation fpl) {
        return pmInstallFp(FeaturePackConfig.forLocation(fpl));
    }

    public Demo pmInstallFp(FeaturePackConfig fpConfig) {
        return schedule(new PmInstallFp(fpConfig));
    }

    public Demo schedule(Task task) {
        tasks.add(task);
        return this;
    }

    public void doDemo() throws Exception {
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
                .addArtifactResolver(SimplisticMavenRepoManager.getInstance(getMvnRepoPath()))
                // set the installation home dir
                .setInstallationHome(getEmptyHome()).build();
        return pm;
    }
}
