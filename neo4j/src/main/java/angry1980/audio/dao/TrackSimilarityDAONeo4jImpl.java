package angry1980.audio.dao;

import angry1980.audio.model.*;
import angry1980.audio.neo4j.FingerprintTypeSimilaritiesQuery;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrackSimilarityDAONeo4jImpl extends Neo4jRelation implements TrackSimilarityDAO {

    public TrackSimilarityDAONeo4jImpl(GraphDatabaseService graphDB) {
        super(graphDB);
    }

    @Override
    public List<TrackSimilarity> tryToFindByTrackId(long trackId) {
        return getTemplate().execute(graphDB -> {
            //return getEntities(graphDB, Neo4jNodeType.TRACK, trackId, Neo4jRelationType.SIMILAR, this::fromRelationToTrackSimilarity);
            return getNode(graphDB, Neo4jNodeType.TRACK, trackId)
                    .map(node -> getConnections(node, Neo4jRelationType.SIMILAR, this::fromRelationToTrackSimilarity))
                    .orElseGet(() -> Collections.emptyList());
        });
    }

    @Override
    public Collection<TrackSimilarity> tryToGetAll() {
        return getTemplate().execute(graphDB -> {
            return getNodesAsStream(graphDB, Neo4jNodeType.TRACK)
                    .map(this::getId)
                    .flatMap(nodeId -> tryToFindByTrackId(nodeId).stream())
                    .collect(Collectors.toSet());
        });
    }

    @Override
    //it makes less the count of transactions and theoretically can improve performance of data import
    public Collection<TrackSimilarity> tryToCreateAll(Collection<TrackSimilarity> entities) {
        getTemplate().execute(graphDB -> {
            entities.stream().forEach(entity -> {
                getNode(graphDB, Neo4jNodeType.TRACK, entity.getTrack1())
                        .ifPresent(node1 -> {
                            getNode(graphDB, Neo4jNodeType.TRACK, entity.getTrack2())
                                    .ifPresent(node2 -> {
                                        getOrCreateRelation(node1, node2, entity);
                                        getOrCreateRelation(node2, node1, entity);
                                    });
                        });
            });
        });
        return entities;

    }

    @Override
    public TrackSimilarity tryToCreate(TrackSimilarity entity) {
        getTemplate().execute(graphDB -> {
            getNode(graphDB, Neo4jNodeType.TRACK, entity.getTrack1())
                .ifPresent(node1 -> {
                    getNode(graphDB, Neo4jNodeType.TRACK, entity.getTrack2())
                            .ifPresent(node2 -> {
                                getOrCreateRelation(node1, node2, entity);
                                getOrCreateRelation(node2, node1, entity);
                            });
                });
            ;
        });
        return entity;
    }

    @Override
    public Optional<List<TrackSimilarity>> findTruthPositiveByFingerprintType(ComparingType type) {
        return getTemplate().execute(graphDB -> {
            return Optional.of(
                    getTemplate().handle(new FingerprintTypeSimilaritiesQuery(type, true)).getResult()
            );
        });
    }

    @Override
    public Optional<List<TrackSimilarity>> findFalsePositiveByFingerprintType(ComparingType type) {
        return getTemplate().execute(graphDB -> {
            return Optional.of(
                    getTemplate().handle(new FingerprintTypeSimilaritiesQuery(type, false)).getResult()
            );
        });

    }

    private void getOrCreateRelation(Node from, Node to, TrackSimilarity s){
        Relationship r = getNodeConnectionsAsStream(from, Neo4jRelationType.SIMILAR)
                .filter(rl -> rl.getEndNode().getId() == to.getId())
                .filter(rl -> s.getComparingType().name().equals(rl.getProperty("type")))
                .findAny()
                .orElseGet(() -> from.createRelationshipTo(to, Neo4jRelationType.SIMILAR));
        r.setProperty(ID_PROPERTY_NAME, s.getTrack1() + "-" + s.getTrack2() + "-" + s.getComparingType());
        r.setProperty("weight", s.getValue());
        r.setProperty("type", s.getComparingType().name());
    }

    private TrackSimilarity fromRelationToTrackSimilarity(Relationship r){
        return ImmutableTrackSimilarity.builder()
                .track1(getId(r.getStartNode()))
                .track2(getId(r.getEndNode()))
                .value((Integer) r.getProperty("weight"))
                .comparingType(ComparingType.valueOf((String) r.getProperty("type")))
                    .build();
    }
}
