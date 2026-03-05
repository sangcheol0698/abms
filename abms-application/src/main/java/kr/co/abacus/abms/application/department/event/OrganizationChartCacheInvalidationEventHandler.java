package kr.co.abacus.abms.application.department.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.inbound.DepartmentFinder;

@Component
@RequiredArgsConstructor
public class OrganizationChartCacheInvalidationEventHandler {

    private final DepartmentFinder departmentFinder;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrganizationChartInvalidationRequestedEvent event) {
        departmentFinder.clearOrganizationChartCache();
    }

}
