package com.example.stack.facade;

import com.example.stack.repository.LockRepository;
import com.example.stack.service.StockService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NamedLockStockFacade {

    private final LockRepository lockRepository;

    private final StockService stockService;

    public NamedLockStockFacade(LockRepository lockRepository, StockService stockService) {
        this.lockRepository = lockRepository;
        this.stockService = stockService;
    }

    @Transactional
    public void decrease(Long id, Long quantity) {
        try {
            lockRepository.getLock(id.toString());
            stockService.decrease(id, quantity);
        } finally {
            lockRepository.releaseLock(id.toString());
        }
    }
}
