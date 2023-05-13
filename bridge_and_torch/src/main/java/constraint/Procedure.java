package constraint;

import evaluator.ValidPathEvaluator;
import expander.ValidPathExpander;
import response.Response;
import schema.Labels;
import schema.Properties;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class Procedure {

    @Context
    public Transaction db;

    @Context
    public Log log;
    
    @org.neo4j.procedure.Procedure(name = "constraint.findValidPaths", mode = Mode.WRITE)
    @Description("CALL constraint.findValidPaths()")
    public Stream<Response> findValidPaths(@Name("n") long n, @Name("constraint") long constraint) {
        List<Response> responseList = new ArrayList<>();
        
        String osobe="";
		for(int i=1; i<=n; i++) {
			osobe+="P"+i;
		}
		String initialS=osobe+"_";
		String finalS="_"+osobe;
        Node initialState = db.findNode(Labels.InitialState, Properties.ATTRIBUTE, initialS);
        Node finalState = db.findNode(Labels.FinalState, Properties.ATTRIBUTE, finalS);
        ValidPathEvaluator validPathEvaluator = new ValidPathEvaluator(log, constraint);
        ValidPathExpander validPathExpander = new ValidPathExpander(log);
        TraversalDescription traversal = this.db.traversalDescription()
                .depthFirst()
                .expand(validPathExpander)
                .uniqueness(Uniqueness.NODE_PATH)
                .evaluator(validPathEvaluator);
        Traverser paths = traversal.traverse(initialState);
        
        for (Path path : paths) {
            if (path.endNode().getId() == finalState.getId()) {
                Response response = new Response();
                response.setPath(path);
                responseList.add(response);
            }
        }
        return responseList.stream();
    }
}
