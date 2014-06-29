package org.reactome.server.analysis.report;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class ReportParameters {
    String name;
    Boolean toHuman;
    int ids;
    int found;
    long milliseconds;

    public ReportParameters(String name, Boolean toHuman, int ids, int found, long milliseconds) {
        this.name = name;
        this.toHuman = toHuman;
        this.ids = ids;
        this.found = found;
        this.milliseconds = milliseconds;
    }

    public String getName() {
        return name;
    }

    public Boolean getToHuman() {
        return toHuman;
    }

    public int getIds() {
        return ids;
    }

    public int getFound() {
        return found;
    }

    public long getMilliseconds() {
        return milliseconds;
    }
}
