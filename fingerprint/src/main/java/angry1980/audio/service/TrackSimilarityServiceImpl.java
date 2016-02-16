package angry1980.audio.service;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.dao.TrackSimilarityDAO;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.similarity.FindSimilarTracks;
import angry1980.audio.similarity.TrackSimilarities;
import angry1980.audio.similarity.TracksToCalculate;
import angry1980.utils.ImmutableCollectors;
import it.unimi.dsi.fastutil.longs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import rx.Observable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class TrackSimilarityServiceImpl implements TrackSimilarityService {

    private static Logger LOG = LoggerFactory.getLogger(TrackSimilarityServiceImpl.class);

    private TrackDAO trackDAO;
    private TrackSimilarityDAO trackSimilarityDAO;
    private List<FindSimilarTracks> findSimilarTracks;
    private TracksToCalculate tracksToCalculate;

    public TrackSimilarityServiceImpl(TrackDAO trackDAO,
                                      TrackSimilarityDAO trackSimilarityDAO,
                                      List<FindSimilarTracks> findSimilarTracks,
                                      TracksToCalculate tracksToCalculate) {
        this.trackDAO = Objects.requireNonNull(trackDAO);
        this.trackSimilarityDAO = Objects.requireNonNull(trackSimilarityDAO);
        this.findSimilarTracks = Objects.requireNonNull(findSimilarTracks);
        this.tracksToCalculate = Objects.requireNonNull(tracksToCalculate);
    }

    @Override
    public Observable<Track> getTracksToCalculateSimilarity() {
        return tracksToCalculate.get();
    }

    @Override
    public Observable<TrackSimilarities> findOrCalculateSimilarities(Track track) {
        return Observable.just(
                    findSimilarTracks.stream()
                        .peek(handler -> LOG.debug("{} is getting ready to handle by {}", track.getId(), handler))
                        .flatMap(handler -> {
                            try{
                                return handler.apply(track.getId()).stream();
                            } catch(Exception e){
                                LOG.error("Error while trying to handle {} by {}", track.getId(), handler);
                            }
                            return Collections.<TrackSimilarity>emptyList().stream();
                        }).collect(ImmutableCollectors.toSet())
                ).map(s -> {
                        LOG.debug("{} was handled. There are {} similarities. ", track.getId(), s.size());
                        return new TrackSimilarities(track, s);
                });
    }

    @Override
    public Observable<TrackSimilarities> findOrCalculateSimilarities(Track track, FingerprintType fingerprintType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Observable<TrackSimilarities> getReport() {
        return Observable.create(subscriber -> {
            trackDAO.getAllOrEmpty().stream()
                    .map(track -> new TrackSimilarities(track, trackSimilarityDAO.findByTrackIdOrEmpty(track.getId())))
                    .forEach(subscriber::onNext);
            subscriber.onCompleted();
        });
    }

    @Override
    public Observable<TrackSimilarity> findSimilarities(FingerprintType fingerprintType, boolean truthPositive) {
        Supplier<Optional<List<TrackSimilarity>>> s = () -> truthPositive
                ? trackSimilarityDAO.findTruthPositiveByFingerprintType(fingerprintType)
                : trackSimilarityDAO.findFalsePositiveByFingerprintType(fingerprintType)
        ;
        return Observable.create(subscriber -> {
            s.get().orElseGet(Collections::emptyList)
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(subscriber::onNext)
            ;
            subscriber.onCompleted();
        });
    }

    @Override
    public Observable<TrackSimilarity> findCommonSimilarities(FingerprintType fingerprintType, boolean onlyTruthPositive) {
        Long2ObjectMap<TrackSimilarity> empty = new Long2ObjectArrayMap<>();
        Function<FingerprintType, Optional<List<TrackSimilarity>>> f = t -> onlyTruthPositive
                                                ? trackSimilarityDAO.findTruthPositiveByFingerprintType(t)
                                                : trackSimilarityDAO.findByFingerprintType(t)
                                                //: trackSimilarityDAO.findFalsePositiveByFingerprintType(t)
        ;
        Long2ObjectMap<Long2ObjectMap<TrackSimilarity>> sorted = f.apply(fingerprintType)
                .map(this::sortByTracks)
                .orElseGet(Long2ObjectArrayMap::new);
        return Observable.create(subscriber -> {
            Arrays.stream(FingerprintType.values())
                    .filter(type -> !type.equals(fingerprintType))
                    .flatMap(type -> f.apply(type).orElseGet(Collections::emptyList).stream())
                    .map(ts -> sorted.getOrDefault(ts.getTrack1(), empty).get(ts.getTrack2()))
                    .filter(Objects::nonNull)
                    .forEach(subscriber::onNext)
            ;
            subscriber.onCompleted();
        });
    }

    private Long2ObjectMap<Long2ObjectMap<TrackSimilarity>> sortByTracks(List<TrackSimilarity> list){
        return list.stream().collect(
                Collector.of(
                        () -> new Long2ObjectArrayMap<Long2ObjectMap<TrackSimilarity>>(),
                        (map, ts) -> map.computeIfAbsent(ts.getTrack1(), t1 -> new Long2ObjectArrayMap<>()).put(ts.getTrack2(), ts),
                        (map1, map2) -> {
                            map2.entrySet().stream()
                                    .filter(entry -> !CollectionUtils.isEmpty(entry.getValue()))
                                    .forEach(entry -> map1.computeIfAbsent(entry.getKey(), k -> new Long2ObjectArrayMap<>()).putAll(entry.getValue()));
                            return map1;
                        }
                )
        );
    }
}
