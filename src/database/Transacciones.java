/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.util.ArrayList;

/**
 *
 * @author aabdala
 */
public class Transacciones {

    Conexion conexion;
    boolean estamosConectados = true;
    String tipoConexion = "";
    private String database;
    private String user;
    private String ip;
    private String password;
    private String query;
    private boolean debug = false;

    public Conexion getConexion() {
        return conexion;
    }

    public Transacciones() {
        conecta(true);
    }

    public Transacciones(boolean local) {
        conecta(local);
    }

    public Transacciones(String database, String user, String ip, String password) {
        this.database = database;
        this.user = user;
        this.ip = ip;
        this.password = password;
        conecta(true);
    }

    public void desconecta() {
        conexion.shutDown();
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getIp() {
        return ip;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void conecta(boolean conex) {
        if (conex) {
            // ArchivoIP aip = new ArchivoIP();
            //String[]config =  aip.obtieneIP();
            // conexion = new Conexion(config[1], config[0]);
            conexion = new Conexion(database, ip, user, password);
            //System.out.println(config[1] + "  " + config[0]);
            //  JOptionPane.showMessageDialog(null, config[1],config[0],JOptionPane.INFORMATION_MESSAGE);
            estamosConectados = conexion.seConecto;
            tipoConexion = "remota";
        } else {
            //conexion = new Conexion("mantenimiento", "localhost");
            // conexion = new Conexion("bio", "localhost", "root", "AMORPHIS");
            estamosConectados = conexion.seConecto;
            tipoConexion = "local";
        }
    }

    public boolean updateHierarchyNCBINode(String taxid, String hierarchy) {
        String query = "UPDATE ncbi_node SET hierarchy = '" + hierarchy + "' WHERE tax_id =" + taxid;
        if (conexion.queryUpdate(query)) {
            return true;
        } else {
            System.out.println(conexion.getLog());
            return false;
        }
    }

    public ArrayList<ArrayList> getNCBINodes(String where) {
        String query = "SELECT ncbi_node.tax_id, ncbi_node.rank, ncbi_node.name, hierarchy FROM ncbi_node " + where;
        conexion.executeStatement(query);
        ArrayList<ArrayList> dbResult = conexion.getTabla();
        return dbResult;
    }

    /**
     * This method provid a query interface to find the full hierarchy (with
     * comma separated taxids), gicen a target taxid node
     *
     * @param taxid
     * @return the complete taxids linage to the given node
     */
    public String getHirarchyByTaxid(String taxid) {
        String query = "SELECT hierarchy FROM ncbi_node WHERE tax_id = " + taxid;
        conexion.executeStatement(query);
        if (conexion.getTabla() != null && conexion.getTabla().size() > 0) {
            return (String) conexion.getTabla().get(0).get(0);
        } else {
            return "";
        }
    }

    /**
     * Method to see if a taxid has been merged into other node
     *
     * @param taxid the tax id to be tested
     * @return the new taxid if it has been merged, blank other wise
     */
    public String testForMergeTaxid(String taxid) {
        String query = "SELECT tax_id FROM ncbi_merged WHERE old_tax_id = " + taxid;
        conexion.executeStatement(query);
        if (conexion.getTabla() != null && conexion.getTabla().size() > 0) {
            return (String) conexion.getTabla().get(0).get(0);
        } else {
            return "";
        }
    }

    public String getHierarcheByTaxIDPrepared(int taxid) {
        conexion.executePreparedStatementS1(taxid);
        if (conexion.getTabla() != null && conexion.getTabla().size() > 0) {
            return (String) conexion.getTabla().get(0).get(0);
        } else {
            return "";
        }
    }

    public String getLiteralTaxonomy(String taxidHierarchy, String extra, String sep) {
        String query = "SELECT GROUP_CONCAT(name ORDER BY FIELD(tax_id, " + taxidHierarchy + ") SEPARATOR '" + sep + "') "
                + "FROM ncbi_node WHERE tax_id IN (" + taxidHierarchy + ") " + extra;
        conexion.executeStatement(query);
        if (conexion.getTabla() != null && conexion.getTabla().size() > 0) {
            return (String) conexion.getTabla().get(0).get(0);
        } else {
            return "";
        }
    }

    public boolean insertaQuery(String query) {
        if (debug) {
            System.out.println(query);
        }
        if (conexion.queryUpdate(query)) {
            return true;
        } else {
            System.err.println(conexion.getLog());
            return false;
        }

    }
}
