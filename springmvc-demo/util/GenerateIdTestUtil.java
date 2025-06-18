
import javax.annotation.Resource;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ActiveProfiles("dev")
@SpringBootTest(classes = MesAdminApplication.class)
class GenerateIdUtilTest {

    @Resource
    private GenerateIdUtil generateIdUtil;

    @Test
    void makeUniqueId() {
        for (int i = 0; i < 10; i++) {
            checkRepeat();
        }
    }

    @Test
    void generateId() {
        for (int i = 0; i < 10; i++) {
            checkRepeat2();
        }
    }

    private void checkRepeat() {
        ConcurrentLinkedDeque<String> idList = new ConcurrentLinkedDeque<>();
        AtomicInteger taskQuantity = new AtomicInteger(1000);
        AtomicInteger threadQuantity = new AtomicInteger(4);
        CountDownLatch countDownLatch = new CountDownLatch(taskQuantity.get());
        for (int threadIdx = 0; threadIdx < threadQuantity.get(); threadIdx++) {
            new Thread(() -> {
                for (int taskIdx = 0; taskIdx < taskQuantity.get() / threadQuantity.get(); taskIdx++) {
                    try {
                        idList.add(generateIdUtil.makeUniqueId("MES_DEMAND_ORDER_TEST"));
                    } catch (RuntimeException e) {
//                    System.out.println("已判断的重复");
                    }
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await(5, TimeUnit.SECONDS);
            HashSet<String> set = new HashSet<>(idList.size());
            for (String s : idList) {
                if (set.contains(s)) {
                    System.out.println("=======" + s + "=======多线程下依然重复");
                } else {
                    set.add(s);
                }
            }
            System.out.println("共有ID条数：" + set.size());
        } catch (InterruptedException e) {
            System.out.println("超时");
        }
    }

    private void checkRepeat2() {
        ConcurrentLinkedDeque<String> idList = new ConcurrentLinkedDeque<>();
        AtomicInteger taskQuantity = new AtomicInteger(1000);
        AtomicInteger threadQuantity = new AtomicInteger(4);
        CountDownLatch countDownLatch = new CountDownLatch(taskQuantity.get());
        for (int threadIdx = 0; threadIdx < threadQuantity.get(); threadIdx++) {
            new Thread(() -> {
                for (int taskIdx = 0; taskIdx < taskQuantity.get() / threadQuantity.get(); taskIdx++) {
                    try {
                        idList.add(generateIdUtil.generateId("MES_DEMAND_ORDER_TEST"));
                    } catch (RuntimeException e) {
//                    System.out.println("已判断的重复");
                    }
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await(5, TimeUnit.SECONDS);
            HashSet<String> set = new HashSet<>(idList.size());
            for (String s : idList) {
                if (set.contains(s)) {
                    System.out.println("checkRepeat2：=======" + s + "=======多线程下依然重复");
                } else {
                    set.add(s);
                }
            }
            System.out.println("checkRepeat2：共有ID条数：" + set.size());
        } catch (InterruptedException e) {
            System.out.println("超时");
        }
    }
}