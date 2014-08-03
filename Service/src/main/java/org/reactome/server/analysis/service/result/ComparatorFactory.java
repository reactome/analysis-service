package org.reactome.server.analysis.service.result;

import org.reactome.server.analysis.core.model.PathwayNodeData;
import org.reactome.server.analysis.core.model.resource.MainResource;

import java.util.Comparator;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public abstract class ComparatorFactory {

    public static Comparator<PathwayNodeSummary> getComparator(AnalysisSortType type){
        if(type==null) type = AnalysisSortType.ENTITIES_PVALUE;
        switch (type){
            case NAME:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        return compareTo(o1.getName(), o2.getName());
                    }
                };
            case TOTAL_ENTITIES:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getEntitiesCount():null;
                        Comparable t2 = d2!=null?d2.getEntitiesCount():null;
                        return compareTo(t1, t2);
                    }
                };
            case TOTAL_REACTIONS:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getReactionsCount():null;
                        Comparable t2 = d2!=null?d2.getReactionsCount():null;
                        return compareTo(t1, t2);
                    }
                };
            case FOUND_ENTITIES:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getEntitiesFound():null;
                        Comparable t2 = d2!=null?d2.getEntitiesFound():null;
                        return compareTo(t1, t2);
                    }
                };
            case FOUND_REACTIONS:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getReactionsFound():null;
                        Comparable t2 = d2!=null?d2.getReactionsFound():null;
                        return compareTo(t1, t2);
                    }
                };
            case ENTITIES_RATIO:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getEntitiesRatio():null;
                        Comparable t2 = d2!=null?d2.getEntitiesRatio():null;
                        return compareTo(t1, t2);
                    }
                };
            case ENTITIES_FDR:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getEntitiesFDR():null;
                        Comparable t2 = d2!=null?d2.getEntitiesFDR():null;
                        return compareTo(t1, t2);
                    }
                };
            case REACTIONS_RATIO:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getReactionsRatio():null;
                        Comparable t2 = d2!=null?d2.getReactionsRatio():null;
                        return compareTo(t1, t2);
                    }
                };
            case ENTITIES_PVALUE:
            default:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getEntitiesPValue():null;
                        Comparable t2 = d2!=null?d2.getEntitiesPValue():null;
                        return compareTo(t1, t2);
                    }
                };
        }
    }

    public static Comparator<PathwayNodeSummary> getComparator(AnalysisSortType type, final MainResource r){
        if(r ==null){
            return ComparatorFactory.getComparator(type);
        }
        if(type==null) type = AnalysisSortType.ENTITIES_PVALUE;
        switch (type){
            case NAME:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        return compareTo(o1.getName(), o2.getName());
                    }
                };
            case TOTAL_ENTITIES:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getEntitiesCount(r):null;
                        Comparable t2 = d2!=null?d2.getEntitiesCount(r):null;
                        return compareTo(t1, t2);
                    }
                };
            case TOTAL_REACTIONS:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getReactionsCount(r):null;
                        Comparable t2 = d2!=null?d2.getReactionsCount(r):null;
                        return compareTo(t1, t2);
                    }
                };
            case FOUND_ENTITIES:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getEntitiesFound(r):null;
                        Comparable t2 = d2!=null?d2.getEntitiesFound(r):null;
                        return compareTo(t1, t2);
                    }
                };
            case FOUND_REACTIONS:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getReactionsFound(r):null;
                        Comparable t2 = d2!=null?d2.getReactionsFound(r):null;
                        return compareTo(t1, t2);
                    }
                };
            case ENTITIES_RATIO:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getEntitiesRatio(r):null;
                        Comparable t2 = d2!=null?d2.getEntitiesRatio(r):null;
                        return compareTo(t1, t2);
                    }
                };
            case ENTITIES_FDR:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getEntitiesFDR(r):null;
                        Comparable t2 = d2!=null?d2.getEntitiesFDR(r):null;
                        return compareTo(t1, t2);
                    }
                };
            case REACTIONS_RATIO:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getReactionsRatio(r):null;
                        Comparable t2 = d2!=null?d2.getReactionsRatio(r):null;
                        return compareTo(t1, t2);
                    }
                };
            case ENTITIES_PVALUE:
            default:
                return new Comparator<PathwayNodeSummary>() {
                    @Override
                    public int compare(PathwayNodeSummary o1, PathwayNodeSummary o2) {
                        PathwayNodeData d1 = o1.getData(); PathwayNodeData d2 = o2.getData();
                        Comparable t1 = d1!=null?d1.getEntitiesPValue(r):null;
                        Comparable t2 = d2!=null?d2.getEntitiesPValue(r):null;
                        return compareTo(t1, t2);
                    }
                };
           }
    }

    static int compareTo(Comparable c1, Comparable c2){
        if(c1 == null){
            if(c2 == null){
                return 0;
            }else{
                return 1;
            }
        }
        if(c2 == null){
            return -1;
        }else{
            return c1.compareTo(c2);
        }
    }
}
