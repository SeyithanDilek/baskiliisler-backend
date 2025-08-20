package com.baskiliisler.backend.scheduler;

import com.baskiliisler.backend.model.Quote;
import com.baskiliisler.backend.repository.QuoteRepository;
import com.baskiliisler.backend.service.QuoteService;
import com.baskiliisler.backend.service.EmailService;
import com.baskiliisler.backend.type.QuoteStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class QuoteExpirationScheduler {

    private final QuoteService quoteService;
    private final QuoteRepository quoteRepo;
    private final EmailService emailService;

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

    /** Her gün 10:00'da - 2 gün içinde süresi dolacak teklifler için hatırlatma */
    @Scheduled(cron = "0 0 10 * * *")
    public void sendQuoteReminders() {
        LocalDate reminderDate = LocalDate.now().plusDays(2);
        
        List<Quote> expiringQuotes = quoteRepo
              .findByStatusAndValidUntil(QuoteStatus.OFFER_SENT, reminderDate);

        if (expiringQuotes != null && !expiringQuotes.isEmpty()) {
            expiringQuotes.forEach(q -> {
                try {
                    // Müşteriye teklif hatırlatma maili gönder
                    emailService.sendQuoteReminderEmail(
                        q.getBrand().getContactEmail(),
                        q.getBrand().getName(),
                        q.getBrand().getName(),
                        q.getId(),
                        q.getTotalPrice(),
                        q.getValidUntil().atStartOfDay()
                    );
                } catch (Exception e) {
                    // Log the error but continue with other quotes
                    System.err.println("Failed to send reminder for quote " + q.getId() + ": " + e.getMessage());
                }
            });
        }
    }
}
