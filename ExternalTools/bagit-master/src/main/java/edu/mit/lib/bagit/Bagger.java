/**
 * Copyright 2013 MIT Libraries
 * Licensed under: http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.mit.lib.bagit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Bagger is a command-line interface to the BagIt library.
 * It allows simple creation, serialization, and validation of Bags. 
 *
 * See README for sample invocations.
 *
 * @author richardrodgers
 */

public class Bagger {
    /* A bit clunky in the cmd-line arg handling, but deliberately so as to limit
       external dependencies for those who want to only use the library API directly. */
    private List<String> payloads = new ArrayList<>();
    private List<String> references = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private List<String> statements = new ArrayList<>();
    private String archFmt = "directory";
    private String csAlg = "MD5";
    private List<String> optFlags = new ArrayList<>();
    private int verbosityLevel;

    public static void main(String[] args) throws IOException, IllegalAccessException {
        if (args.length < 2) {
            usage();
        }
        Bagger bagger = new Bagger();
        int i = 2;
        while (i < args.length) {
            switch(args[i]) {
                case "-p": bagger.payloads.add(args[i+1]); break;
                case "-r": bagger.references.add(args[i+1]); break;
                case "-t": bagger.tags.add(args[i+1]); break;
                case "-m": bagger.statements.add(args[i+1]); break;
                case "-a": bagger.archFmt = args[i+1]; break;
                case "-c": bagger.csAlg = args[i+1]; break;
                case "-o": bagger.optFlags.add(args[i+1]); break;
                case "-v": bagger.verbosityLevel = Integer.parseInt(args[i+1]); break;
                default: System.out.println("Unknown option: '" + args[i] + "'"); usage();
            }
            i += 2;
        }
        // execute command if recognized
        switch(args[0]) {
            case "fill" : bagger.fill(new File(args[1])); break;
            case "plug" : bagger.plug(new File(args[1])); break; 
            case "complete" : bagger.complete(new File(args[1])); break; 
            case "validate" : bagger.validate(new File(args[1])); break; 
            default: System.out.println("Unknown command: '" + args[0] + "'"); usage();
        }
    }

    public static void usage() {
        System.out.println(
            "Usage: Bagger command bagName [-options]\n" +
            "Commands:\n" +
            "fill       fill a bag with contents\n" +
            "plug       plug holes in bag - holes specified by -r (default: all declared)\n" +
            "complete   returns 0 if bag complete, else non-zero\n" +
            "validate   returns 0 if bag valid, else non-zero");
        System.out.println(
            "Options:\n" +
            "-p    [<bag path>=]<payload file>\n" +
            "-r    <bag path>=<URL> - payload reference\n" +
            "-t    [<bag path>=]<tag file>\n" +
            "-m    <name>=<value> - metadata statement\n" +
            "-a    <archive format> - e.g. 'zip', 'tgz' (default: loose directory)\n" +
            "-c    <checksum algorithm> - default: 'MD5'\n" +
            "-o    <optimization flag>\n" +
            "-v    <level> - output level to console (default: 0 = no output)");
        System.out.println(
            "Optimization flags:\n" +
            "nag   suppress automatic metadata generation");
        System.exit(1);
    }

    private Bagger() {}

    private void fill(File baseDir) throws IOException {
        Filler filler = new Filler(baseDir, csAlg);
        if (optFlags.contains("nag")) {
            filler.noAutoGen();
        }
        for (String payload : payloads) {
            if (payload.indexOf("=") > 0) {
                String[] parts = payload.split("=");
                filler.payload(parts[0], new File(parts[1]));
            } else {
                filler.payload(payload, new File(payload));
            }
        }
        for (String reference : references) {
            String[] parts = reference.split("=");
            filler.payloadRef(parts[0], 0L, parts[1]);
        }
        for (String tag : tags) {
            if (tag.indexOf("=") > 0) {
                String[] parts = tag.split("=");
                filler.tag(parts[0], new File(parts[1]));
            } else {
                filler.tag(tag, new File(tag));
            }
        }
        for (String statement : statements) {
            String[] parts = statement.split("=");
            filler.metadata(parts[0], parts[1]);
        }
        File bagFile = null;
        if (archFmt.equals("directory")) {
            bagFile = filler.toDirectory();
        } else {
            bagFile = filler.toPackage(archFmt);
        }
        if (verbosityLevel > 0) {
            message(bagFile.getName(), true, "created");
        }
    }

    private void plug(File bagFile) throws IOException {
        Loader loader = new Loader(bagFile);
        Map<String, String> holeMap;
        if (references.size() > 0) {
            // only process holes specified
            holeMap = new HashMap<>();
            for (String ref : references) {
                String[] parts = ref.split("=");
                holeMap.put(parts[0], parts[1]);
            }
        } else {
            // any present
            holeMap = loader.payloadRefs();
        }
        // non-optimized - sequential fetching
        for (String relPath : holeMap.keySet()) {
            if (verbosityLevel > 1) {
                System.out.println("Dereferencing " + holeMap.get(relPath));
            }
            loader.resolveRef(relPath, new URL(holeMap.get(relPath)).openStream());
        }
        if (verbosityLevel > 0) {
            System.out.println("Filled " + holeMap.size() + " holes in bag '" + bagFile.getName() + "'");
        }
        // must load bag to update contents
        loader.load();
    }

    private void complete(File bagFile) throws IOException {
        boolean complete = new Loader(bagFile).load().isComplete();
        if (verbosityLevel > 0) {
           message(bagFile.getName(), complete, "complete");
        }
        System.exit(complete ? 0 : -1);
    }

    private void validate(File bagFile) throws IOException {
        boolean valid = new Loader(bagFile).load().isValid();
        if (verbosityLevel > 0) {
            message(bagFile.getName(), valid, "valid");
        }
        System.exit(valid ? 0 : -1);
    }

    private void message(String name, boolean ok, String value) {
        StringBuilder sb = new StringBuilder("Bag '");
        sb.append(name).append("' is ");
        if (! ok) sb.append("in");
        sb.append(value);
        System.out.println(sb.toString());
    }

}
