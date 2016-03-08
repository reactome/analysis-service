package org.reactome.server.analysis.core.model;

import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.NodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharSequenceNodeFactory;
import org.reactome.server.analysis.core.model.resource.Resource;
import org.reactome.server.analysis.core.util.MapSet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class IdentifiersMap<T> implements Serializable {

    private RadixTree<MapSet<Resource, T>> tree;

    public IdentifiersMap() {
        NodeFactory nodeFactory = new DefaultCharSequenceNodeFactory();
        this.tree = new ConcurrentRadixTree<MapSet<Resource, T>>(nodeFactory);
    }

    private MapSet<Resource, T> getOrCreateResourceEntitiesMap(String identifier) {
        MapSet<Resource, T> map = this.tree.getValueForExactKey(identifier);
        if (map == null) {
            map = new MapSet<Resource, T>();
            this.tree.put(identifier, map);
        }
        return map;
    }

    public void add(String identifier, Resource resource, T node) {
        String id = identifier.trim().toUpperCase();
        MapSet<Resource, T> map = getOrCreateResourceEntitiesMap(id);
        map.add(resource, node);
    }

    public MapSet<Resource, T> get(AnalysisIdentifier identifier) {
        Set<AnalysisIdentifier> identifiers = expandIdentifierWithPolimorfism(identifier);

        MapSet<Resource, T> rtn = new MapSet<Resource, T>();
        for (AnalysisIdentifier aux : identifiers) {
            String id = aux.getId().toUpperCase();
            MapSet<Resource, T> res = this.tree.getValueForExactKey(id);
            if (res != null) {
                rtn.addAll(res);
            }
        }
        return rtn;
    }

    public MapSet<Resource, T> get(String identifier) {
        MapSet<Resource, T> res = this.tree.getValueForExactKey(identifier.toUpperCase());
        if (res != null) return res;
        return new MapSet<Resource, T>();
    }

    /**
     * Returns all the identifiers in the Map (upper Case)
     *
     * @return all the identifiers in the Map (upper Case)
     */
    public Set<String> keySet() {
        Set<String> keySet = new HashSet<String>();
        for (CharSequence charSequence : this.tree.getKeysStartingWith("")) {
            keySet.add(String.valueOf(charSequence));
        }
        return keySet;
    }

    private Set<AnalysisIdentifier> expandIdentifierWithPolimorfism(AnalysisIdentifier identifier) {
        //The compiler takes care of replacing this where the variables are used, so no worries for performance
        String UNIPROT = "([O,P,Q][0-9][A-Z, 0-9]{3}[0-9]|[A-N,R-Z]([0-9][A-Z][A-Z,0-9]{2}){1,2}[0-9])";
//        String UNIPROT_POLIMORFISM = UNIPROT + "\\-\\d+$";

        Set<AnalysisIdentifier> rtn = new HashSet<AnalysisIdentifier>();
        String id = identifier.getId().toUpperCase();
        rtn.add(identifier);
        if (id.matches(UNIPROT) && !id.contains("-")) {
            for (CharSequence sequence : this.tree.getKeysStartingWith(id + "-")) {
//                String aux = sequence.toString();
//                if(aux.matches(UNIPROT_POLIMORFISM)){
                rtn.add(new AnalysisIdentifier(sequence.toString(), identifier.getExp()));
//                }
            }
        }
        return rtn;
    }
}
