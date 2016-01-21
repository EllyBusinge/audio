package angry1980.audio.dao;

import angry1980.audio.model.Neo4jNodeType;
import angry1980.audio.model.Neo4jRelationType;
import org.neo4j.graphdb.*;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class Neo4j {

    public static final String ID_PROPERTY_NAME = "id";

    private GraphDatabaseService graphDB;

    public Neo4j(GraphDatabaseService graphDB) {
        this.graphDB = Objects.requireNonNull(graphDB);
    }

    protected <T> T template(Function<GraphDatabaseService, T> f){
        try(Transaction tx = graphDB.beginTx()){
            T result = f.apply(graphDB);
            tx.success();
            return result;
        }
    }

    protected void template(Consumer<GraphDatabaseService> c){
        try(Transaction tx = graphDB.beginTx()){
            c.accept(graphDB);
            tx.success();
        }
    }

    protected long getId(Node node){
        return (long) node.getProperty(ID_PROPERTY_NAME);
    }

    protected Optional<Node> getNode(GraphDatabaseService graphDB, Neo4jNodeType type, long id){
        return Optional.ofNullable(graphDB.findNode(type, ID_PROPERTY_NAME, id));
    }

    protected Node getOrCreateNode(GraphDatabaseService graphDB, Neo4jNodeType type, long id, Function<Node, Node> create){
        return getNode(graphDB, type, id).orElseGet(() -> create.apply(createNode(graphDB, type, id)));
    }

    protected Node createNode(GraphDatabaseService graphDB, Neo4jNodeType type, long id){
        Node node = graphDB.createNode(type);
        node.setProperty(ID_PROPERTY_NAME, id);
        return node;
    }

    protected Stream<Node> getAllNodes(GraphDatabaseService graphDB, Neo4jNodeType type) {
        Iterable<Node> iterable = () -> graphDB.findNodes(type);
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    protected Stream<Relationship> getConnections(Node node, Neo4jRelationType type){
        return StreamSupport.stream(node.getRelationships(Direction.OUTGOING, type).spliterator(), false);
    }

}
