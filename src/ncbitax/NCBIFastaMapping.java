/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ncbitax;

import database.Transacciones;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is created to map a accessions ids to tax ids according to NCBI
 * nucleotide database and NCBI's Taxonomi DB According to The expecting mapping
 * files for this programs can be downloaded from:
 * ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/accession2taxid/README The NCBI
 * mapping files to taxids conteins the following format accession -tab-
 * accession.version -tab- taxid -tab- gi
 *
 * @author aabdala
 */
public class NCBIFastaMapping {

    private String mappingFile; //NCBI mapping file 2 accession ftp://ftp.ncbi.nlm.nih.gov/pub/taxonomy/accession2taxid/ info https://www.ncbi.nlm.nih.gov/guide/genomes-maps/
    private String fastaFile; //fasta files with accession number rigth before >
    private Transacciones transacciones;
    private String output;
    private boolean debug = false;
    private boolean appendTaxid = false;
    private boolean taxID0 = true;
    private String outSep = "\t";
    private String rankType = "ALL";
    private String rankValues = "";
    private boolean notFasta = false;
    private boolean flagBulkNotFound = false;
    private boolean withHashMap = false;
    private String sep = ";";
    int notFound = 0;
    int errors = 0;
    int notAtDB = 0;
    int oks = 0;
    FileWriter mappwriter;
    FileWriter notFoundWriter;
    FileWriter errorWriter;
    FileWriter notAtDBWriter;
    //List<String> rangos = Arrays.asList("kingdom", "superkingdom", "subkingdom", "superphylum", "phylum", "subphylum", "superclass", "infraclass", "class", "subclass", "parvorder", "superorder", "infraorder", "order", "suborder", "superfamily", "family", "subfamily", "tribe", "subtribe", "genus", "subgenus", "species", "species group", "species subgroup", "subspecies", "forma", "varietas", "no rank");
    String splitChars = "\t";
    /**
     * we set fixed values for some parameters column to find the tax id and the
     * char to split each line. However this could be changed by the setters of
     * this attribs.
     */
    int taxIDCol = 3;// non zero index

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getSplitChars() {
        return splitChars;
    }

    public void setSplitChars(String splitChars) {
        this.splitChars = splitChars;
    }

    public int getTaxIDCol() {
        return taxIDCol;
    }

    public void setTaxIDCol(int taxIDCol) {
        this.taxIDCol = taxIDCol;
    }

    public NCBIFastaMapping(String mappingFile, String fastaFile, Transacciones transacciones) {
        this.mappingFile = mappingFile;
        this.fastaFile = fastaFile;
        this.transacciones = transacciones;
    }

    public String getMappingFile() {
        return mappingFile;
    }

    public void setMappingFile(String mappingFile) {
        this.mappingFile = mappingFile;
    }

    public String getFastaFile() {
        return fastaFile;
    }

    public void setFastaFile(String fastaFile) {
        this.fastaFile = fastaFile;
    }

