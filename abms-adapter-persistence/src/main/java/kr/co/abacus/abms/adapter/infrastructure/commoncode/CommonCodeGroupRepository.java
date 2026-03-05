package kr.co.abacus.abms.adapter.infrastructure.commoncode;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.commoncode.CommonCodeGroup;

public interface CommonCodeGroupRepository
        extends Repository<CommonCodeGroup, String>,
        kr.co.abacus.abms.application.commoncode.outbound.CommonCodeGroupRepository {

    @Override
    @Query("SELECT cg " +
            "FROM CommonCodeGroup cg " +
            "WHERE cg.groupCode = :groupCode AND cg.deleted = false")
    Optional<CommonCodeGroup> findByGroupCode(String groupCode);
}
