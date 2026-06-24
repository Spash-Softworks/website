package pl.kamerz.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@PageTitle("Gallery")
@Route(value = "gallery", layout = Layout.class)
public class Gallery extends VerticalLayout {

    record Project(String name, String desc, String tag, String downloadUrl, String docsTab) {
        static Project ready(String name, String desc, String tag, String download, String docsTab) {
            return new Project(name, desc, tag, download, docsTab);
        }
        static Project soon(String name, String desc) {
            return new Project(name, desc, "Soon", null, null);
        }
        boolean isSoon() { return downloadUrl == null && docsTab == null; }
    }

    private static final HttpClient PROBE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final Set<String> BROKEN = ConcurrentHashMap.newKeySet();

    /** Fires up to 5 HEAD checks in parallel, returns the first URL that responds 200. */
    private static String pickUrl() {
        List<String> pool = new ArrayList<>(GIFS);
        pool.removeIf(BROKEN::contains);
        Collections.shuffle(pool, ThreadLocalRandom.current());
        List<String> candidates = pool.subList(0, Math.min(5, pool.size()));

        CompletableFuture<String> winner = new CompletableFuture<>();

        List<CompletableFuture<Void>> checks = candidates.stream().map(url ->
                PROBE.sendAsync(
                        HttpRequest.newBuilder(URI.create(url))
                                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                                .timeout(Duration.ofSeconds(4))
                                .header("User-Agent", "Mozilla/5.0")
                                .build(),
                        HttpResponse.BodyHandlers.discarding())
                        .thenAccept(resp -> {
                            if (resp.statusCode() == 200) {
                                winner.complete(url);
                            } else {
                                BROKEN.add(url);
                            }
                        })
                        .exceptionally(ex -> { BROKEN.add(url); return null; })
        ).toList();

        // If all checks finish without a winner, resolve to null
        CompletableFuture.allOf(checks.toArray(new CompletableFuture[0]))
                .thenRun(() -> winner.complete(null));

        return winner.join();
    }