    public Transacciones getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(Transacciones transacciones) {
        this.transacciones = transacciones;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isAppendTaxid() {
        return appendTaxid;
    }

    public void setAppendTaxid(boolean appendTaxid) {
        this.appendTaxid = appendTaxid;
    }

    public String getOutSep() {
        return outSep;
    }

    public void setOutSep(String outSep) {
        this.outSep = outSep;
    }

    /**
     * This method takes both files mapping and fasta and process the fasta file
     * line by line, extract the accession and runs grep one by one agains the
     * mapping
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void mappTaxIDs() throws FileNotFoundException, IOException {
        BufferedReader fastaReader = new BufferedReader(new FileReader(fastaFile));
        Map<String, String> taxoMapp = new HashMap<String, String>();
        String taxoLevels = getDesiredLinage();
        File mf = new File(mappingFile);
        if (!mf.exists()) {
            throw new FileNotFoundException(mappingFile + " (No such file or directory)");
        }
        FileWriter mappwriter = new FileWriter(output);
        FileWriter notFoundWriter = null;
        FileWriter errorWriter = null;
        FileWriter notAtDBWriter = null;
        String linea;
        int headers = 0;
        int notFound = 0;
        int errors = 0;
        int notAtDB = 0;
        int oks = 0;
        long start = System.currentTimeMillis();
        long counter5k = System.currentTimeMillis();
        while ((linea = fastaReader.readLine()) != null) {
            if (notFasta || linea.charAt(0) == '>') {
                headers++;
                String accession;
                if (notFasta) {//not fasta file
                    accession = linea;
                } else {
                    accession = linea.indexOf(" ") != -1 ? linea.substring(1, linea.indexOf(" ")) : linea.substring(1);
                }
                String res = grepAccession(accession);
                if (res.length() > 0) {
                    try {
                        String taxid = res.split(splitChars)[taxIDCol - 1];
                        if (taxoMapp.containsKey(taxid)) {
                            if (appendTaxid) {
                                mappwriter.write(accession + outSep + taxoMapp.get(taxid) + outSep + taxid + "\n");
                            } else {
                                mappwriter.write(accession + outSep + taxoMapp.get(taxid) + "\n");
                            }
                            oks++;
                        } else {
                            String hierarchy = transacciones.getHirarchyByTaxid(taxid);
                            if (hierarchy.length() > 0) {
                                String taxonomy = transacciones.getLiteralTaxonomy(hierarchy + "," + taxid, taxoLevels, sep);
                                if (appendTaxid) {
                                    mappwriter.write(accession + outSep + taxonomy + outSep + taxid + "\n");
                                } else {
                                    mappwriter.write(accession + outSep + taxonomy + "\n");
                                }
                                oks++;
                                if (withHashMap) {
                                    taxoMapp.put(taxid, taxonomy);
                                }
                            } else {
                                if (notAtDBWriter == null) {
                                    notAtDBWriter = new FileWriter(output + ".not_at_db");
                                    notAtDBWriter.write("tax id\n");
                                }
                                notAtDBWriter.write(accession + outSep + taxid + "\n");
                                notAtDB++;
                            }
                        }
                    } catch (IndexOutOfBoundsException iobe) {
                        System.err.println("No column index: " + taxIDCol + " for grep result: " + res + "\nLine: " + linea + " Accession: " + accession);
                        if (errorWriter == null) {
                            errorWriter = new FileWriter(output + ".err");
                            errorWriter.write("Line\tAccession\n");
                        }
                        errors++;
                        errorWriter.write(linea + "/n" + accession + "\n");
                    }
                } else {
                    if (notFoundWriter == null) {
                        notFoundWriter = new FileWriter(output + ".not_found");
                        notFoundWriter.write("Accession\n");
                    }
                    notFound++;
                    notFoundWriter.write(accession + "\n");
                }
                if (headers % 5000 == 0) {
                    long current = System.currentTimeMillis();
                    //finish / 1000 + " s."
                    System.out.println("Accession numbers processed: " + headers + " Time: " + (current - counter5k) / 1000 + ".s ... Total time elapsed: " + (current - start) / 1000 + ".s");
                    counter5k = System.currentTimeMillis();
                    if (((double) oks / (double) headers) < 0.5) {
                        System.out.println("At this point your mapping yield is not so good: " + (double) oks / (double) headers
                                + "!!\nctrl+c if you want to stop this run and maybe check for a better mapping file!");
                    }
                }
            }
        }
        System.out.println("***************END***************\n"
                + "Total time: " + (System.currentTimeMillis() - start) / 1000 + " seconds\n"
                + "   -- files created --\n");
        fastaReader.close();
        mappwriter.close();
        System.out.println("\nTaxonomy mapped file: " + output);
        if (notFoundWriter != null) {
            notFoundWriter.close();
            System.out.println("\nAccessions not mapped: " + output + ".not_found");
        }
        if (errorWriter != null) {
            errorWriter.close();
            System.out.println("\nError parsing mapping: " + output + ".err");

        }
        if (notAtDBWriter != null) {
            notAtDBWriter.close();
            System.out.println("\nTax ids not found into DB: " + output + ".not_at_db\n");
        }
        System.out.println("\n   -- sumary --\n"
                + "Mapping yield: " + (double) oks / (double) headers
                + "\nAccessions processed:" + headers
                + "\nAccessions mapped:" + oks
                + "\nAccessions not found on mapping file:" + notFound
                + "\nTaxids not found on reference database:" + notAtDB
                + "\nParsing mapping errors:" + errors);
    }

    /**
     * This method only use one file, the ncbi mapping file. and for all the
     * accessions into this file, the program will create an output file with
     * accession <delim> linage. This method is a shortcut when working with
     * files very big. So the mapping is done by greps LIKE: grep -F -f
     * all_accession.txt nucl_gb.accession2taxid > mapFile.txt
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void completeTaxa() throws FileNotFoundException, IOException {
        BufferedReader mappReader = new BufferedReader(new FileReader(mappingFile));
        Map<String, String> taxoMapp = new HashMap<String, String>();
        //ranktype ALL = no extra
        /*  try {
            transacciones.getConexion().setPreparedStatemenS1("SELECT hierarchy FROM ncbi_node WHERE tax_id = ?");
        } catch (SQLException ex) {
            Logger.getLogger(NCBIFastaMapping.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("SQL exp");
        }*/
        String taxoLevels = getDesiredLinage();
        FileWriter mappwriter = new FileWriter(output);
        FileWriter notFoundWriter = null;
        FileWriter errorWriter = null;
        FileWriter notAtDBWriter = null;
        String linea;
        int headers = 0;
        int notFound = 0;
        int errors = 0;
        int notAtDB = 0;
        int oks = 0;
        long start = System.currentTimeMillis();
        long counter5k = System.currentTimeMillis();
        while ((linea = mappReader.readLine()) != null) {
            headers++;
            try {
                String splitLine[] = linea.split(splitChars);
                //String taxid = splitLine[taxIDCol - 1];
                // String accession = splitLine[1];//usually on column2 make it a param!
                if (taxoMapp.containsKey(splitLine[taxIDCol - 1])) {
                    if (appendTaxid) {
                        mappwriter.write(splitLine[1] + outSep + taxoMapp.get(splitLine[taxIDCol - 1]) + outSep + splitLine[taxIDCol - 1] + "\n");
                    } else {
                        mappwriter.write(splitLine[1] + outSep + taxoMapp.get(splitLine[taxIDCol - 1]) + "\n");
                    }
                    oks++;
                } else {
                    String taxid = splitLine[taxIDCol - 1];
                    String hierarchy = transacciones.getHirarchyByTaxid(taxid);
                    //String hierarchy = transacciones.getHierarcheByTaxIDPrepared(Integer.parseInt(taxid.trim()));
                    if (hierarchy.length() > 0) {
                        String taxonomy = transacciones.getLiteralTaxonomy(hierarchy + "," + taxid, taxoLevels, sep);
                        if (appendTaxid) {
                            mappwriter.write(splitLine[1] + outSep + taxonomy + outSep + splitLine[taxIDCol - 1] + "\n");
                        } else {
                            mappwriter.write(splitLine[1] + outSep + taxonomy + "\n");
                        }
                        oks++;
                        if (withHashMap) {
                            taxoMapp.put(splitLine[taxIDCol - 1], taxonomy);
                        }
                    } else if (taxid.equals("0") && taxID0) {
                        if (appendTaxid) {
                            mappwriter.write(splitLine[1] + outSep + "NotAssigned" + outSep + splitLine[taxIDCol - 1] + "\n");
                        } else {
                            mappwriter.write(splitLine[1] + outSep + "NotAssigned" + "\n");
                        }
                        oks++;

                    } else {
                        if (notAtDBWriter == null) {
                            notAtDBWriter = new FileWriter(output + ".not_at_db");
                            notAtDBWriter.write("Acc\ttax id\n");
                        }
                        notAtDBWriter.write(splitLine[1] + "\t" + splitLine[taxIDCol - 1] + "\n");
                        notAtDB++;
                    }
                }
            } catch (IndexOutOfBoundsException iobe) {
                /* String splitLine[] = linea.split(splitChars);
                String taxid = splitLine[taxIDCol - 1];
                String accession = splitLine[1];*/
                System.err.println("Split line with " + splitChars + "on index: " + (taxIDCol - 1) + " or index: 1 for line: " + linea);
                if (errorWriter == null) {
                    errorWriter = new FileWriter(output + ".err");
                    errorWriter.write("Line\tAccession\n");
                }
                errors++;
                errorWriter.write(linea + "\n");
            }

            if (headers % 50000 == 0) {
                long current = System.currentTimeMillis();
                //finish / 1000 + " s."
                System.out.println("Accession numbers processed: " + headers + " Time: " + (current - counter5k) / 1000 + ".s ... Total time elapsed: " + (current - start) / 1000 + ".s");
                counter5k = System.currentTimeMillis();
                if (((double) oks / (double) headers) < 0.5) {
                    System.out.println("At this point your mapping yield is not so good: " + (double) oks / (double) headers
                            + "!!\nctrl+c if you want to stop this run and maybe check for a better mapping file!");
                }
            }

        }
        System.out.println("***************END***************\n"
                + "Total time: " + (System.currentTimeMillis() - start) / 1000 + " seconds\n"
                + "   -- files created --\n");
        mappReader.close();
        mappwriter.close();
        System.out.println("\nTaxonomy mapped file: " + output);
        if (notFoundWriter != null) {
            notFoundWriter.close();
            System.out.println("\nAccessions not mapped: " + output + ".not_found");
        }
        if (errorWriter != null) {
            errorWriter.close();
            System.out.println("\nError parsing mapping: " + output + ".err");

        }
        if (notAtDBWriter != null) {
            notAtDBWriter.close();
            System.out.println("\nTax ids not found into DB: " + output + ".not_at_db\n");
        }
        System.out.println("\n   -- sumary --\n"
                + "Mapping yield: " + (double) oks / (double) headers
                + "\nAccessions processed:" + headers
                + "\nAccessions mapped:" + oks
                + "\nAccessions not found on mapping file:" + notFound
                + "\nTaxids not found on reference database:" + notAtDB
                + "\nParsing mapping errors:" + errors);
    }

