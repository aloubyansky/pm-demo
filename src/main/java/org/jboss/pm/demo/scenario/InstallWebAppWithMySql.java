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

import org.jboss.pm.demo.Demo;
import org.jboss.pm.demo.MvnInstallMySqlFp;
import org.jboss.pm.demo.MvnInstallWebAppFp;
import org.jboss.provisioning.config.ConfigModel;
import org.jboss.provisioning.config.FeatureConfig;
import org.jboss.provisioning.config.FeatureGroup;
import org.jboss.provisioning.config.FeaturePackConfig;
import org.jboss.provisioning.spec.FeatureId;

/**
 *
 * @author Alexey Loubyansky
 */
public class InstallWebAppWithMySql {

    public static void main(String[] args) throws Exception {

        Demo.newInstance()

        // install feature-pack into the local Maven repo
        .schedule(new MvnInstallMySqlFp())
        .schedule(new MvnInstallWebAppFp())

        .pmInstallFp(FeaturePackConfig.builder(Demo.WEBAPP_GAV)
                .addConfig(ConfigModel.builder("standalone", "standalone.xml")
                        .addFeatureGroup(FeatureGroup.builder("mysql-ds")
                                .setOrigin("mysql-jdbc")
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
