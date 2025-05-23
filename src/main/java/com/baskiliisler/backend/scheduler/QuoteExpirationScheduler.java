package com.baskiliisler.backend.scheduler;

import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.repository.QuoteRepository;
import com.baskiliisler.backend.service.QuoteService;
import com.baskiliisler.backend.type.QuoteStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class QuoteExpirationScheduler {

    private final QuoteService quoteService;
    private final QuoteRepository quoteRepo;

    /** Her gece 02:00'de */
    @Scheduled(cron = "0 0 2 * * *")
    public void expireOutdatedQuotes() {

        LocalDate today = LocalDate.now();

        List<Quote> outdated = quoteRepo
              .findByStatusAndValidUntilBefore(QuoteStatus.OFFER_SENT, today);

        if (outdated != null && !outdated.isEmpty()) {
            outdated.forEach(q -> {
                try {
                    quoteService.expireQuote(q.getId());
                } catch (Exception e) {
                    // Log the error but continue with other quotes
                    System.err.println("Failed to expire quote " + q.getId() + ": " + e.getMessage());
                }
            });
        }
    }
}
