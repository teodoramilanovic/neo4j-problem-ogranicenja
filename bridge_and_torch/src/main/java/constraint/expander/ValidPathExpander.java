package expander;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.logging.Log;

public class ValidPathExpander implements PathExpander {
	
    private final Log log;

    public ValidPathExpander(Log log) {
        this.log = log;
    }

    @Override
    public ResourceIterable<Relationship> expand(Path path, BranchState branchState) {
        Node last = path.endNode();
        
        return last.getRelationships(Direction.OUTGOING);
    }

    @Override
    public PathExpander reverse() {
        return null;
    }
}
