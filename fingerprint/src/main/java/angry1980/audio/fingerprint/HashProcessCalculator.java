package angry1980.audio.fingerprint;

import angry1980.audio.Adapter;
import angry1980.audio.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HashProcessCalculator extends ProcessCalculator<HashFingerprint>{

    private static Logger LOG = LoggerFactory.getLogger(HashProcessCalculator.class);

    private final FingerprintType type;

    public HashProcessCalculator(ProcessCreator creator, Adapter adapter, FingerprintType type) {
        super(creator, adapter);
        this.type = Objects.requireNonNull(type);
    }

    private List<TrackHash> convert(long trackId, byte[] data){
        List<TrackHash> hashes = new ArrayList<>(data.length/4);
        IntBuffer buff = ByteBuffer.wrap(data).asIntBuffer();
        for (int i = 0; i < hashes.size(); i ++){
            hashes.add(ImmutableTrackHash.builder().hash(buff.get()).time(i).trackId(trackId).build());
        }
        return hashes;
    }

    @Override
    protected HashFingerprint create(Track track, byte[] hash) {
        LOG.debug("Creation of fingerprint entity for track {}", track.getId());
        HashFingerprint f = ImmutableHashFingerprint.builder()
                .trackId(track.getId())
                .hashes(convert(track.getId(), hash))
                .type(type)
                .build();
        LOG.debug("{} was created for track {}", f, track.getId());
        LOG.debug("There are {} hash values in fingerprint for track {} ", f.getHashes().size(), track.getId());
        return f;
    }

}