    private static final List<String> GIFS = List.of(
        "https://media.discordapp.net/attachments/1469982958214709328/1477710928547156062/nk-speechbubble-225nsxly.gif?ex=6a361d4b&is=6a34cbcb&hm=ace868ac8531428438ce97dbee567f8233d06e6f994dbb160f8afe32b5291cc6&",
        "https://media.discordapp.net/attachments/1425003642976600159/1441212520449839265/attachment.gif?format=webp&animated=true&ex=6a367d01&is=6a352b81&hm=f1a7c7b1af3b1514f4527696b7e7bf337d4001e996d3b29f42784fdfe529e992&",
        "https://images-ext-1.discordapp.net/external/o-HXnvOEZMI-tglgWnc4YL-MaIPIdb7Autm1kRrFyJ4/https/media.tenor.com/Q6l4Gz7q25IAAAPo/furry-cute.mp4",
        "https://images-ext-1.discordapp.net/external/sQhv2EljTCGght9kgC1XloPn2cq9UVYRlwCDq1RVj7k/https/media.tenor.com/6bEaFOS6XoEAAAPo/wypher.mp4",
        "https://images-ext-1.discordapp.net/external/N2r6h5gFsHAPcPzOCmNusl3F6h1kCWCd0sKio16WNYA/https/media.tenor.com/MbKCS1WWJ4EAAAPo/furry-femboy.mp4",
        "https://media.discordapp.net/attachments/923024902401966114/972767523583950908/skay2.gif?format=webp&animated=true&ex=6a36465a&is=6a34f4da&hm=0f0043966fff31ce66d1f406e639c254be0fa6b86a73b30d8be636365eb407c7&",
        "https://media.discordapp.net/attachments/1106158902745247764/1412161023498256414/FEMBOY-ARMY-DISCORD-22.gif?format=webp&animated=true&ex=6a3644b0&is=6a34f330&hm=9e60a7088d9c8380386cd35b760a9711b299150c2de733108111000cf534dce3&",
        "https://media.discordapp.net/attachments/1444016097694318613/1449910362060099645/FEMBOY-ARMY-DISCORD-2.gif?format=webp&animated=true&ex=6a367d80&is=6a352c00&hm=b347f29b09ac4b43a22cc9f81bc173118fcc07ea3f8594ae30acaaa20533aa14&",
        "https://media.discordapp.net/attachments/1444016097694318613/1455838670862028903/FEMBOY-ARMY-DISCORD-28.gif?format=webp&animated=true&ex=6a364deb&is=6a34fc6b&hm=c555e95b27cc38d75fb3a30cd37b3938b332764b345c6c0191327302a13c2167&",
        "https://media.discordapp.net/attachments/1444016097694318613/1455838607884554344/FEMBOY-ARMY-DISCORD-37.gif?format=webp&animated=true&ex=6a364ddc&is=6a34fc5c&hm=45b68bf932f08c7c167fa90d286c988c33858fdc2b78ac6a500bb1ed65c8e152&",
        "https://media.discordapp.net/attachments/1409638806055030834/1486992358515015680/FEMBOY-ARMY-DISCORD-3.gif?format=webp&animated=true&ex=6a36430d&is=6a34f18d&hm=e55a2c131ffe605602fe4e38ae75f4c82b757d9c998928174f0f2ad3fb20cbe1&",
        "https://media.discordapp.net/attachments/1373844326697799720/1488525922603630672/FEMBOY-ARMY-DISCORD-1.gif?format=webp&animated=true&ex=6a36914b&is=6a353fcb&hm=bc47797d423117f4950e7551f37db64f91f60db3e8218894ccf2d968f464236b&",
        "https://media.discordapp.net/attachments/1493996113663889471/1512799512916263066/attachment_1.gif?format=webp&animated=true&ex=6a368b56&is=6a3539d6&hm=a6ee2b118ed75bcbe20d12c7a9674b08251170d8b214e2486b51ee8b46d58a80&",
        "https://media.discordapp.net/attachments/1493996113663889471/1512799218782437396/ssstwitter.com_1745661778185.gif?format=webp&animated=true&ex=6a368b10&is=6a353990&hm=aa938f092dbafe7496987298b3ae9e05af6cba8f93e05b6baf9df85431e945d3&",
        "https://media.discordapp.net/attachments/1493996113663889471/1512458406210965514/pizza.gif?format=webp&animated=true&ex=6a35f668&is=6a34a4e8&hm=0f0043966fff31ce66d1f406e639c254be0fa6b86a73b30d8be636365eb407c7&",
        "https://media.discordapp.net/attachments/1309163660618367118/1310019307874553946/attachment-10.gif?format=webp&animated=true&ex=6a3674cd&is=6a35234d&hm=672b95c336e7b9c213d10a3e492467b4e27afbadc9b9e33c7f56d34a718b1330&",
        "https://images-ext-1.discordapp.net/external/d9w_cbnM5e3mulsp3aYLYT84YYCR-7y6UTzn4Lw0UUI/https/gif.fxtwitter.com/tweet_video/HKHieQwbUAAZFS8.webp?animated=true",
        "https://media.discordapp.net/attachments/1106158902745247764/1279090559369089044/FEMBOY-ARMY-DISCORD-5.gif?format=webp&animated=true&ex=6a35ffa9&is=6a34ae29&hm=cfe25c427ec316166b0cbe49f20042aee66e65c46beac35b194b8bfbd36f802c&",
        "https://media.discordapp.net/attachments/1409638806055030834/1440333215490510949/FEMBOY-ARMY-DISCORD-1.gif?format=webp&animated=true&ex=6a3695d6&is=6a354456&hm=0595694018e7cce70ea2991e7e528a46672275e55705d9a8697813d47730d7db&",
        "https://media.discordapp.net/attachments/867746422027452426/1123393720750985287/wawawawa.gif?format=webp&animated=true&ex=6a3678ae&is=6a35272e&hm=38ebaa12fc3302f42eda4171a9823a777add54c70e8838b50e32971a56b7c139&",
        "https://images-ext-1.discordapp.net/external/3eChG2eEDWgZr4Khhs8QhGRGchdQgxiaYO2xqgWUXwc/https/media.tenor.com/F-ODInPlXQwAAAPo/%25D1%2584%25D1%2583%25D1%2580%25D1%2580%25D0%25B8-furry.mp4",
        "https://media.discordapp.net/attachments/1280073587121721436/1364133067161079818/20250420_013825.gif?format=webp&animated=true&ex=6a3639eb&is=6a34e86b&hm=26b84539bf6ab46630b81cb29600e28bf99e769cfa21b9d2b585e8cc288ac901&",
        "https://media.discordapp.net/attachments/1493996113663889471/1511436744577384619/attachment.gif?format=webp&animated=true&ex=6a363369&is=6a34e1e9&hm=c5d886e37e6605d6231d3f2a62d4a5b73785ab3012a80d7b138783f00bdafd4b&",
        "https://images-ext-1.discordapp.net/external/A5hK62TLKkWIk87gWCcNm6SNex9pVtvpMXVf-uc1a7E/https/media.tenor.com/ZUJ9xXRh3doAAAPo/wypher-koiwypher.mp4",
        "https://media.discordapp.net/attachments/1503870920756957204/1511361847536783463/cxzczxcatw.gif?format=webp&animated=true&ex=6a369668&is=6a3544e8&hm=e4ce6dccaacbc110499f170b2006f37767f10ea2f0758d89a4e34a4477beedef&",
        "https://media.discordapp.net/attachments/1177268170579529780/1250173495011442820/attachment.gif?format=webp&animated=true&ex=6a36448c&is=6a34f30c&hm=cf58b3a2e1aa9a6b5dbc0cd20183651a6793ecd8dcaafa67157b7a4ff1d2e175&",
        "https://media.discordapp.net/attachments/1264660325660098692/1499427737285427351/speechmemified_0f9732ae94cd50e9c54c54584ba85ab9.gif?format=webp&animated=true&ex=6a3604a6&is=6a34b326&hm=7d2675bd9aa8bfba9291239538a4462de7556b46d82ec7bb0dceb82fe072e5b5&",
        "https://media.discordapp.net/attachments/1230833882056757341/1368035503135522816/attachment-49.gif?format=webp&animated=true&ex=6a369498&is=6a354318&hm=359d0895535b593b210ea873f24b4e998f085ddb5a25ef3112f218abd0090a45&",
        "https://media.discordapp.net/attachments/1302891890206707715/1312286732103979019/attachment.gif?format=webp&animated=true&ex=6a3622c2&is=6a34d142&hm=f1822d1e5c68e4097092ab4a78f28e73fc983b2dbf2ea589553c263f29af8c10&",
        "https://images-ext-1.discordapp.net/external/q4TcCkhqh68-k-pgD5EqD5BsW_F4qefW7aQArx-37jk/https/media.tenor.com/S45gEq9hpVUAAAPo/furry-discord.mp4",
        "https://media.discordapp.net/attachments/1287170529760903251/1347038171472466021/mauzymice-silly.gif?format=webp&animated=true&ex=6a35ff8e&is=6a34ae0e&hm=32340eee961c1b97cf6551b1f6b54572bdceeffa387521e9a34f4f2374131c7a&",
        "https://media.discordapp.net/attachments/819068116251836439/1302306461002633346/FBD76553-2FCF-441A-9FFF-D125804575D5.gif?format=webp&animated=true&ex=6a361527&is=6a34c3a7&hm=55dc86cf492b1130298714a0dd5e64342a1280c6a3019f69f4c651774eab2e47&",
        "https://media.discordapp.net/attachments/1285246838408089715/1334862294492123146/GIF_20240723_133333_331.gif?format=webp&animated=true&ex=6a3686e3&is=6a353563&hm=03cf8bdb9f365520e39c96af02537aa0313edef51e22d013c17b9bd4f432ef20&",
        "https://media.discordapp.net/attachments/1091441098829856949/1348118261333758022/attachment-3_1.gif?format=webp&animated=true&ex=6a35f8f8&is=6a34a778&hm=c4d39f4b79b3b42dc00238157af50dd3090a9193ff43ec60166cb006c2cd3b8c&",
        "https://media.tenor.com/3LUQu7EUAqsAAAAC/sagu4433-furry.gif",
        "https://media.tenor.com/sM7taTnCzBgAAAAC/boykisser-furry.gif",
        "https://media.tenor.com/vJWlisL6iNgAAAAC/femboy-furry.gif",
        "https://media.tenor.com/IbvKVfS0h1EAAAAC/furry-wypher.gif",
        "https://media.discordapp.net/attachments/1422961659219280105/1423869266318327879/8elEfXp-1.gif?format=webp&animated=true&ex=6a360417&is=6a34b297&hm=89e1817496129eecb76672216635ed2196a44aaa08bc41065cf7ae8f1ca6248d&",
        "https://media.discordapp.net/attachments/724885059861086221/971829672696508488/Yeatrue-1-1-1.gif?format=webp&animated=true&ex=6a3628a9&is=6a34d729&hm=b235440befe44de65566772bfcc9cfd8d156e5b802b3ca7f0a277592280cda78&",
        "https://media.tenor.com/dWqnh5b1wHYAAAPs/boy-kiss.webm",
        "https://media.tenor.com/2OhsawIojFoAAAPs/me-when-i-get-you-boykisser.webm",
        "https://media.discordapp.net/attachments/1185217399633489950/1221280494130434068/attachment-2-2.gif?format=webp&animated=true&ex=6a35f718&is=6a34a598&hm=8d00162e12b750332a4872a4f12c01c64520de39ee4128d994cece73882673ac&",
        "https://images-ext-1.discordapp.net/external/Y27My5b8tCXH1cLKwUXH-pdKV4-7c73hMa7ZEZWCe1w/https/media.tenor.com/V3Yy5xSdJ3UAAAPo/silent.mp4",
        "https://images-ext-1.discordapp.net/external/pX8ouYy5yD44r56i0tlIch3DwLKqraBqU6qFqMpSd9g/https/media.tenor.com/5qKqcb19N7gAAAPo/cheese-burger-yum.mp4",
        "https://media.tenor.com/Ya8wgmZrzrQAAAPs/n-no-boykisser.webm",
        "https://images-ext-1.discordapp.net/external/P6pVPdg2FzALdE-TD-ZYMp9mPaJkUGQCSziz1hMjYk8/https/media.tenor.com/qOExcG78x8YAAAPo/shut-up-homie.mp4",
        "https://media.discordapp.net/attachments/1416912744216793162/1498818225591291984/attachment.gif?format=webp&animated=true&ex=6a366fff&is=6a351e7f&hm=b99010fa4cd020e909d66582557c19fa1698a9681c8dab06a9f001dff559a129&",
        "https://images-ext-1.discordapp.net/external/DfdsWV7xzs1tCVuinSM8TmZgH41bRkp39PUExwS9P8o/https/media.tenor.com/cbTNRRokmHwAAAPo/boykisser.mp4",
        "https://images-ext-1.discordapp.net/external/dbljWJXXMfGIBHGM0abihvRPwSEjf5UBrbD6bpTr1GI/https/media.tenor.com/Mw5wQ4Z6nDMAAAPo/boykisser-angy.mp4",
        "https://media.discordapp.net/attachments/1278907375948009503/1420159194774110250/attachment.gif?format=webp&animated=true&ex=6a365c91&is=6a350b11&hm=0ec8d3bec9a6eedb6e3f38497e3fc09afc99e67aa1885e3ab3ffb141319491d1&",
        "https://images-ext-1.discordapp.net/external/DDqJzCwUXyaBNqd0m667uJyW1kXpN9rs1Em4AS6Ww9w/https/media.tenor.com/_NRJIzd6gN0AAAPo/headpats.mp4",
        "https://images-ext-1.discordapp.net/external/lQOiHPt3N9cbkhCubqRIyn133cM9mo0coxRmtRPNf8Y/https/media.tenor.com/S2dCDX62z3UAAAPo/furry-kiss-furr.mp4",
        "https://media.discordapp.net/attachments/1212216012481630289/1260790085113221200/dedqt5d-a100e67f-7b1d-4ce9-8eef-4edfe19f3852.gif?format=webp&animated=true&ex=6a35ffc4&is=6a34ae44&hm=57367230ab358ac673692db61ca9274fb78595ac25aa4099960a4f53256b1ce3&",
        "https://media.discordapp.net/attachments/1268028947489357986/1285997668807606303/IMG_2095.gif?format=webp&animated=true&ex=6a3613e8&is=6a34c268&hm=8afcbb68bbcb6223c70a8c13fd5214f1cfa596e6217b5223463ae7929bd45e81&",
        "https://images-ext-1.discordapp.net/external/LN-Kv89d8dPgAn43k08F3oxHbrDYZ85fI2hMojDObkY/https/media.tenor.com/la-8MgilWWAAAAPo/furry-furry-cuddle.mp4",
        "https://media.discordapp.net/attachments/1092956060492501043/1153829205595922484/attachment-126.gif?format=webp&animated=true&ex=6a3673ef&is=6a35226f&hm=8e6f74271efcd93e94de11b6c001cba92c23ee0e5c126c346ff311a009a3519a&",
        "https://media.discordapp.net/attachments/1503870920756957204/1504371151697084547/attachment.gif?format=webp&animated=true&ex=6a363450&is=6a34e2d0&hm=040fe02201e44b19bfc2bf1b445cd6fc1f0d87235c116e587128180d1c3281c8&",
        "https://media.discordapp.net/attachments/1485232577492156458/1488901241722503188/HEg6JkUaYAAwxAX.gif?format=webp&animated=true&ex=6a35f496&is=6a34a316&hm=5881217e3b74472cfaad5de2a29b9db668b38d31e96252f6a1292e49c8b07f9b&",
        "https://media.discordapp.net/attachments/1139219968592969739/1156843212506402916/attachment.gif?format=webp&animated=true&ex=6a363632&is=6a34e4b2&hm=ac5ce94fc645ef1728592da4ceacfc65c1fefc73170a0e9e79449caae4c09719&",
        "https://media.discordapp.net/attachments/191334391278993408/1472689812325662831/heybro.gif?format=webp&animated=true&ex=6a364e04&is=6a34fc84&hm=de7be6b297597878088b59185d49b773c542c4efeecad3088e93bae0f2c66c07&",
        "https://media.discordapp.net/attachments/1287170529760903251/1470104027755839723/attachment-7.gif?format=webp&animated=true&ex=6a362051&is=6a34ced1&hm=580e8a18a580ad9ad91f767733ac54421a3e87876308c209dbedffddac1acb8a&",
        "https://media.discordapp.net/attachments/1218223186554916924/1454411345951981701/attachment.gif?format=webp&animated=true&ex=6a36629e&is=6a35111e&hm=86788e0e0560435d31afae86585e4d6a4335f4c795822046662e39f0021fa4c7&",
        "https://media.discordapp.net/attachments/1285813086019649577/1295832910775058482/attachment-19.gif?format=webp&animated=true&ex=6a364331&is=6a34f1b1&hm=9a0d8369c420c0e088898f75492c4d99eac78b5e095be20c5d8e69d8433080d3&",
        "https://media.discordapp.net/attachments/1425003642976600159/1492726566730469376/attachment.gif?format=webp&animated=true&ex=6a360773&is=6a34b5f3&hm=b513a7519f4334ab67c17d1672b7afc514ee9057d346c19c3faf2727f346b4e0&",
        "https://media.discordapp.net/attachments/1425003642976600159/1500713704834994226/attachment.gif?format=webp&animated=true&ex=6a36150d&is=6a34c38d&hm=752d4bdf3edd3792038222e9b84a25619b836325204bc31bbe738cfd8f8d18d5&",
        "https://media.discordapp.net/attachments/1284069945105449002/1499888427678699641/attachment.gif?format=webp&animated=true&ex=6a366033&is=6a350eb3&hm=b74958793601d89b164615fe453fb736db4c724854b1a852dc71445bbcf76a2a&",
        "https://media.discordapp.net/attachments/1425003642976600159/1496326350682718228/attachment.gif?format=webp&animated=true&ex=6a3699c2&is=6a354842&hm=170b0b9a660c287602de5a587a87d19992246e60288f2c3cfc58b73358005b21&",
        "https://media.discordapp.net/attachments/1091441098829856949/1442945269074493450/hLYmqIOjSL3Erwe7.gif?format=webp&animated=true&ex=6a363340&is=6a34e1c0&hm=935c31067fa0d19aa357bb0d5955db0c00895ac9fae17500d4b72cfb76e0b29a&",
        "https://media.discordapp.net/attachments/1173359712998281386/1437346866265067612/speechmemified_Captura_de_pantalla_2025-11-10_024242.gif?format=webp&animated=true&ex=6a364495&is=6a34f315&hm=b2484a1c2be07b3333addfaf4b9c777e2e6bbd5dfa4295796ccb40b1e17b83ac&",
        "https://media.discordapp.net/attachments/1365001935081705663/1464312613012897935/2023-02-07_171606991.gif?format=webp&animated=true&ex=6a3626a4&is=6a34d524&hm=626e841be8b121f41665f5c26c8047e7e1891be8cea2763b4533e23ee4cb7777&",
        "https://media.discordapp.net/attachments/1503050079433396225/1503735735054893126/eepydogboyyo.gif?format=webp&animated=true&ex=6a368789&is=6a353609&hm=a0a77093ac1d76308c95a912dc9473dbf1dc89ee72ef70120eb7c3cbffd445a8&",
        "https://media.discordapp.net/attachments/1181966310914134046/1339189062237687902/carpi_gif_1_bubbler.gif?format=webp&animated=true&ex=6a367281&is=6a352101&hm=952eb56e3f48ceac4b3e5cdcacf6368fab45c40162a695a6cf187787e3483ca9&",
        "https://images-ext-1.discordapp.net/external/FWWT0Gh_4RvmjLWhKcRmeSMVrWz3SlGxLu6AIlKmbBA/https/media.tenor.com/OrJiTqPixtYAAAPo/hugs-love.mp4",
        "https://media.discordapp.net/attachments/1186843739369513043/1288362132752306216/attachment-50.gif?format=webp&animated=true&ex=6a361c3d&is=6a34cabd&hm=3cc216c03c4167ef6be5c733d006cc1b5814c3cd8cabbab58c09e60adfdafc8f&",
        "https://media.discordapp.net/attachments/1408564139143463099/1418201214516203642/IMG_2966.gif?format=webp&animated=true&ex=6a367d4e&is=6a352bce&hm=37d0505cbe7ee64938257a5832b20b5e31680a3c7000f0abfd791ac8000e941a&",
        "https://media.discordapp.net/attachments/1409518176185745478/1409537171060428902/fuckyou.gif?format=webp&animated=true&ex=6a35f389&is=6a34a209&hm=993690c5359f626f7a0c2665baf24a675cf573432c81d3018874de3bd9968bd0&",
        "https://media.discordapp.net/attachments/1287169120206459021/1503179809449185492/siquesis-boykisser-explode.gif?format=webp&animated=true&ex=6a367c0a&is=6a352a8a&hm=4649c74be20b6af28746aa882dbc4d0755e55aea2da5ced01c420f9cedc218b4&",
        "https://media.discordapp.net/attachments/1268983316174995541/1381754449638461510/caption.gif?format=webp&animated=true&ex=6a36645c&is=6a3512dc&hm=fcc497067e234dea7e7c0e2b3374623da289e3454f21efb894026572e911a5d8&",
        "https://media.discordapp.net/attachments/940120698272219146/1089214066964304043/UwU_take.gif?format=webp&animated=true&ex=6a360d66&is=6a34bbe6&hm=f8cb12d3919c2d525af42ef8c8132d75e5f56ef8c7bd31fb17ac26f5ee63fa1c&",
        "https://media.discordapp.net/attachments/1482739203828879603/1492909908788777135/ooattachment.gif?format=webp&animated=true&ex=6a360973&is=6a34b7f3&hm=1d05f069fe5a4d4c8371ea07365fc9e30d77e40a3d37df065084a62af2d98c38&",
        "https://images-ext-1.discordapp.net/external/KMVz3mpk5qCi8bQmlCevAEB1k4RwSQx_jzPoQwwyXVY/https/media.tenor.com/FraPRlGRUSQAAAPo/crystal-the-cavern-spirit.mp4",
        "https://media.discordapp.net/attachments/1333241809215819816/1380739134078980197/attachment.gif?format=webp&animated=true&ex=6a35fe86&is=6a34ad06&hm=324dcc5f5fed1c3dccceeb9f1e90ac87a73db77bc5cd642173e505315cad440f&",
        "https://images-ext-1.discordapp.net/external/V1QX4Q4IEYaSzCzDN9qVvPIhZ0Lpv7ps-1Ir6U7eBHE/https/media.tenor.com/3DzhsHs_8G8AAAPo/boykisser-spin.mp4",
        "https://images-ext-1.discordapp.net/external/Tnof0yiia1iqJTN9u3PJhrEJOXmdLvsBvONBsv9e_8w/https/media.tenor.com/SFsmQdwxd8YAAAPo/boykisser-dance.mp4",
        "https://media.discordapp.net/attachments/1369706882427650190/1371159043585871962/image-removebg-preview_-_2022-11-11T203916.463.gif?format=webp&animated=true&ex=6a36141e&is=6a34c29e&hm=5cc85af59040147e95cdf0d56c04607456b3c9d844262afddc5309125b964ecc&",
        "https://media.discordapp.net/attachments/1366627402523873422/1366627589644619867/SPOILER_SPOILER_IMG_5751.gif?format=webp&animated=true&ex=6a3612a0&is=6a34c120&hm=73a8424bb84ddb4360e14d4f7d7ab243fb3b417d0ef676e06333cdc144da0185&",
        "https://media.discordapp.net/attachments/959510469943652405/1088183814636961852/17D8EE6C-A6E3-4801-9D01-81502106CF29.gif?format=webp&animated=true&ex=6a364266&is=6a34f0e6&hm=c8fe0501835da25f8e44831dcf28dbc59c40ee6cf82b0114a78db2785de5c146&",
        "https://media.tenor.com/OtUPc7kKL9oAAAPs/boykisser-kiss.webm",
        "https://media.discordapp.net/attachments/1271193303177629726/1281595037926555771/attachment-28.gif?format=webp&animated=true&ex=6a368a63&is=6a3538e3&hm=f9796ecc23217dba67c73cb92032cbf21923159a1e89f067677aa5413b681ad4&",
        "https://media.discordapp.net/attachments/1493996113663889471/1498006607399882964/boikisser.gif?format=webp&animated=true&ex=6a361f1e&is=6a34cd9e&hm=c7646cd9754f6fa38c2055ce3eb3cb973ec886345c95f536196c51de421224f5&",
        "https://media.discordapp.net/attachments/1493996113663889471/1498007191268098240/furry-furries.gif?format=webp&animated=true&ex=6a361faa&is=6a34ce2a&hm=5ed9143bb51427338505f89da1d6d0d90f04844b62d597d66369cdb51b839ae3&",
        "https://media.discordapp.net/attachments/1493996113663889471/1499526163406782484/ooattachment.gif?format=webp&animated=true&ex=6a366051&is=6a350ed1&hm=f7c6b93f0d5c7b5118f18bdbd2e812a0cfc139bc14cfc219e8704dc28055c93b&",
        "https://media.discordapp.net/attachments/1342876502374486037/1342879444661964852/attachment.gif?ex=6a3607b1&is=6a34b631&hm=347132254b5da9e8548fc26908b9ba7ccee286337b4b27d9c9811b0c3ab8f551&",
        "https://media.discordapp.net/attachments/1327536250139185172/1355811825622581359/attachment-1.gif?format=webp&animated=true&ex=6a3646a8&is=6a34f528&hm=8f38a9bbb8d4c4e67311ada63214d653ff72585bfc8894c4706e265266bc11b6&",
        "https://media.discordapp.net/attachments/1443839489196822685/1507422603990667414/attachment.gif?ex=6a361973&is=6a34c7f3&hm=403cf0e4e5acc0f0b1aac522298bd3f20461fa1257be47694facdd725fd6df5f&",
        "https://media.discordapp.net/attachments/1443839489196822685/1507422667203018884/attachment.gif?ex=6a361982&is=6a34c802&hm=1e370eda89e02211347766e2688c64d3cb14fdbca72d5932a10e240cca645f11&",
        "https://media.discordapp.net/attachments/1282273469429583964/1422248532722847795/FEMBOY-ARMY-DISCORD.gif?ex=6a360d6a&is=6a34bbea&hm=08508dbec871f9c2d586166f6bac7ec98b1f92791d75f9c5796a711f167d0861&",
        "https://media.discordapp.net/attachments/1511807198991224872/1515333557453717755/47.gif?ex=6a3688d9&is=6a353759&hm=af6d65adafca49bc92ac7cdf8c84d6ddf6bd5e3edde65f1ea6751aa38281df7c&",
        "https://media.discordapp.net/attachments/1511807198991224872/1515332595108548718/114.gif?ex=6a3687f4&is=6a353674&hm=d1712d2a08ad9d97dfb36cbb8d43e940f77cf957cd69040d208b49b1fcc2a614&",
        "https://media.discordapp.net/attachments/1369389276843741267/1480661548992299089/caption.gif?ex=6a364d46&is=6a34fbc6&hm=53ce618f8e24be59b983481265213b9bf14657008f4e361f3746bab007efbfe3&",
        "https://media.discordapp.net/attachments/1503870920756957204/1515358597171773600/30.gif?ex=6a35f76b&is=6a34a5eb&hm=52c2492916e00f8ce762c68e3aeeec6206f99655ec6df1adc35261ad4245ba90&",
        "https://media.discordapp.net/attachments/1503870920756957204/1515358020085874798/116.gif?ex=6a35f6e1&is=6a34a561&hm=275ac106450d332fa751aa4addccac54e6854994f5753265d6856161bdb78bd9&",
        "https://media.discordapp.net/attachments/1503870920756957204/1515357460746076320/108.gif?ex=6a35f65c&is=6a34a4dc&hm=a81f959b528127093146cb9859a15043974f6e8ddd16a09a35aad8042e1a64b4&",
        "https://media.discordapp.net/attachments/1503870920756957204/1515356749346111508/17.gif?ex=6a35f5b3&is=6a34a433&hm=938d467c5a002bfa59164360ed4e30bd707ef50ccbaa08d2a2eca781f5cd1931&",
        "https://media.discordapp.net/attachments/1503870920756957204/1515058423857680426/togif.gif?ex=6a36315c&is=6a34dfdc&hm=dd5996ea4591e53d53259b3af887237014fa1fbdaaba1c1076a40dd9878429e6&",
        "https://media.discordapp.net/attachments/1503870920756957204/1515332978442506300/107.gif?ex=6a36884f&is=6a3536cf&hm=a44915c1478f793f39e58c995bd964dc0827e0d1932fd6adb35ef090678d1b4f&",
        "https://media.discordapp.net/attachments/1511807198991224872/1515689548376445009/33.gif?ex=6a3682e4&is=6a353164&hm=4d67741ce58463ecd05b27223dfddd2428f005021d5363fc240a2026f396a784&",
        "https://media.discordapp.net/attachments/1511807198991224872/1515690139572244560/81.gif?ex=6a368371&is=6a3531f1&hm=84cb06cfe67dd94d151ae3660d4b74fae5eaa3d1cac961ea375094a8c7c64091&",
        "https://media.discordapp.net/attachments/1503870920756957204/1515690557043769354/97.gif?ex=6a3683d4&is=6a353254&hm=0c2297fa4a052d2276c99f109c99a9dcd13184e50f4dbabcdf7ab9da0fb2c9e0&",
        "https://media.discordapp.net/attachments/1511807198991224872/1515697443197423837/5.gif?ex=6a368a3e&is=6a3538be&hm=caec4020461aa0d3141fa0b4dc5806445de0552617d7c410d523dc9103f68336&",
        "https://media.discordapp.net/attachments/1511807198991224872/1515698941877026846/33.gif?ex=6a368ba4&is=6a353a24&hm=0a4ff057ac4dea251022f711af8eb1b50677498367265caf2c45c3fb2a901139&",
        "https://media.discordapp.net/attachments/1511807198991224872/1515701685178077184/79.gif?ex=6a368e32&is=6a353cb2&hm=c654bac6a15f7fdd703afa4ba35b210a9a0b22ccfd8877ba4a52b78d60c7fbcc&",
        "https://media.discordapp.net/attachments/979033239559761920/1073190491417485362/attachment-6.gif?format=webp&animated=true&ex=6a366d08&is=6a351b88&hm=532c88e8eca6a0d2584d4fcaf96077c9ef1d5e5c021acd199f964287a88052f1&",
        "https://media.discordapp.net/attachments/414048461412106240/1268089516430721174/attachment.gif?format=webp&animated=true&ex=6a362fe4&is=6a34de64&hm=80e567eda3152f7f8ba4e26fd774a2597d20d2dca219ad0a55af953ce9477a47&",
        "https://media.discordapp.net/attachments/919335494473621564/1207419411653206046/attachment-13-1.gif?format=webp&animated=true&ex=6a364bb4&is=6a34fa34&hm=b9379c91c6f6b65c6148d3b58802c08007a1e4b2ed3680c09e2538328d307085&",
        "https://media.discordapp.net/attachments/1009383133726130216/1154872001295552573/7CFCAA29-8AED-4E57-A5A1-33707DEF0821.gif?format=webp&animated=true&ex=6a364a9d&is=6a34f91d&hm=227cbed3ead4297bfab4a12b2ec5401f7d850ac4cf1cb70d8df4ce2eba8c363b&",
        "https://media.discordapp.net/attachments/1238992591064662086/1345094873664979054/caption.gif?format=webp&animated=true&ex=6a362df8&is=6a34dc78&hm=082f4b6bc79d1adb6578b0953152b0615c57d9ac69fd48220a0c73181099fc1c&",
        "https://media.discordapp.net/attachments/1099822350465253391/1495787634591797308/attachment.gif?ex=6a35f58a&is=6a34a40a&hm=044f86b33c7855c524f44645d4cd08cafba6035a38cf41398e32b2c8fe21d481&",
        "https://media.discordapp.net/attachments/1498738791622381744/1499842465073725491/johnassault-1.gif?format=webp&animated=true&ex=6a363565&is=6a34e3e5&hm=5349abeee584ae0af18334a4c8dde5a0122bb39c69c90fd415d394f16c152a7f&",
        "https://media.discordapp.net/attachments/1284069945105449002/1516344675152166933/attachment.gif?format=webp&animated=true&ex=6a364206&is=6a34f086&hm=2c7567985330f2851e13bf8d1ebd9858ca8f76569bf3dc4e155fbbe5e83fdcd7&",
        "https://media.discordapp.net/attachments/1284069945105449002/1517153877814939738/attachment.gif?ex=6a3690a7&is=6a353f27&hm=c327ca45a62c538dcc46e94d08cd316a098bd1e295cf1ee3ce0ed12aac7677b5&"
    );

