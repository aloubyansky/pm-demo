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

import org.jboss.provisioning.config.ConfigModel;
import org.jboss.provisioning.config.FeatureConfig;
import org.jboss.provisioning.config.FeatureGroup;
import org.jboss.provisioning.config.FeaturePackConfig;
import org.jboss.provisioning.repomanager.FeaturePackRepositoryManager;
import org.jboss.provisioning.spec.PackageDependencySpec;

/**
 *
 * @author Alexey Loubyansky
 */
public class MvnInstall extends Task {

    @Override
    public void doExecute(TaskContext ctx) throws Exception {

        FeaturePackRepositoryManager.newInstance(ctx.getMvnRepoPath()).
        installer().

            // FEATURE-PACK
            newFeaturePack(Demo.MYSQL_GAV)

                // DEPENDENCIES ON OTHER FEATURE-PACKS
                .addDependency("wfcore", FeaturePackConfig.builder(Demo.WFCORE_GAV)
                        .setInheritPackages(false)
                        .setInheritConfigs(false)
                        .build())
                .addDependency("wfservlet",FeaturePackConfig.builder(Demo.WFSERVLET_GAV)
                        .setInheritPackages(false)
                        .setInheritConfigs(false)
                        .build())
                .addDependency("wfly", FeaturePackConfig.builder(Demo.WFLY_GAV)
                        .setInheritPackages(false)
                        .setInheritConfigs(false)
                        .build())

                // DRIVER PACKAGE (BINARIES)
                .newPackage("com.mysql.main", true)
                    // Dependencies on the relevant javax.* packages
                    .addDependency("wfcore", PackageDependencySpec.forPackage("javax.api.main"))
                    .addDependency("wfservlet", PackageDependencySpec.forPackage("javax.transaction.api.main"))
                    // Package content
                    .addDir("modules/system/layers/base/com/mysql/main", ctx.getResource("driver"), false, true)
                    .getFeaturePack()

                // DRIVER CONFIG
                .addFeatureGroup(FeatureGroup.builder("mysql-jdbc")
                        .addFeatureGroup(FeatureGroup.forGroup("wfly", "ds-support"))
                        .addFeature(new FeatureConfig("jdbc-driver")
                                .setOrigin("wfly")
                                .setParam("jdbc-driver", "mysql")
                                .setParam("driver-name", "mysql")
                                .setParam("driver-module-name", "com.mysql")
                                .setParam("driver-xa-datasource-class-name", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource"))
                        .build())

                // DATASOURCE CONFIG
                .addFeatureGroup(FeatureGroup.builder("mysql-ds")
                        .addFeatureGroup(FeatureGroup.forGroup("mysql-jdbc"))
                        .addFeature(new FeatureConfig("data-source")
                                .setOrigin("wfly")
                                .setParam("data-source", "MySqlDS")
                                .setParam("jndi-name", "java:jboss/datasources/MySqlDS")
                                .setParam("connection-url", "jdbc:mysql://YOUR_HOST/YOUR_DB")
                                .setParam("driver-name", "mysql")
                                .setParam("user-name", "USER")
                                .setParam("password", "PASSWORD"))
                        .addFeature(new FeatureConfig("ee-default-data-source-binding")
                                .setOrigin("wfservlet")
                                .setParam("datasource", "java:jboss/datasources/MySqlDS"))
                        .build())

                 // TARGET WFLY CONFIGS
                .addConfig(ConfigModel.builder("standalone", null)
                        .addFeatureGroup(FeatureGroup.forGroup("mysql-jdbc"))
                        .addFeatureGroup(FeatureGroup.forGroup("mysql-ds"))
                        .build())

               .getInstaller()
        .install();
    }
}