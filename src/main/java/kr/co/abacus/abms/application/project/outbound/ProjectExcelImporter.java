package kr.co.abacus.abms.application.project.outbound;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import kr.co.abacus.abms.domain.project.ProjectCreateRequest;

public interface ProjectExcelImporter {

    List<ProjectCreateRequest> importProjects(InputStream inputStream, Function<String, Long> partyLookup);

}
