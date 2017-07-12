<img src=https://cloud.githubusercontent.com/assets/6883670/22938783/bbef4474-f2d4-11e6-92a5-07c1a6964491.png width=220 height=100 />

# Analysis Core

## What is Reactome Analysis Core?
Reactome Analysis Core is a project that can be directly executed to create the analysis data content or mapping files,
but it's also used as part of the AnalysisService (as a dependency).
The project is split into 'Builder' and 'Exporter':
  * Builder: queries the MySql database and creates/populates the data structures needed for the analysis.
  * Exporter: generates mapping files from different resources to reactome content.

## Builder Tool

Creates the intermediate binary file to be used for the analysis service and the exporters:

```console
java -jar tools-jar-with-dependencies.jar build \
      -d db \ 
      -u user \
      -p passwd \
      -o pathTO/analysis_vXX.bin
```

Add ```--verbose``` to see the building status on the screen.

Please note XX refers to the current Reactome release number. The analysis_vXX.bin file has to be copied in the 
corresponding "AnalysisService/input/" folder and then change the symlink of analysis.bin in that folder to point
to the new file.

Once the AnalysisService is restarted the new data will be used.
