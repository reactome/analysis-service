[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

# Reactome Analysis Service

## What is the Reactome Analysis Service

The Analysis Service is the Reactome API that provides an overrepresentation analysis, an expression data analysis and a species comparison tool. It is based on Spring MVC, based on REST and fully documented in Open API (previously Swagger).
Analysis Service is token based, so for every analysis request a TOKEN is associated to the result. From this moment on, the results can be accessed via the API “token” method in order to retrieve more detailed information. Taking advantage of the token produced during every analysis, it is possible to link back to the PathwayBrowser and browse the results overlaid on either the pathways overview or a selected pathway.

#### Installation Guide

* :warning: Pre-Requirement (in the given order)
    1. Maven 3.X - [Installation Guide](http://maven.apache.org/install.html)
    2. Analysis Intermediate binary file - [Analysis Core](https://github.com/reactome/AnalysisTools/tree/master/Core)

##### Git Clone

```console
git clone https://github.com/reactome/AnalysisService.git
cd AnalysisService
```

##### Configuring Maven Profile :memo:

Maven Profile is a set of configuration values which can be used to set or override default values of Maven build. Using a build profile, you can customise build for different environments such as Production v/s Development environments.
Add the following code-snippet containing all the Reactome properties inside the tag ```<profiles>``` into your ```~/.m2/settings.xml```.
Please refer to Maven Profile [Guideline](http://maven.apache.org/guides/introduction/introduction-to-profiles.html) if you don't have settings.xml


```html
<profile>
    <id>AnalysisService-Local</id>
    <properties>
        <!-- Analysis -->
        <analysis.structure.file>/Users/reactome/Reactome/analysis/data/analysis.bin</analysis.structure.file>
        <analysis.result.root>/Users/reactome/Reactome/analysis/temp</analysis.result.root>
        <analysis.report.log>/Users/reactome/Reactome/analysis/report</analysis.report.log>
        <!-- Logging -->
        <log4j.root>/Users/reactome/Reactome/analysis/log</log4j.root>
        <!-- Reactome Server to query header and footer -->
        <template.server>http://reactomedev.oicr.on.ca/</template.server>
    </properties>
</profile>
```

#### Set MAVEN_OPTS :warning:
```console
export MAVEN_OPTS="-Xms2048m -Xmx5120m"
```

##### Running Analysis Service activating ```AnalysisService-Local``` profile
```console
mvn spring-boot:run -P AnalysisService-Local
```

in case you didn't set up the profile it is still possible to run Reactome Analysis Service. You may need to add all the properties into a command-line call.
```console
mvn spring-boot:run  \
    -Dlog4j.root=/Users/reactome/Reactome/analysis/log \
    -Dtemplate.server=http://reactomedev.oicr.on.ca/ \
    -Danalysis.structure.file=/Users/reactome/Reactome/analysis/data/analysis.bin \
    -Danalysis.result.root=/Users/reactome/Reactome/analysis/temp \
    -Danalysis.report.log=/Users/reactome/Reactome/analysis/report
```

Check if Tomcat has been initialised
```rb
[INFO] Running war on http://localhost:8080/
[INFO] Using existing Tomcat server configuration at /Users/reactome/Reactome/AnalysisTools/Service/target/tomcat
INFO: Starting ProtocolHandler ["http-bio-8080"]
```

### How to use the Analysis Services ?

If you are interested in learning about data submission, retrieving data using a pre-existing token and also integrate our Analysis into your application, please refer to our [Developer's Zone](http://www.reactome.org/pages/documentation/developer-guide/analysis-service/).
