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
package main;

import static main.Configuration.TEST_DIRECTORY;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import model.EncapsulationData;

/**
 * This class provides test data. It is the only class where paths to test data
 * files should appear! If you want to rename or add a test file, alter this
 * class.
 */
public class TestDataProvider {
    private TestDataProvider() {
    }

    public final static File JPG_FILE = new File(TEST_DIRECTORY + "testdata/cat_jpg_1.jpg");
    public final static File JPG_FILE_2 = new File(TEST_DIRECTORY + "testdata/cat_jpg_2.jpg");
    public final static File JPG_FILE_3 = new File(TEST_DIRECTORY + "testdata/cat_jpg_3.jpg");
    public final static File PNG_FILE = new File(TEST_DIRECTORY + "testdata/cat_png_1.png");
    public final static File PNG_FILE_2 = new File(TEST_DIRECTORY + "testdata/cat_png_2.png");
    public final static File BMP_FILE = new File(TEST_DIRECTORY + "testdata/cat_bmp_1.bmp");
    public final static File PDF_FILE = new File(TEST_DIRECTORY + "testdata/PosterAbstract.pdf");
    public final static File PS_FILE = new File(TEST_DIRECTORY + "testdata/PosterAbstract.ps");
    public final static File TXT_FILE = new File(TEST_DIRECTORY + "testdata/PeriCAT.txt");
    public final static File TXT_FILE_2 = new File(TEST_DIRECTORY + "testdata/short.txt");
    public final static File TXT_FILE_3 = new File(TEST_DIRECTORY + "testdata/short_tutorial.txt");
    public final static File XML_FILE = new File(TEST_DIRECTORY + "testdata/metadata.xml");

    public static EncapsulationData getDatasetBmp() {
	List<File> bmpPayload = new ArrayList<File>();
	bmpPayload.add(PNG_FILE);
	return new EncapsulationData(BMP_FILE, bmpPayload, "bmp_png_dataset");
    }

    public static EncapsulationData getDatasetTxtInPng() {
	List<File> txtPayload = new ArrayList<File>();
	txtPayload.add(TXT_FILE);
	return new EncapsulationData(PNG_FILE, txtPayload, "png_txt_dataset");
    };

    public static EncapsulationData getDatasetMultiXmlInPng() {
	List<File> xmlPayload = new ArrayList<File>();
	xmlPayload.add(XML_FILE);
	xmlPayload.add(TXT_FILE_2);
	return new EncapsulationData(PNG_FILE, xmlPayload, "png_xml_txt_dataset");
    }

    public static EncapsulationData getDatasetMultiXmlInTxt() {
	List<File> xmlPayload = new ArrayList<File>();
	xmlPayload.add(TXT_FILE);
	xmlPayload.add(XML_FILE);
	return new EncapsulationData(TXT_FILE_2, xmlPayload, "2txt_xml_dataset");
    }

    public static EncapsulationData getDatasetMixed() {
	List<File> xmlPayload = new ArrayList<File>();
	xmlPayload.add(XML_FILE);
	xmlPayload.add(TXT_FILE_2);
	xmlPayload.add(PS_FILE);
	return new EncapsulationData(PNG_FILE, xmlPayload, "png_xml_txt_ps_dataset");
    }

    public static EncapsulationData getDatasetTxtInJpg() {
	List<File> txtPayload = new ArrayList<File>();
	txtPayload.add(TXT_FILE);
	return new EncapsulationData(JPG_FILE, txtPayload, "jpg_txt_dataset");
    }

    public static EncapsulationData getDatasetJpgInPng() {
	List<File> txtPayload = new ArrayList<File>();
	txtPayload.add(JPG_FILE);
	return new EncapsulationData(PNG_FILE_2, txtPayload, "png_jpg_dataset");
    }

    public static EncapsulationData getDatasetMultipleTxtInJpg() {
	List<File> txtPayload = new ArrayList<File>();
	txtPayload.add(TXT_FILE);
	txtPayload.add(TXT_FILE_2);
	return new EncapsulationData(JPG_FILE_2, txtPayload, "jpg_2txt_dataset");
    }

    public static EncapsulationData getDatasetTxtInPdf() {
	List<File> txtPayload = new ArrayList<File>();
	txtPayload.add(TXT_FILE_2);
	return new EncapsulationData(PDF_FILE, txtPayload, "pdf_txt_dataset");
    }

    public static EncapsulationData getDatasetPngInPng() {
	List<File> pngPayload = new ArrayList<File>();
	pngPayload.add(PNG_FILE_2);
	return new EncapsulationData(PNG_FILE, pngPayload, "png_png_dataset");
    }

    public static List<EncapsulationData> getTestDatasets() {
	List<EncapsulationData> datasets = new ArrayList<EncapsulationData>();
	if (PNG_FILE.exists()) {
	    // the testdata won't be loaded in the build jar!
	    datasets.add(TestDataProvider.getDatasetTxtInPng());
	    datasets.add(TestDataProvider.getDatasetMultiXmlInPng());
	    datasets.add(TestDataProvider.getDatasetMultiXmlInTxt());
	    datasets.add(TestDataProvider.getDatasetMixed());
	    datasets.add(TestDataProvider.getDatasetTxtInJpg());
	    datasets.add(TestDataProvider.getDatasetJpgInPng());
	    datasets.add(TestDataProvider.getDatasetMultipleTxtInJpg());
	    datasets.add(TestDataProvider.getDatasetTxtInPdf());
	    datasets.add(TestDataProvider.getDatasetPngInPng());
	} else {
	    datasets.add(new EncapsulationData(null, new ArrayList<File>(), "new data set"));
	}
	return datasets;
    }
}
