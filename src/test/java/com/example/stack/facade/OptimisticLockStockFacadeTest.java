package com.example.stack.facade;

import com.example.stack.domain.Stock;
import com.example.stack.repository.StockRepository;
import com.example.stack.service.OptimisticLockStockService;
import com.example.stack.service.PessimisticLockStockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OptimisticLockStockFacadeTest {
    @Autowired
    private OptimisticLockStockFacade stockService;
    // OptimisticLock 장점은 별도의 락을 잡지 않아 비관적 락보다 성능상 장점을 가짐
    // 업데이트가 실패했을 때, 재시도 로직을 개발자가 직접 작성해주어야 함.
    // 충돌이 빈번하게 일어날 것으로 예상되면 비관적 락이 나을 수 있음.
    // 그게 아니라면 OptimisticLock을 추천.

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void insert() {
        Stock stock = new Stock(1L, 100L);

        stockRepository.saveAndFlush(stock);
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @DisplayName("동시에 100개의 요청")
    @Test
    void 동시100요청() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        // then
        assertEquals(90L, stock.getQuantity());
    }
}