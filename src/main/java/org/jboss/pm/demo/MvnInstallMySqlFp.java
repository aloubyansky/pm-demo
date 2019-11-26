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

import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.creator.FeaturePackCreator;
import org.jboss.galleon.spec.PackageDependencySpec;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;

/**
 *
 * @author Alexey Loubyansky
 */
public class MvnInstallMySqlFp extends Task {

    @Override
    protected String logAs() {
        return "mvn install " + Demo.MYSQL_GAV;
    }

    @Override
    public void doExecute(TaskContext ctx) throws Exception {

        FeaturePackCreator.getInstance()
        
            // FEATURE-PACK
            .newFeaturePack().setFPID(Demo.MYSQL_GAV.getFPID())

                // DEPENDENCIES ON OTHER FEATURE-PACKS
                .addDependency(FeaturePackConfig.builder(Demo.WFCORE_GAV)
                        .setInheritPackages(false)
                        .setInheritConfigs(false)
                        .build())
                .addDependency(FeaturePackConfig.builder(Demo.WFSERVLET_GAV)
                        .setInheritPackages(false)
                        .setInheritConfigs(false)
                        .build())
                .addDependency(FeaturePackConfig.builder(Demo.WFLY_GAV)
                        .setInheritPackages(false)
                        .setInheritConfigs(false)
                        .build())

                // DRIVER PACKAGE (BINARIES)
                .newPackage("com.mysql.main", true)
                    // Dependencies on the relevant javax.* packages
                    .addDependency(PackageDependencySpec.required("javax.api"))
                    .addDependency(PackageDependencySpec.required("javax.transaction.api"))
                    // Package content
                    .addDir("modules/system/layers/base/com/mysql/main", ctx.getResource("driver"), false, true)
                    .getFeaturePack()

                // DRIVER CONFIG
                .addFeatureGroup(FeatureGroup.builder("mysql-jdbc")
                        .addFeature(new FeatureConfig("subsystem.datasources.jdbc-driver")
                                .setParam("jdbc-driver", "mysql")
                                .setParam("driver-name", "mysql")
                                .setParam("driver-module-name", "com.mysql")
                                .setParam("driver-xa-datasource-class-name", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource"))
                        .build())

                // DATASOURCE CONFIG
                .addFeatureGroup(FeatureGroup.builder("mysql-ds")
                        .addFeatureGroup(FeatureGroup.forGroup("mysql-jdbc"))
                        .addFeature(new FeatureConfig("subsystem.datasources.data-source")
                                .setParam("data-source", "MySqlDS")
                                .setParam("jndi-name", "java:jboss/datasources/MySqlDS")
                                .setParam("driver-name", "mysql")
                                .setParam("connection-url", "jdbc:mysql://YOUR_HOST/YOUR_DB")
                                .setParam("user-name", "USER")
                                .setParam("password", "PASSWORD"))
                        .addFeature(new FeatureConfig("subsystem.ee.service.default-bindings")
                                .setParam("datasource", "java:jboss/datasources/MySqlDS"))
                        .build())

                 // TARGET WFLY CONFIGS
                .addConfig(ConfigModel.builder("standalone", null)
                		.includeLayer("datasources")
                        .addFeatureGroup(FeatureGroup.forGroup("mysql-jdbc"))
                        .addFeatureGroup(FeatureGroup.forGroup("mysql-ds"))
                        .build())

                .getCreator()
                .addArtifactResolver(SimplisticMavenRepoManager.getInstance(ctx.getMvnRepoPath()))
        .install();
    }
}