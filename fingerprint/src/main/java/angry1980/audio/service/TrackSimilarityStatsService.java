package angry1980.audio.service;

import angry1980.audio.model.FingerprintType;
import angry1980.audio.stats.FingerprintTypeComparing;
import angry1980.audio.stats.FingerprintTypeResult;
import rx.Observable;

import java.util.List;
import java.util.Map;

public interface TrackSimilarityStatsService {

    Observable<FingerprintTypeResult> getResultDependsOnFingerprintType();

    Observable<FingerprintTypeComparing> compareFingerprintTypes();

    FingerprintTypeResult getResultDependsOnFingerprintType(FingerprintType type);

    FingerprintTypeResult getResultDependsOnFingerprintType(FingerprintType type, int minWeight);

    int getCommonCount();

    Map<Long, List<Long>> generateClusters();
}
