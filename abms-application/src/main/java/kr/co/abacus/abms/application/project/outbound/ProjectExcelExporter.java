package kr.co.abacus.abms.application.project.outbound;

import java.util.List;
import java.util.Map;

import kr.co.abacus.abms.domain.project.Project;

public interface ProjectExcelExporter {

    byte[] export(List<Project> projects, Map<Long, String> partyNames);

    byte[] exportSample();

}
