package angry1980.audio.fingerprint;

import angry1980.audio.Adapter;
import angry1980.audio.model.ComparingType;
import angry1980.audio.model.Fingerprint;
import angry1980.audio.model.FingerprintType;
import angry1980.audio.model.Track;
import angry1980.utils.ProcessWaiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

public abstract class ProcessCalculator<F extends Fingerprint> implements Calculator<F>{

    private static Logger LOG = LoggerFactory.getLogger(ProcessCalculator.class);

    public interface ProcessCreator{

        ProcessBuilder create(File file);

        ComparingType getType();
    }

    private Adapter adapter;
    private ProcessCreator processCreator;

    public ProcessCalculator(ProcessCreator creator, Adapter adapter) {
        this.adapter = Objects.requireNonNull(adapter);
        this.processCreator = Objects.requireNonNull(creator);
    }

    @Override
    public Optional<F> calculate(Track track, FingerprintType fingerprintType) {
        LOG.debug("Start of {} fingerprint calculation for track {}", processCreator.getType(), track.getId());
        return Optional.of(track)
                .flatMap(adapter::getContent)
                .map(this::calculateAudioHash)
                .map(hash -> this.create(track, fingerprintType, hash))
        ;
    }

    protected abstract F create(Track track, FingerprintType fingerprintType, byte[] hash);

    private byte[] calculateAudioHash(File file){
        LOG.debug("Start of {} fingerprint calculation for file {}", processCreator.getType(), file.getAbsolutePath());
        byte[] hashBuffer = null;
        try {

            Process hasher = createProcess(file).start();
            //hasher.waitFor(4, TimeUnit.SECONDS);
            ProcessWaiter.Result hasherResult = ProcessWaiter.waitFor(hasher, 15000);

            if (hasherResult.isTimeout()) {
                LOG.error("Timeout error while trying to calculate {} fingerprint for file {}", processCreator.getType(), file.getAbsolutePath());
            } else if (hasherResult.getCode() != 0) {
                LOG.error("Calculation of audio hash finished with errors: {}", new String(hasherResult.getErrorStream().toByteArray()));
            } else {
                hashBuffer = hasherResult.getOutputStream().toByteArray();
            }
        } catch (Exception e) {
            LOG.error("Error while calculating audio hash for file {}", file.getAbsolutePath());
        }
        return hashBuffer;

    }

    private ProcessBuilder createProcess(File file){
        return processCreator.create(file);
    }


    /*

    private static final int INT_LEN = 4;

    private static final boolean IS_LITTLE_ENDIAN_NATIVE = ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN);

    private int readInt(byte[] hasherOutput) throws IOException {

        if (hasherOutput.length < INT_LEN) {
            throw new EOFException("Hasher output is less than 4 bytes");
        }

        int b1 = hasherOutput[0] & 0xFF;
        int b2 = hasherOutput[1] & 0xFF;
        int b3 = hasherOutput[2] & 0xFF;
        int b4 = hasherOutput[3] & 0xFF;

        return IS_LITTLE_ENDIAN_NATIVE
                ? (b4 << 24) | (b3 << 16) | (b2 << 8) | b1
                : (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    private byte[] readAudioHash(byte[] hasherOutput) throws IOException {
        int len = readInt(hasherOutput) * INT_LEN;

        int dataLen = hasherOutput.length - INT_LEN;
        if (len < 0 || len != dataLen) {
            throw new IOException("Incorrect length of hash: " + len + " for hash array of length " + dataLen);
        }

        byte[] hashBytes = new byte[dataLen];
        if (IS_LITTLE_ENDIAN_NATIVE) {
            for (int i = 0, j = INT_LEN; i < dataLen; i += INT_LEN, j += INT_LEN) {
                hashBytes[i]     = hasherOutput[j + 3];
                hashBytes[i + 1] = hasherOutput[j + 2];
                hashBytes[i + 2] = hasherOutput[j + 1];
                hashBytes[i + 3] = hasherOutput[j];
            }
        } else {
            System.arraycopy(hasherOutput, INT_LEN, hashBytes, 0, dataLen);
        }

        return hashBytes;
    }
*/

}
