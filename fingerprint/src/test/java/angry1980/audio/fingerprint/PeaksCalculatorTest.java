package angry1980.audio.fingerprint;

import angry1980.audio.ClassPathAdapter;
import angry1980.audio.model.*;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PeaksCalculatorTest {


    private Track track1;
    private PeaksCalculator calculator;

    @Before
    public void init(){
        calculator = new PeaksCalculator(new ClassPathAdapter());
        track1 = ImmutableTrack.builder().id(1).cluster(0).path("/test1.mp3").build();
    }

    @Test
    public void testTrack1(){
        List<TrackHash> hashes = getHashes(track1.getId());
        Optional<Fingerprint> f = calculator.calculate(track1);
        assertTrue(f.isPresent());
        List<TrackHash> result = f.get().getHashes();
        assertNotNull(result);
        assertTrue(result.size() == hashes.size());
        //System.out.println(Arrays.toString(result.stream().mapToLong(TrackHash::getHash).toArray()));
        IntStream.range(0, result.size()).forEach(i -> assertEquals(result.get(i), hashes.get(i)));
    }

    private List<TrackHash> getHashes(long trackId){
        AtomicInteger time = new AtomicInteger();
        return Arrays.stream(hashes.get(trackId))
                .mapToObj(hash -> ImmutableTrackHash.builder().trackId(trackId).time(time.getAndIncrement()).hash(hash).build())
                .collect(Collectors.toList());
    }

    private static Map<Long, long[]> hashes = ImmutableMap.of(
            //windowSize=1024, overlap = 512
            1L, new long[]{-152791090L, -152790890L, -152790890L, -447758186L, -647558232L, -149188880L, -152592504L, -1844558174L, -551392090L, -347390894L, -952387104L, 2049409092L, -951392534L, 653409476L, -1445759230L, -148587906L, 1249411084L, -243554412L, 48011680L, 651807310L, -1047754224L, -242358616L, -1047757174L, -2047757978L, -552589720L, -1751790136L, -1746789296L, -241558184L, -2044558386L, 251211084L, 1251207510L, -949388734L, 1451409080L, -152789890L, 1943376362L, -148192734L, 649210522L, 850012908L, 847408318L, 1946579980L, -447757632L, 649412920L, 852009488L, -1746789686L, -152388708L, -1644758194L, -946788272L, 1248009120L, 249811508L, -246954772L, 850607088L, -1352792088L, -1547990106L, -1844955600L, -147991086L, -148793074L, -148590890L, -752392734L, -241354992L, -150790686L, -148587674L, -1749388094L, 2142179010L, 649209126L, -152787928L, -352790922L, 1247207464L, -244554626L, -2146989730L, -243357570L, 52412716L, 2146779972L, -1045960576L, -1845754232L, -948390902L, -751390912L, -152592934L, 1649210716L, -151989074L, -952387498L, -146388302L, 1451611920L, -844758170L, -1751187882L, -647755008L, 2142976598L, 2050210066L, -841759430L, 453412876L, -445959394L, -1641358374L, 50610274L, 1047206890L, -1643956224L, 47212326L, -951191108L, -151788936L, 1851611498L, -2042556424L, 451608892L, -148986920L, -1347188696L, 1049608912L, -1042754968L, -242356816L, -1843158210L, -443358428L, -1243556830L, -1843756010L, -1042158230L, 1942976006L, 451206866L, -843555402L, 51609310L, -751187488L, -1247558186L, -152786898L, 53409120L, -1752792930L, -1445758028L, -146591490L, -748786904L, -242359386L, -949787330L, -1151190488L, -747790876L, 450410294L, 1942378968L, -1151591882L, -1150390290L, -1843760424L, 649208126L, -1841955226L, 847609716L, -1842558218L, 1053210512L, -148191922L, -151389522L, 450406872L, -751392934L, -947590078L, 1448208912L, 853209482L, -552388526L, 651607920L, 1252209114L, -2042555970L, 2049010322L, 451411684L, -1645158822L, -1748792904L, -845958230L, -243158830L, -1243154228L, -946793106L, 1853212676L, -1644958228L, -1948392096L, -148393126L, -152790890L, -2043355774L, -243558186L, -243355986L, -152589480L, 253009114L, 1848210308L, 650611308L, -1847556186L, -243358230L, -643154972L, 1052611476L, -149591100L, -151987490L, 2146579618L, -348189890L, -2043357816L, -242958810L, -2042359586L, 1249010900L, -548787090L, -842557430L, -2042959428L, -148989506L, 1252411112L, -150390904L, -1441557830L, -1447358004L, -151390334L, -1152787134L, -1045956014L, 1850007106L, -749186908L, -149591100L, 1450213108L, 1942175020L, -149388504L, 1850411688L, -441759578L, -1043360388L, -146592116L, 2147179768L, 651810308L, 2147380166L, 47209064L, -1045158182L, -1441956188L, -244558230L, -151390934L, -1950790322L, -1842958784L, 1847412100L, 1942580218L, -152792300L, -247557190L, -1647758204L, -1443556806L, -1641958202L, 447410700L, -148588876L, -1242155786L, -245158224L, -247558994L, -548788108L, -1750590878L, 2051411268L, -1241758020L, -242159992L, 2052808700L, 248607068L, 448012076L, 248208474L, 450009512L, -1752790332L, -244157784L, -1843359210L, 1450009894L, 650409090L, 1650409124L, -1746987078L, -1146392912L, -1748391130L, 1651813118L, -347790932L, -1245954202L, -148990334L, 2146379578L, -2043360026L, -1441757604L, 250407710L, 1847407072L, 1052607288L, -1041757418L, 247408886L, -247156006L, -1044959170L, 1447209106L, 1651208510L, 451609900L, -150386884L, -243354176L, -2044160404L, -1150993272L, 2047209066L, -548192730L, -2044360006L, -244557824L, -1841555570L, -552792112L, 1652210070L, -641354586L, -148389474L, -1352789910L, 1453411280L, -752787278L, -2041557776L, -247757398L, -152791312L, -843559582L, -1046156010L, -443559218L, 452012666L, -1645360230L, 1652409078L, 251409078L, -846957580L, 1047612284L, -247554620L, -244957614L, -947189334L, -1948387722L, 1447612908L, -244755408L, -150790892L, -243556186L, -152789910L, 247410316L, 1851609098L, -246958616L, -1949190488L, -847360018L, 653008496L, -443558580L, -1044354616L, 1050208120L, -1951388886L, -243957808L, -150190112L, 1942378614L, -752791328L, -246359030L, -1548593124L, -348390488L, 1449611478L, -244760626L, -148390924L, 250211928L, -1445558230L, -243358226L, 1850209076L, -148390916L, -148390892L, 1449409090L, 1847211682L, 2051812468L, 1251012666L, -148590910L, 51612310L, -247758174L, -751386930L, -148390916L, -1644554630L, -1146791320L, 450007270L, -242954206L, 47209108L, -152790890L, -152790934L, -152790890L, -152790890L}
    );

}