    public Gallery() {
        addClassName("gallery-view");
        setPadding(false);
        setSpacing(false);

        H1 title = new H1("Gallery");
        title.addClassName("page-title");
        Paragraph subtitle = new Paragraph("Premade projects and integrations, ready to drop in.");
        subtitle.addClassName("page-subtitle");

        Div header = new Div(title, subtitle);
        header.addClassName("docs-header");

        String discord = "https://dsc.gg/spashapi";

        Div content = new Div(
            section("Executor APIs", "Wrapper libraries for supported executors.",
                List.of(
                    Project.ready("Velocity",        "Full .NET wrapper for the Velocity executor.",       "API", discord, "velocity"),
                    Project.ready("Madium",          "Full .NET wrapper for the Madium executor.",         "API", discord, "madium"),
                    Project.ready("Ronix",           "Full .NET wrapper for the Ronix executor.",          "API", discord, "ronix"),
                    Project.ready("Xeno",            "Full .NET wrapper for the Xeno executor.",           "API", discord, "xeno"),
                    Project.ready("SpashAPIInternal","Internal Roblox module for executor development.",   "API", discord, null),
                    Project.soon("Pluto",            "Pluto executor API support, coming soon."),
                    Project.soon("Solara",           "Solara executor API support, coming soon.")
                )),
            section("Injectors", "Injection tooling.",
                List.of(
                    Project.soon("Injector", "WPF/WinForms injection tooling, coming soon.")
                ))
        );
        content.addClassName("gallery-content");

        Div body = new Div(content);
        body.addClassName("gallery-body");

        String gifUrl = pickUrl();
        if (gifUrl != null) {
            Span gifLabel = new Span("meow :3");
            gifLabel.addClassName("furry-label");

            Div gifDisplay = new Div();
            gifDisplay.addClassName("furry-gif-display");

            if (gifUrl.contains(".mp4") || gifUrl.contains(".webm")) {
                gifDisplay.add(new Html("<video src=\"" + gifUrl + "\" autoplay loop muted playsinline></video>"));
            } else {
                gifDisplay.add(new Image(gifUrl, ""));
            }

            Div gifCard = new Div(gifLabel, gifDisplay);
            gifCard.addClassName("furry-gif-card");
            body.add(gifCard);
        }

        add(header, body);
    }

