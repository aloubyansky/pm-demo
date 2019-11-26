package org.jboss.pm.demo;

import java.nio.file.Paths;

import org.jboss.galleon.ProvisioningManager;
import org.jboss.galleon.config.ConfigModel;
import org.jboss.galleon.config.FeaturePackConfig;
import org.jboss.galleon.config.ProvisioningConfig;
import org.jboss.galleon.universe.FeaturePackLocation;
import org.jboss.galleon.universe.maven.repo.SimplisticMavenRepoManager;

public class ProvisionDefaultWebServerDemo {

	public static void main(String[] args) throws Exception {
		
        final ProvisioningConfig provisioningConfig = ProvisioningConfig.builder()
                .addFeaturePackDep(FeaturePackConfig.builder(FeaturePackLocation.fromString("org.wildfly:wildfly-galleon-pack:18.0.0.Final"))
                        .setInheritConfigs(false)
                        .setInheritPackages(false)
                        .build())
                .addConfig(ConfigModel.builder("standalone", "standalone.xml")
                        .includeLayer("web-server")
                        .build())
                .build();

        try (ProvisioningManager pm = ProvisioningManager.builder()
                .setInstallationHome(Paths.get(System.getProperty("user.home")).resolve("playground"))
                .addArtifactResolver(SimplisticMavenRepoManager.getInstance(Paths.get(System.getProperty("user.home")).resolve(".m2").resolve("repository")))
                .build()) {
            pm.provision(provisioningConfig);
        }

	}
}
