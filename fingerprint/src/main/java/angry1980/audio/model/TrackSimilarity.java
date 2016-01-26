package angry1980.audio.model;

import org.immutables.value.Value;

import java.util.Objects;

@Value.Immutable
public abstract class TrackSimilarity {

    public abstract long getTrack1();

    public abstract long getTrack2();

    @Value.Default
    public int getValue(){
        return 0;
    }

    public abstract FingerprintType getFingerprintType();

    public TrackSimilarity add(int value){
        return ImmutableTrackSimilarity.builder().from(this).value(getValue() + value).build();
    }

    public TrackSimilarity add(TrackSimilarity other){
        if(!this.equals(other)){
            return this;
        }
        return this.add(other.getValue());
    }

    public TrackSimilarity reverse(){
        return ImmutableTrackSimilarity.builder().from(this).track1(getTrack2()).track2(getTrack1()).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackSimilarity that = (TrackSimilarity) o;
        return getTrack1() == that.getTrack1() &&
                getTrack2() == that.getTrack2() &&
                getFingerprintType().equals(that.getFingerprintType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTrack1(), getTrack2(), getFingerprintType());
    }

}
