package com.sdgesi;

import io.quarkus.arc.config.ConfigProperties;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ConfigProperties(prefix = "application")
@Data
public class Configuration {

    @ConfigProperty(defaultValue = "1000")
    int chunkSize;

    @ConfigProperty(defaultValue = "staging")
    String stagingCollection;

    @ConfigProperty(defaultValue = "contractors")
    String collectionName;

    @ConfigProperty(defaultValue = "contractors")
    String dbName;

    @ConfigProperty(defaultValue = "OnlineServices/DataPortal/DownLoadFile.ashx?fName=MasterLicenseData&type=C")
    String uri;

    @ConfigProperty(defaultValue = "https://www.cslb.ca.gov:443")
    String host;





}
