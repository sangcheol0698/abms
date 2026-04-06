package kr.co.abacus.abms.application.weeklyreport.inbound;

import java.util.List;

import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportDraftDetail;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportDraftListItem;

public interface WeeklyReportDraftFinder {

    List<WeeklyReportDraftListItem> findRecentDrafts(Long accountId);

    WeeklyReportDraftDetail findDraftDetail(Long accountId, Long draftId);
}
