# Analysis Core

## What is Reactome Analysis Core?
Reactome Analysis Core is a project that can be directly executed to create the analysis data content or mapping files,
but it's also used as part of the AnalysisService (as a dependency).
The project is split into 'Builder' and 'Exporter':
  * Builder: queries the MySql database and creates/populates the data structures needed for the analysis.
  * Exporter: generates mapping files from different resources to reactome content.

## Builder Tool

Creates the intermediate binary file to be used for the analysis service and the exporters:

    $java -jar tools-jar-with-dependencies.jar build -d db -u user -p passwd -o pathTO/analysis_vXX.bin

Add --verbose to see the building status on the screen.

Please note XX refers to the current Reactome release number. The analysis_vXX.bin file has to be copied in the 
corresponding "AnalysisService/input/" folder and then change the symlink of analysis.bin in that folder to point
to the new file.

Once the AnalysisService is restarted the new data will be used.


## Exporter Tool

Different options in this case generate different mapping files:

#### UniProt Exporter

References ONLY to lower level pathways:

    $java -jar tools-jar-with-dependencies.jar export -r UniProt -d {db} -u {user} -p {passwd} -i pathTO/analysis_vXX.bin -o pathTO/UniProt2Reactome.txt

References to all level pathways:

    $java -jar tools-jar-with-dependencies.jar export -r UniProt -d {db} -u {user} -p {passwd} -i pathTO/analysis_vXX.bin -o pathTO/UniProt2Reactome_All_Levels.txt --all
    
References to all reactions:

    $java -jar tools-jar-with-dependencies.jar export -r UniProt -t reactions -d {db} -u {user} -p {passwd} -i pathTO/analysis_vXX.bin -o pathTO/UniProt2ReactomeReactions.txt


#### ChEBI Exporter

References ONLY to lower level pathways:

    $java -jar tools-jar-with-dependencies.jar export -r chEBI -d {db} -u {user} -p {passwd} -i pathTO/analysis_vXX.bin -o pathTO/ChEBI2Reactome.txt

References to all level pathways:

    $java -jar tools-jar-with-dependencies.jar export -r chEBI -d {db} -u {user} -p {passwd} -i pathTO/analysis_vXX.bin -o pathTO/ChEBI2Reactome_All_Levels.txt --all
    
References to all reactions:
    
    $java -jar tools-jar-with-dependencies.jar export -r chEBI -t reactions -d {db} -u {user} -p {passwd} -i pathTO/analysis_vXX.bin -o pathTO/ChEBI2ReactomeReactions.txt


#### Ensembl Exporter

References ONLY to lower level pathways:

    $java -jar tools-jar-with-dependencies.jar export -r Ensembl -d {db} -u {user} -p {passwd} -i pathTO/analysis_vXX.bin -o pathTO/Ensembl2Reactome.txt

References to all level pathways:

    $java -jar tools-jar-with-dependencies.jar export -r Ensembl -d {db} -u {user} -p {passwd} -i pathTO/analysis_vXX.bin -o pathTO/Ensembl2Reactome_All_Levels.txt --all

References to all reactions:
    
    $java -jar tools-jar-with-dependencies.jar export -r Ensembl -t reactions -d {db} -u {user} -p {passwd} -i pathTO/analysis_vXX.bin -o pathTO/Ensembl2ReactomeReactions.txt


#### MirBase Exporter

References ONLY to lower level pathways:

    $java -jar tools-jar-with-dependencies.jar export -r MirBase -d {db} -u {user} -p {passwd} -i pathTO/analysis_vXX.bin -o pathTO/MirBase2Reactome.txt

References to all level pathways:

    $java -jar tools-jar-with-dependencies.jar export -r MirBase -d {db} -u {user} -p {passwd} -i pathTO/analysis_vXX.bin -o pathTO/MirBase2Reactome_All_Levels.txt --all

References to all reactions:
    
    $java -jar tools-jar-with-dependencies.jar export -r MirBase -t reactions -d {db} -u {user} -p {passwd} -i pathTO/analysis_vXX.bin -o pathTO/MirBase2ReactomeReactions.txt



#### Hierarchy Exporting

All pathways for each species file:

    $java -jar tools-jar-with-dependencies.jar hierarchy -t details -i pathTO/analysis_vXX.bin -o pathTO/ReactomePathways.txt

Parent-child binary relationship file:

    $java -jar tools-jar-with-dependencies.jar hierarchy -t relationship -i pathTO/analysis_vXX.bin -o pathTO/ReactomePathwaysRelation.txt

