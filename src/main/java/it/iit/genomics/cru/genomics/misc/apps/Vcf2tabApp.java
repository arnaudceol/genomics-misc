/* 
 * Copyright 2014 Center for Genomic Science of IIT@SEMM, Istituto Italiano di Tecnologia (IIT),.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.iit.genomics.cru.genomics.misc.apps;

import it.iit.genomics.cru.genomics.misc.vcf.Vcf2tab;
import java.io.IOException;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Arnaud Ceol
 */
public class Vcf2tabApp {

    public static void main(String[] args) throws
            IOException,
            ParseException {

        Options options = new Options();

        Option helpOpt = new Option("help", "print this message.");
        options.addOption(helpOpt);

        Option option1 = new Option("f", "filename", true, "VCF file to load");
        option1.setRequired(true);
        options.addOption(option1);

        option1 = new Option("o", "outputfile", true, "outputfilene");
        option1.setRequired(true);
        options.addOption(option1);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;

        try {
            // parse the command line arguments
            cmd = parser.parse(options, args, true);
        } catch (ParseException exp) {
            displayUsage("vcf2tab", options);
            System.exit(1);
        }
        if (cmd.hasOption("help")) {
            displayUsage("vcf2tab", options);
            System.exit(0);
        }

        String filename = cmd.getOptionValue("f");
        String outputfilename = cmd.getOptionValue("o");

        Vcf2tab loader = new Vcf2tab();

        loader.file2tab(filename, outputfilename); //(args[0]);

    }

    private static void displayUsage(String appName, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            formatter.printHelp("bin/" + appName + ".bat ",
                    options);
        } else {
            formatter.printHelp("sh bin/" + appName,
                    options);
        }
    }

}
