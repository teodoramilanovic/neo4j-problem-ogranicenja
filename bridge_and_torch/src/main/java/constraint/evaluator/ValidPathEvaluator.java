package evaluator;

import schema.Labels;
import schema.Properties;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.logging.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ValidPathEvaluator implements Evaluator {

    private final Log log;
    private final long timeAvailable;

    public ValidPathEvaluator(Log log, long t) {
        this.log = log;
        this.timeAvailable=t;
    }

    public Evaluation evaluate(Path path) {
    	Iterable<Relationship> relationships = path.relationships();
        Iterator<Relationship> iterator = relationships.iterator();
        int timePassed=0;
        boolean torch=true;
        
        while (iterator.hasNext()) {
        	Relationship r=iterator.next();
        	RelationshipType type = r.getType();
            String name=type.toString();
            
            String[] s=name.split("_");
            if(s[0].equals("prelaze")) {
            	
            	if(!torch)
            		return Evaluation.EXCLUDE_AND_PRUNE;
            	
            	int vrijeme=((Long)r.getProperty(Properties.ATTRIBUTE)).intValue();
            	timePassed+=vrijeme;
            }
            else {
            	if(torch)
            		return Evaluation.EXCLUDE_AND_PRUNE;
            	
            	int vrijeme=((Long)r.getProperty(Properties.ATTRIBUTE)).intValue();
            	timePassed+=vrijeme;
            }
            
            torch=!torch;
        }
        
        if(timePassed<=timeAvailable)
        	return Evaluation.INCLUDE_AND_CONTINUE;
        else
        	return Evaluation.EXCLUDE_AND_PRUNE;
        
    }
}
