/*
 * This project has received funding from the European Unions Seventh 
 * Framework Programme for research, technological development and 
 * demonstration under grant agreement no FP7-601138 PERICLES.
 * 
 * Copyright 2015 Anna Eggers, State- and Univeristy Library Goettingen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package algorithm;

import static model.Criterion.CARRIER_PROCESSABILITY;
import static model.Criterion.CARRIER_RESTORABILITY;
import static model.Criterion.COMPRESSION;
import static model.Criterion.DETECTABILITY;
import static model.Criterion.ENCAPSULATION_METHOD;
import static model.Criterion.ENCRYPTION;
import static model.Criterion.PAYLOAD_ACCESSIBILITY;
import static model.Criterion.PAYLOAD_RESTORABILITY;
import static model.Criterion.STANDARDS;
import static model.Criterion.VELOCITY;
import static model.Criterion.VISIBILITY;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.dspace.foresite.Agent;
import org.dspace.foresite.Aggregation;
import org.dspace.foresite.OREException;
import org.dspace.foresite.OREFactory;
import org.dspace.foresite.ORESerialiser;
import org.dspace.foresite.ORESerialiserException;
import org.dspace.foresite.ORESerialiserFactory;
import org.dspace.foresite.ResourceMap;
import org.dspace.foresite.ResourceMapDocument;

import model.RestoredFile;
import model.Scenario;
import view.GUIPanel;

/**
 * This plug-In uses the DSPACE implementation of OAI-ORE to create a simple package of this format.
 */
public class OaiOreSubmissionInformationPackage extends AbstractAlgorithm {
    private final JRadioButton tarButton = new JRadioButton("tar");
    private final JRadioButton zipButton = new JRadioButton("zip");

    public OaiOreSubmissionInformationPackage() {
	createConfigurationPanel();
    }

    private void createConfigurationPanel() {
	panel = new GUIPanel();
	panel.setLayout(new GridBagLayout());
	GridBagConstraints constraints = new GridBagConstraints();
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.NORTHWEST;
	panel.add(new JLabel("Choose archiving technique:"), constraints);
	constraints.gridx++;
	ButtonGroup archivingGroup = new ButtonGroup();
	archivingGroup.add(tarButton);
	archivingGroup.add(zipButton);
	zipButton.setSelected(true);
	panel.add(zipButton, constraints);
	constraints.gridy++;
	panel.add(tarButton, constraints);
	constraints.gridx = 0;
	constraints.gridy++;
    }

    @Override
    public File encapsulate(File carrier, List<File> userPayloadList) throws IOException {
	List<File> payloadList = new ArrayList<File>();
	payloadList.addAll(userPayloadList);
	String mapName = "";
	mapName = getOutputFileName(carrier);
	try {
	    Aggregation informationPackage = OREFactory
		    .createAggregation(new URI("http://www.pericles-project.eu/pericat/oai-ore-package"));
	    informationPackage.addTitle(
		    "PeriCAT - PERICLES Content Aggregation Tool created Oai-Ore sumbission information package");
	    informationPackage.createAggregatedResource(new URI(
		    "http://www.pericles-project.eu/pericat/oai-ore-package/carrier-files/" + carrier.getName()));
	    for (File payload : payloadList) {
		informationPackage.createAggregatedResource(new URI(
			"http://www.pericles-project.eu/pericat/oai-ore-package/payload-files/" + payload.getName()));
	    }
	    ResourceMap resourceMap = informationPackage.createResourceMap(new URI(
		    "http://www.pericles-project.eu/pericat/oai-ore-package/resource-map/" + mapName + ".map.rdf.xml"));
	    Agent creator = OREFactory.createAgent();
	    List<String> names = new ArrayList<String>();
	    names.add("PeriCAT");
	    creator.setNames(names);
	    informationPackage.addCreator(creator);
	    resourceMap.addCreator(creator);
	    // serialising:
	    ORESerialiser serialiser = ORESerialiserFactory.getInstance("RDF/XML");
	    ResourceMapDocument doc = serialiser.serialise(resourceMap);
	    String serialisedMap = doc.toString();
	    File resourceMapFile = new File(mapName + ".map.rdf.xml");
	    FileUtils.writeByteArrayToFile(resourceMapFile, serialisedMap.getBytes());
	    payloadList.add(resourceMapFile);
	    File outputFile = null;
	    if (zipButton.isSelected()) {
		outputFile = new ZipPackaging().encapsulate(carrier, payloadList);
	    } else if (tarButton.isSelected()) {
		outputFile = new TarPackaging().encapsulate(carrier, payloadList);
	    }
	    resourceMapFile.delete(); // was temporary
	    return outputFile;
	} catch (OREException | URISyntaxException | ORESerialiserException e) {
	}
	return null;
    }

