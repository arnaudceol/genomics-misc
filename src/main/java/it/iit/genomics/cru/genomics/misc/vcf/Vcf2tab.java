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
package it.iit.genomics.cru.genomics.misc.vcf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * Convert VCF (1-based inclusive) to a simple tab delimited file in format
 * 0-based exclusive. 
 * 
 * In VCF format, one reference sequence may be associate on a
 * single line to many alternative sequences. Such sequences are split in the
 * output file (one line per alternative sequence.
 *
 * In VCF format, each sequence always is at least 1 base long. This means that 
 * for instance in the case of deletion/insertion, the residue at position -1 
 * (or length+1 if start - 1 is < 0) is added to both ref and alt sequences and 
 * to the range. In the output we remove such residue.
 * 
 * Examples:
 * 
 * VCF_________output
 * 10-10 A G   9-10  A G 
 * 10-10 A G,T 9-10  A G 
 *         and 9-10  A T
 * 10-10 A AA  9-10    A 
 * 10-11 AA A  10-10 A  
 * 
 * @author Arnaud Ceol
 */
public class Vcf2tab {

    
    private final static String FIELD_REGION = "region";
    private final static String FIELD_SEQUENCE_ID = "sequenceid";
    private final static String FIELD_START_POSITION = "startposition";
    private final static String FIELD_END_POSITION = "endposition";
    private final static String FIELD_REFERENCE_SEQUENCE = "refsequence";
    private final static String FIELD_ALTERNATE_SEQUENCE = "altsequence";
    
    /**
     * Columns in the VCF format
     */
    private final static int VCF_COLUMN_CHROMOSOME = 0;

    private final static int VCF_COLUMN_POSITION = 1;

    private final static int VCF_COLUMN_ID = 2;

    private final static int VCF_COLUMN_REF_SEQUENCE = 3;

    private final static int VCF_COLUMN_ALT_SEQUENCE = 4;

    private final static int VCF_COLUMN_INFO = 7;

    private final boolean first100 = false;

    private int currentLine = 0;

    public void printHeader(BufferedWriter output) throws IOException {
        String header
                = FIELD_REGION + "\t"
                + FIELD_SEQUENCE_ID + "\t"
                + FIELD_START_POSITION + "\t"
                + FIELD_END_POSITION + "\t"
                + FIELD_REFERENCE_SEQUENCE + "\t"
                + FIELD_ALTERNATE_SEQUENCE + "\t";

        output.write(header + "\n");
    }

    public void file2tab(String inputfile, String outputfile) throws IOException {
        InputStream fstream;

        FileWriter fw = new FileWriter(outputfile);
        try (BufferedWriter bw = new BufferedWriter(fw)) {
            if (inputfile.endsWith("gz")) {
                fstream = new GZIPInputStream(new FileInputStream(inputfile));
            } else {
                fstream = new FileInputStream(inputfile);
            }
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                printHeader(bw);

                String strLine = br.readLine();
                
                while (strLine.startsWith("#")) {
                    strLine = br.readLine();
                    currentLine++;
                }

                int i = 0;
                do {
                    currentLine++;
                    i++;
                    if (i % 10 == 0) {
                        System.out.print(".");
                    }
                    if (i % 1000 == 0) {
                        System.out.println(" " + i);
                    }

                    line2tab(strLine, bw);

                    if (first100 && i > 100) {
                        break;
                    }
                } while ((strLine = br.readLine()) != null);
            }
            bw.flush();
        }

    }

    private void line2tab(String line, BufferedWriter bw) throws IOException {

        String[] columns = line.split("\t");

        String chromosome = columns[VCF_COLUMN_CHROMOSOME];

        if (chromosome == null || "".equals(chromosome)) {
            return;
        }

        if (false == chromosome.startsWith("chr")) {
            chromosome = "chr" + chromosome;
        }

        int start = Integer.parseInt(columns[VCF_COLUMN_POSITION]);
        String referenceSeq = columns[VCF_COLUMN_REF_SEQUENCE];
        String alternateSeqs = columns[VCF_COLUMN_ALT_SEQUENCE];

        int end = start + referenceSeq.length() - 1;

        // continue;
        String currentAssemblyChromosome;
        int currentAssemblyStart;
        int currentAssemblyEnd;

        /**
         * In VCF, if there is any indel, one additional base is inserted. The
         * base is the one at position -1, unless the position was 1, then it is
         * the last +1
         */
        for (String altSeq : alternateSeqs.split(",")) {
            // 1-based inclusive to 0-base exclusive
            int curStart = start - 1;
            int curEnd = end;
            String curRefSeq = referenceSeq;
            String curAltSeq = altSeq;

            while (curRefSeq.length() > 0 && curAltSeq.length() > 0 && curRefSeq.charAt(0) == curAltSeq.charAt(0)) {
                curStart++;
                curRefSeq = curRefSeq.substring(1);
                curAltSeq = curAltSeq.substring(1);
            }

            while (curRefSeq.length() > 0 && curAltSeq.length() > 0 && curRefSeq.charAt(curRefSeq.length() - 1) == curAltSeq.charAt(curAltSeq.length() - 1)) {
                curEnd--;
                curRefSeq = curRefSeq.substring(0, curRefSeq.length() - 1);
                curAltSeq = curAltSeq.substring(0, curAltSeq.length() - 1);
            }

            currentAssemblyChromosome = chromosome;
            currentAssemblyStart = curStart;
            currentAssemblyEnd = curEnd;

            String output
                    = currentAssemblyChromosome + "\t"
                    + currentAssemblyStart + "\t"
                    + currentAssemblyEnd + "\t"
                    + curRefSeq + "\t"
                    + curAltSeq + "\t";

            bw.write(output + "\n");
        }
    }

}
