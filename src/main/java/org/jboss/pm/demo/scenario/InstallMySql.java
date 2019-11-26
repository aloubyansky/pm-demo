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

package org.jboss.pm.demo.scenario;

import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeatureConfig;
import org.jboss.galleon.config.FeatureGroup;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.spec.FeatureId;
import org.jboss.pm.demo.Demo;
import org.jboss.pm.demo.MvnInstallMySqlFp;

/**
 *
 * @author Alexey Loubyansky
 */
public class InstallMySql {

    public static void main(String[] args) throws Exception {

        Demo.newInstance()

        // install feature-pack into the local Maven repo
        .schedule(new MvnInstallMySqlFp())

        .pmInstallFp(
                FeaturePackConfig.builder(Demo.WFSERVLET_GAV)
                .setInheritPackages(false)
                .setInheritConfigs(false)
                // picking the default configs to install
                .includeDefaultConfig("standalone", "standalone.xml")
                .includeDefaultConfig("standalone", "standalone-load-balancer.xml")
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

        .doDemo();
    }
}