    private Div section(String heading, String desc, List<Project> projects) {
        Div section = new Div();
        section.addClassName("gl-section");

        H2 h = new H2(heading);
        h.addClassName("gl-section-title");
        Paragraph d = new Paragraph(desc);
        d.addClassName("gl-section-desc");
        section.add(h, d);

        Div list = new Div();
        list.addClassName("gl-list");
        for (Project p : projects) list.add(row(p));
        section.add(list);
        return section;
    }

    private Div row(Project p) {
        Div row = new Div();
        row.addClassName("gl-row");
        if (p.isSoon()) row.addClassName("gl-row-soon");

        Div info = new Div();
        info.addClassName("gl-info");
        H3 name = new H3(p.name());
        name.addClassName("gl-name");
        Paragraph desc = new Paragraph(p.desc());
        desc.addClassName("gl-desc");
        info.add(name, desc);

        Span tag = new Span(p.tag());
        tag.addClassName("gl-tag");

        Div actions = new Div(tag);
        actions.addClassName("gl-actions");

        if (!p.isSoon()) {
            Anchor dl = new Anchor(p.downloadUrl(), "Download");
            dl.setTarget("_blank");
            dl.addClassNames("gl-btn", "gl-btn-primary");
            actions.add(dl);

            String docsHref = p.docsTab() != null ? "/docs?tab=" + p.docsTab() : p.downloadUrl();
            Anchor docs = new Anchor(docsHref, "Docs");
            if (p.docsTab() == null) docs.setTarget("_blank");
            docs.addClassNames("gl-btn", "gl-btn-secondary");
            actions.add(docs);
        }

        row.add(info, actions);
        return row;
    }
}
