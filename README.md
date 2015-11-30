# PeriCAT
The PERICLES Content Aggregation Tool is a framework for Information Encapsulation techniques developed during the PERICLES EU-project.

![PeriCAT](https://github.com/pericles-project/PeriCAT/blob/master/images/PeriCAT_slide_CCBY2.png)

The framework provides a set of Information Encapsulation techniques from different domains, and a decision mechanism to determine which of the techniques is the best one to use for a given user scenario.

# Documentation

*  [About] (https://github.com/pericles-project/PeriCAT/wiki)
*  [Installation] (https://github.com/pericles-project/PeriCAT/wiki/Installation)
*  [Quick Start Guide] (https://github.com/pericles-project/PeriCAT/wiki/Quick-Start-Guide)
*  [Information Encapsulation Techniques] (https://github.com/pericles-project/PeriCAT/wiki/Integrated-Techniques)
*  [Decision Mechanism] (https://github.com/pericles-project/PeriCAT/wiki/About-PeriCAT%27s-Decision-Mechanism)
*  [Sheer Curation] (https://github.com/pericles-project/PeriCAT/wiki/Sheer-Curation-with-PET-and-PeriCAT)
*  [Developer Guide] (https://github.com/pericles-project/PeriCAT/wiki/Developer-Guide)

## Directory Structure

Execute the PeriCAT.jar to run the tool. It is delivered in a parent directory, called PeriCAT, together with the PeriCAT_libs directory, in which you can find the two external tools f5.jar and openstego.jar. That is because the licenses of f5 and openstego are not compatible with the PeriCAT license. f5 is licensed under LGPL [F5 Steganography] (https://code.google.com/p/f5-steganography/) and openstego under GPLv2 [OpenStego] (https://github.com/syvaidya/openstego).
Therefore they have to be delivered externally of PeriCAT, and can't be included into the PeriCAT.jar. PeriCAT is able run without these tools, but then the steganography algorithms can't be used.

If you execute the PeriCAT.jar, the tool will create three additional directories:
* PeriCAT_scenarios - This ist the default directory in which the scenario files will be saved
* PeriCAT_output - Here is the encapsulated information stored
* PeriCAT_restored - If encapsulated information will be decapsulated, then the resulting files are stored in this directory.

## Note

PeriCAT is a tool with the purpose of supporting the PERICLES research. This means that it is no mature business solution in its current state. However, we are very happy if the tool inspires others as base for further developments, education, or research. Therefore the Apache version 2 Open Source license is chosen to enable the possibility to reuse and play arround with the source code for everyone interested in this project.

Visit the [PERICLES] (http://www.pericles-project.eu/) homepage for the related the background research and deliverables.

## License

PeriCAT is licensed under the Apache License, Version 2.0.

You may obtain a copy of the License at: [Apache v2] (http://www.apache.org/licenses/LICENSE-2.0)

# Credits

 _This project has received funding from the European Union’s Seventh Framework Programme for research, technological development and demonstration under grant agreement no FP7- 601138 PERICLES._   
 
 <a href="http://ec.europa.eu/research/fp7"><img src="https://github.com/pericles-project/pet/blob/master/wiki-images/LogoEU.png" width="110"/></a>
 <a href="http://www.pericles-project.eu/"> <img src="https://github.com/pericles-project/pet/blob/master/wiki-images/PERICLES%20logo_black.jpg" width="200" align="right"/> </a>

<a href="http://www.sub.uni-goettingen.de/"><img src="https://github.com/pericles-project/pet/blob/master/wiki-images/sub-logo.jpg" width="300"/></a>