    @Override
    protected List<RestoredFile> restore(File outputFile) throws IOException {
	List<RestoredFile> restoredFiles = new ArrayList<RestoredFile>();
	List<String> zipExtension = new ArrayList<String>();
	zipExtension.add("zip");
	SuffixFileFilter zipFilter = new SuffixFileFilter(zipExtension);
	List<String> tarExtension = new ArrayList<String>();
	tarExtension.add("tar");
	SuffixFileFilter tarFilter = new SuffixFileFilter(tarExtension);
	if (zipFilter.accept(outputFile)) {
	    restoredFiles = new ZipPackaging().restore(outputFile);
	} else if (tarFilter.accept(outputFile)) {
	    restoredFiles = new TarPackaging().restore(outputFile);
	}
	for (RestoredFile file : restoredFiles) {
	    file.algorithm = this;
	}
	return restoredFiles;
    }

    @Override
    public String getDescription() {
	return "Creates an OAI-ORE SIP (Submission Information Package).\n"
		+ "The package files are encapsulated in a zip or tar archive file.\n\n"
		+ "This is a very basic OAI-ORE wrapper around the carrier and payload files. Note that"
		+ " the OAI-ORE standard provides a lot more configuration possibilities than used for "
		+ "the PeriCAT created packages. We suggest to use a more specialised tool, if you need"
		+ " more complex OAI-ORE files.\n\n"
		+ "For more information about OAI-ORE see: http://www.openarchives.org/ore/ : \n"
		+ "Open Archives Initiative Object Reuse and Exchange (OAI-ORE) defines standards "
		+ "for the description and exchange of aggregations of Web resources. These "
		+ "aggregations, sometimes called compound digital objects, may combine distributed "
		+ "resources with multiple media types including text, images, data, and video. The "
		+ "goal of these standards is to expose the rich content in these aggregations to "
		+ "applications that support authoring, deposit, exchange, visualization, reuse, and "
		+ "preservation. Although a motivating use case for the work is the changing nature "
		+ "of scholarship and scholarly communication, and the need for cyberinfrastructure "
		+ "to support that scholarship, the intent of the effort is to develop standards that "
		+ "generalize across all web-based information including the increasing popular social "
		+ "networks of \"web 2.0\".";
    }

    @Override
    Scenario defineScenario() {
	Scenario scenario = new Scenario("Oai Ore packaging scenario");
	scenario.description = "This is the ideal scenario for creating an OAI-ORE Submission Information Package.";
	scenario.setCriterionValue(ENCAPSULATION_METHOD, PACKAGING);
	scenario.setCriterionValue(VISIBILITY, VISIBLE);
	scenario.setCriterionValue(DETECTABILITY, DETECTABLE);
	scenario.setCriterionValue(CARRIER_RESTORABILITY, YES);
	scenario.setCriterionValue(PAYLOAD_RESTORABILITY, YES);
	scenario.setCriterionValue(CARRIER_PROCESSABILITY, NO);
	scenario.setCriterionValue(PAYLOAD_ACCESSIBILITY, NO);
	scenario.setCriterionValue(ENCRYPTION, NO);
	scenario.setCriterionValue(COMPRESSION, YES);
	scenario.setCriterionValue(VELOCITY, NO);
	scenario.setCriterionValue(STANDARDS, YES);
	return scenario;
    }

    @Override
    SuffixFileFilter configureCarrierFileFilter() {
	return new AcceptAllFilter();
    }

    @Override
    SuffixFileFilter configurePayloadFileFilter() {
	return new AcceptAllFilter();
    }

    @Override
    SuffixFileFilter configureDecapsulationFileFilter() {
	List<String> supportedFileFormats = new ArrayList<String>();
	supportedFileFormats.add("zip");
	supportedFileFormats.add("tar");
	return new SuffixFileFilter(supportedFileFormats);
    }

    @Override
    public String getName() {
	return "OAI-ORE SIP";
    }

    @Override
    public boolean fulfilledTechnicalCriteria(File carrier, List<File> payloadList) {
	return true;
    }
}
