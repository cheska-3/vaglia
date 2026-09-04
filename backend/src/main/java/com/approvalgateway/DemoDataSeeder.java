package com.approvalgateway;

import com.approvalgateway.service.ApprovalService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Seeds one example pending approval on startup so the dashboard isn't empty on first run. */
@Component
public class DemoDataSeeder implements CommandLineRunner {

    private final ApprovalService approvalService;

    public DemoDataSeeder(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @Override
    public void run(String... args) {
        approvalService.simulateEmailDraftAutomation(
                "mario.rossi@clienteesempio.it",
                "Buongiorno, volevo sapere quando arriva il mio ordine #4521, sono passati 10 giorni."
        );
        approvalService.simulatePaymentConfirmationAutomation(
                "IT60X0542811101000000123456",
                420.00,
                "Pagamento fornitore — fattura 8832"
        );
    }
}