    /**
     * When the program runs on any of the possible modes, it generates an
     * outputfile with extension .not_at_db. The intention of this method is to
     * take that file and process against possible merged nodes, therefor it
     * will perform the mapping for the tax_id not founded. The input file.
     * myfile.not_at_db is writen as a tsv file with acc and taxid
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void processNotAtDB() throws FileNotFoundException, IOException {
        BufferedReader mappReader = new BufferedReader(new FileReader(mappingFile));
        Map<String, String> taxoMapp = new HashMap<String, String>();
        String taxoLevels = getDesiredLinage();
        FileWriter mappwriter = new FileWriter(output);
        FileWriter notFoundWriter = null;
        FileWriter errorWriter = null;
        FileWriter notAtDBWriter = null;
        FileWriter mergedNotAtDBWriter = null;
        String linea;
        int headers = 0;
        int notFound = 0;
        int errors = 0;
        int notAtDB = 0;
        int mergednotAtDB = 0;
        int oks = 0;
        long start = System.currentTimeMillis();
        long counter5k = System.currentTimeMillis();
        while ((linea = mappReader.readLine()) != null) {
            if (!linea.startsWith("#")) {
                headers++;
                try {
                    String splitLine[] = linea.split(splitChars);
                    String taxid = splitLine[1];
                    String accession = splitLine[0];
                    String newTaxid = getMergedTaxID(taxid);
                    if (newTaxid.length() == 0) {
                        if (notAtDBWriter == null) {
                            notAtDBWriter = new FileWriter(output + ".not_at_db");
                            notAtDBWriter.write("#Acc\ttax id\n");
                        }
                        notAtDBWriter.write(accession + "\t" + taxid + "\n");
                        notAtDB++;
                    } else {
                        if (taxoMapp.containsKey(newTaxid)) {
                            if (appendTaxid) {
                                mappwriter.write(accession + outSep + taxoMapp.get(newTaxid) + outSep + newTaxid + "\n");
                            } else {
                                mappwriter.write(splitLine[1] + outSep + taxoMapp.get(newTaxid) + "\n");
                            }
                            oks++;
                        } else {
                            String hierarchy = transacciones.getHirarchyByTaxid(newTaxid);
                            //String hierarchy = transacciones.getHierarcheByTaxIDPrepared(Integer.parseInt(taxid.trim()));
                            if (hierarchy.length() > 0) {
                                String taxonomy = transacciones.getLiteralTaxonomy(hierarchy + "," + newTaxid, taxoLevels, sep);
                                if (appendTaxid) {
                                    mappwriter.write(accession + outSep + taxonomy + outSep + newTaxid + "\n");
                                } else {
                                    mappwriter.write(accession + outSep + taxonomy + "\n");
                                }
                                oks++;
                                if (withHashMap) {
                                    taxoMapp.put(newTaxid, taxonomy);
                                }
                            } else {//encontro el nodo en ncbi_merge, pero ese nodo no tiene una entrada en ncbi_nodes, caso rao pero puede pasar 
                                if (mergedNotAtDBWriter == null) {
                                    mergedNotAtDBWriter = new FileWriter(output + ".merged_not_at_db");
                                    mergedNotAtDBWriter.write("Acc\ttax id\n");
                                }
                                mergedNotAtDBWriter.write(accession + "\t" + newTaxid + "\n");
                                mergednotAtDB++;
                            }
                        }
                    }
                } catch (IndexOutOfBoundsException iobe) {
                    /* String splitLine[] = linea.split(splitChars);
                String taxid = splitLine[taxIDCol - 1];
                String accession = splitLine[1];*/
                    System.err.println("Split line with " + splitChars + "on index: " + (taxIDCol - 1) + " or index: 1 for line: " + linea);
                    if (errorWriter == null) {
                        errorWriter = new FileWriter(output + ".err");
                        errorWriter.write("Line\tAccession\n");
                    }
                    errors++;
                    errorWriter.write(linea + "\n");
                }

                if (headers % 50000 == 0) {
                    long current = System.currentTimeMillis();
                    //finish / 1000 + " s."
                    System.out.println("Accession numbers processed: " + headers + " Time: " + (current - counter5k) / 1000 + ".s ... Total time elapsed: " + (current - start) / 1000 + ".s");
                    counter5k = System.currentTimeMillis();
                    if (((double) oks / (double) headers) < 0.5) {
                        System.out.println("At this point your mapping yield is not so good: " + (double) oks / (double) headers
                                + "!!\nctrl+c if you want to stop this run and maybe check for a better mapping file!");
                    }
                }

            }
        }
        System.out.println("***************END***************\n"
                + "Total time: " + (System.currentTimeMillis() - start) / 1000 + " seconds\n"
                + "   -- files created --\n");
        mappReader.close();
        mappwriter.close();
        System.out.println("\nTaxonomy mapped file: " + output);
        if (notFoundWriter != null) {
            notFoundWriter.close();
            System.out.println("\nAccessions not mapped: " + output + ".not_found");
        }
        if (errorWriter != null) {
            errorWriter.close();
            System.out.println("\nError parsing mapping: " + output + ".err");

        }
        if (notAtDBWriter != null) {
            notAtDBWriter.close();
            System.out.println("\nTax ids not found into DB: " + output + ".not_at_db\n");
        }
        if (mergedNotAtDBWriter != null) {
            mergedNotAtDBWriter.close();
            System.out.println("\nTax ids found in merge table but not into nodes table: " + output + ".merged_not_at_db\n");
        }
        System.out.println("\n   -- sumary --\n"
                + "Mapping yield: " + (double) oks / (double) headers
                + "\nAccessions processed:" + headers
                + "\nAccessions mapped:" + oks
                + "\nTaxids not found on reference database:" + notAtDB
                + "\nMerged taxids not found on reference database:" + mergednotAtDB
                + "\nParsing mapping errors:" + errors);
    }

    /**
     * This method test if any taxid is obsolete and now is merged on other
     * valid taxid
     *
     * @param taxid the taxid to be tested
     * @return the new taxid or empty string if not exist
     */
    public String getMergedTaxID(String taxid) {
        String newId = transacciones.testForMergeTaxid(taxid);
        return newId;
    }

    /**
     * Performs the grep one by one
     *
     * @param accesion the accession to be "greped" against the mapping file
     * @return
     */
    public String grepAccession(String accesion) {
        //String commandLine = "c:/Program Files/R/R-3.0.3/bin/Rscript \"" + workingDir + "scripts/scriptDiversidad.R\" \"" + workingDir + "\" " + nameMatriz + " " + sc.getRealPath("") + fileNameRare + " " + sc.getRealPath("") + fileNameRenyi + " " + betaIndex + " " + sc.getRealPath("") + fileNameBeta + " " + imgExtraTitle;
        String res = "";
        try {
            String commandLine = "grep " + accesion + " -m1 " + mappingFile;//grep X51700.1 -m1 nucl_gb.accession2taxid
            if (debug) {
                System.out.println(commandLine);
            }
            Process proc = Runtime.getRuntime().exec(commandLine);
            proc.waitFor();
            InputStreamReader inputstreamreader = new InputStreamReader(proc.getInputStream());
            BufferedReader reader = new BufferedReader(inputstreamreader);
            String line = null;
            int lines = 0;
            while ((line = reader.readLine()) != null) {
                res += line;
                lines++;
                if (debug) {
                    System.out.println("line: " + lines + ": " + line);
                }
                //or break?
            }
            proc.destroy();
            reader.close();
            //inputstreamreader.close();
        } catch (InterruptedException ie) {

        } catch (IOException ie) {

        }
        return res;
    }

    /**
     * Perform grep searchs with more than one accession per time
     *
     * @param accesion the accession numbers in form 'acc1\|acc2\|...\|accN'
     * @param bulk the number N of accessions
     * @return
     */
    public String grepBulkAccession(String accesion, int bulk) {
        //String commandLine = "c:/Program Files/R/R-3.0.3/bin/Rscript \"" + workingDir + "scripts/scriptDiversidad.R\" \"" + workingDir + "\" " + nameMatriz + " " + sc.getRealPath("") + fileNameRare + " " + sc.getRealPath("") + fileNameRenyi + " " + betaIndex + " " + sc.getRealPath("") + fileNameBeta + " " + imgExtraTitle;
        StringBuilder res = new StringBuilder();
        flagBulkNotFound = false;
        try {
            String commandLine = "grep " + accesion + " -m" + bulk + " " + mappingFile;//grep X51700.1 -m1 nucl_gb.accession2taxid
            String command[] = {"sh", "-c", "grep " + accesion + " -m" + bulk + " " + mappingFile};
            if (debug) {
                System.out.println(commandLine);
            }
            Process proc = Runtime.getRuntime().exec(command);
            proc.waitFor();
            InputStreamReader inputstreamreader = new InputStreamReader(proc.getInputStream());
            BufferedReader reader = new BufferedReader(inputstreamreader);
            String line = null;
            int lines = 0;
            while ((line = reader.readLine()) != null) {
                res.append(line).append("\n");
                lines++;
                if (debug) {
                    System.out.println("line: " + lines + ": " + line);
                }
                //or break?
            }
            proc.destroy();
            reader.close();
            if (lines != bulk) {
                flagBulkNotFound = true;
            }
            //inputstreamreader.close();
        } catch (InterruptedException ie) {

        } catch (IOException ie) {

        }
        return res.toString();
    }

    /**
     * This method take the rankTYpe class attribute and according to its value
     * determines the linage to be searched on the DB
     *
     * @return
     */
    public String getDesiredLinage() {
        String taxoLevels = "";//ranktype ALL = no extra
        if (rankType.equals("KNOWN")) {//REMOVES NO RANK
            taxoLevels = " AND rank IN('kingdom','superkingdom','subkingdom','superphylum','phylum','subphylum','superclass','infraclass','class','subclass','parvorder','superorder','infraorder','order','suborder','superfamily','family','subfamily','tribe','subtribe','genus','subgenus','species','species group','species subgroup','subspecies','forma','varietas')";
        } else if (rankType.equals("CLASSIC")) {
            taxoLevels = "AND rank IN('kingdom','superkingdom','phylum','class','order','family','genus','species','subspecies')";
        } else if (rankType.equals("CUSTOM")) {
            taxoLevels = "AND rank IN(";
            boolean isFirst = true;
            for (String rank : rankValues.split(",")) {
                if (isFirst) {
                    taxoLevels += "'" + rank + "'";
                    isFirst = false;
                } else {
                    taxoLevels += ",'" + rank + "'";
                }

            }

            taxoLevels += ")";
        }
        return taxoLevels;
    }

    /**
     * This method is the "son" of mappTaxIDs() but perform a batch grep search
     * for certain amount of accession defined by bulk param
     *
     * @param bulk the number of accession to search on each grep
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void mappBulkTaxIDs(int bulk) throws FileNotFoundException, IOException {
        BufferedReader fastaReader = new BufferedReader(new FileReader(fastaFile));
        HashMap<String, String> taxoMapp = new HashMap<String, String>();
        String taxoLevels = getDesiredLinage();
        File mf = new File(mappingFile);
        if (!mf.exists()) {
            throw new FileNotFoundException(mappingFile + " (No such file or directory)");
        }
        mappwriter = new FileWriter(output);
        notFoundWriter = null;
        errorWriter = null;
        notAtDBWriter = null;
        String linea;
        int headers = 0;

        long start = System.currentTimeMillis();
        long counter5k = System.currentTimeMillis();
        int cbulk = 0;
        StringBuilder accessions = null;
        while ((linea = fastaReader.readLine()) != null) {
            if (notFasta || linea.charAt(0) == '>') {
                headers++;
                if (cbulk == 0) {
                    accessions = new StringBuilder();
                }
                String accession;
                if (notFasta) {//not fasta file
                    accession = linea;
                } else {
                    accession = linea.indexOf(" ") != -1 ? linea.substring(1, linea.indexOf(" ")) : linea.substring(1);
                }
                cbulk++;
                if (cbulk == 1) {
                    accessions.append("'").append(accession);
                } else if (cbulk == bulk) {//ready to send
                    accessions.append("\\|").append(accession).append("'");

                } else {
                    accessions.append("\\|").append(accession);
                }
                if (cbulk == bulk) {
                    cbulk = 0;
                    batchAnnotation(accessions, taxoLevels, bulk, taxoMapp);
                }
                if (headers % 5000 == 0) {
                    long current = System.currentTimeMillis();
                    //finish / 1000 + " s."
                    System.out.println("Accession numbers processed: " + headers + " Time: " + (current - counter5k) / 1000 + ".s ... Total time elapsed: " + (current - start) / 1000 + ".s");
                    counter5k = System.currentTimeMillis();
                    if (((double) oks / (double) headers) < 0.5) {
                        System.out.println("At this point your mapping yield is not so good: " + (double) oks / (double) headers
                                + "!!\nctrl+c if you want to stop this run and maybe check for a better mapping file!");
                    }
                }
            }
        }
        //Finish the last ones
        if (cbulk > 0) {
            cbulk = 0;
            batchAnnotation(accessions, taxoLevels, bulk, taxoMapp);
            long current = System.currentTimeMillis();
            //finish / 1000 + " s."
            System.out.println("Accession numbers processed: " + headers + " Time: " + (current - counter5k) / 1000 + ".s ... Total time elapsed: " + (current - start) / 1000 + ".s");
        }
        System.out.println("***************END***************\n"
                + "Total time: " + (System.currentTimeMillis() - start) / 1000 + " seconds\n"
                + "   -- files created --\n");
        fastaReader.close();
        mappwriter.close();
        System.out.println("\nTaxonomy mapped file: " + output);
        if (notFoundWriter != null) {
            notFoundWriter.close();
            System.out.println("\nAccessions not mapped: " + output + ".not_found");
        }
        if (errorWriter != null) {
            errorWriter.close();
            System.out.println("\nError parsing mapping: " + output + ".err");

        }
        if (notAtDBWriter != null) {
            notAtDBWriter.close();
            System.out.println("\nTax ids not found into DB: " + output + ".not_at_db\n");
        }
        System.out.println("\n   -- sumary --\n"
                + "Mapping yield: " + (double) oks / (double) headers
                + "\nAccessions processed:" + headers
                + "\nAccessions mapped:" + oks
                + "\nAccessions not found on mapping file:" + notFound
                + "\nTaxids not found on reference database:" + notAtDB
                + "\nParsing mapping errors:" + errors);
    }

    /**
     * This method is the one in charge of write the output files according to
     * the grep search results
     *
     * @param accessions the accession number that were searched against the
     * mapping DB
     * @param taxoLevels the desired taxo level
     * @param bulk the number of accession searched on one single grep
     * @param taxoMapp If it uses hash map, the object in use
     * @throws IOException
     */
    public void batchAnnotation(StringBuilder accessions, String taxoLevels, int bulk, HashMap taxoMapp) throws IOException {
        String res = grepBulkAccession(accessions.toString(), bulk);
        String lines[] = res.split("\n");
        String accession = "";
        for (int i = 0; i < lines.length; i++) {
            //if (res.length() > 0) {
            try {
                String taxid = lines[i].split(splitChars)[taxIDCol - 1];
                accession = lines[i].split(splitChars)[1];
                if (taxoMapp.containsKey(taxid)) {
                    if (appendTaxid) {
                        mappwriter.write(accession + outSep + taxoMapp.get(taxid) + outSep + taxid + "\n");
                    } else {
                        mappwriter.write(accession + outSep + taxoMapp.get(taxid) + "\n");
                    }
                    oks++;
                } else {
                    String hierarchy = transacciones.getHirarchyByTaxid(taxid);
                    if (hierarchy.length() > 0) {
                        String taxonomy = transacciones.getLiteralTaxonomy(hierarchy + "," + taxid, taxoLevels, sep);
                        if (appendTaxid) {
                            mappwriter.write(accession + outSep + taxonomy + outSep + taxid + "\n");
                        } else {
                            mappwriter.write(accession + outSep + taxonomy + "\n");
                        }
                        oks++;
                        if (withHashMap) {
                            taxoMapp.put(taxid, taxonomy);
                        }
                    } else {
                        if (notAtDBWriter == null) {
                            notAtDBWriter = new FileWriter(output + ".not_at_db");
                            notAtDBWriter.write("Accession\ttax id\n");
                        }
                        notAtDBWriter.write(accession + "\t" + taxid + "\n");
                        notAtDB++;
                    }
                }
            } catch (IndexOutOfBoundsException iobe) {
                if (lines[i].length() > 1) {//cuando no encuentra nada regresa una linea en blanco y esa cae en este error....
                    System.err.println("No column index: " + taxIDCol + " for grep result: " + lines[i] + "\nLine: " + lines + " Accession: " + accession);
                    if (errorWriter == null) {
                        errorWriter = new FileWriter(output + ".err");
                        errorWriter.write("Line\tAccession\n");
                    }
                    errors++;
                    errorWriter.write(lines + "\n" + accession + "\n");
                }
            }
        }
        if (flagBulkNotFound) {
            if (notFoundWriter == null) {
                notFoundWriter = new FileWriter(output + ".not_found");
                notFoundWriter.write("Line\tAccession\n");
            }
            /**
             * here accessions looks like 'Access1\|Access2\|...\|AccessN'
             */
            String accs[] = accessions.toString().substring(1, accessions.length() - 1).split("\\\\|\\|");
            for (int i = 0; i < accs.length; i++) {
                if (!res.contains(accs[i])) {
                    notFound++;
                    notFoundWriter.write(accs[i] + "\n");
                    if (debug) {
                        System.out.println("ACCESSION NOT FOUND: " + accs[i]);
                    }
                }

            }

        }
    }

    public String getRankType() {
        return rankType;
    }

    public boolean isNotFasta() {
        return notFasta;
    }

    public void setNotFasta(boolean notFasta) {
        this.notFasta = notFasta;
    }

    public void setRankType(String rankType) {
        this.rankType = rankType;
    }

    public String getRankValues() {
        return rankValues;
    }

    public void setRankValues(String rankValues) {
        this.rankValues = rankValues;
    }

    public boolean isFlagBulkNotFound() {
        return flagBulkNotFound;
    }

    public void setFlagBulkNotFound(boolean flagBulkNotFound) {
        this.flagBulkNotFound = flagBulkNotFound;
    }

    public boolean isWithHashMap() {
        return withHashMap;
    }

    public void setWithHashMap(boolean withHashMap) {
        this.withHashMap = withHashMap;
    }

    public String getSep() {
        return sep;
    }

    public void setSep(String sep) {
        this.sep = sep;
    }

}
