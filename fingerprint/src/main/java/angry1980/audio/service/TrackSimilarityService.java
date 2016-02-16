package angry1980.audio.service;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.Track;
import angry1980.audio.model.TrackSimilarity;
import angry1980.audio.similarity.TrackSimilarities;
import rx.Observable;

public interface TrackSimilarityService {

    Observable<Track> getTracksToCalculateSimilarity();

    Observable<TrackSimilarities> findOrCalculateSimilarities(Track track);

    Observable<TrackSimilarities> findOrCalculateSimilarities(Track track, FingerprintType fingerprintType);

    Observable<TrackSimilarity> findCommonSimilarities(FingerprintType fingerprintType, boolean onlyTruthPositive);

    Observable<TrackSimilarities> getReport();

}
