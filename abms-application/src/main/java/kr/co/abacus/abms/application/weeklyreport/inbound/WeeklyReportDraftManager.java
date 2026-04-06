package kr.co.abacus.abms.application.weeklyreport.inbound;

import kr.co.abacus.abms.application.weeklyreport.dto.command.WeeklyReportGenerateCommand;
import kr.co.abacus.abms.application.weeklyreport.dto.command.WeeklyReportUpdateCommand;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportDraftDetail;

public interface WeeklyReportDraftManager {

    WeeklyReportDraftDetail createDraft(Long accountId, WeeklyReportGenerateCommand command);

    WeeklyReportDraftDetail regenerateDraft(Long accountId, Long draftId);

    WeeklyReportDraftDetail cancelDraft(Long accountId, Long draftId);

    WeeklyReportDraftDetail updateDraft(Long accountId, Long draftId, WeeklyReportUpdateCommand command);

    void deleteDraft(Long accountId, Long draftId);
}
