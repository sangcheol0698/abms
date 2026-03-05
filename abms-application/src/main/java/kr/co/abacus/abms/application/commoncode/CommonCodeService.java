package kr.co.abacus.abms.application.commoncode;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.co.abacus.abms.application.commoncode.dto.CommonCodeInfo;
import kr.co.abacus.abms.application.commoncode.outbound.CommonCodeGroupRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonCodeService {

    private final CommonCodeGroupRepository commonCodeGroupRepository;

    @Cacheable(cacheNames = "commonCode", key = "#groupCode")
    public List<CommonCodeInfo> findByGroupCode(String groupCode) {
        log.info("[공통코드 조회] groupCode: {}", groupCode);

        return commonCodeGroupRepository.findDetailsByGroupCode(groupCode);
    }

    @Cacheable(cacheNames = "commonCode", key = "#groupCode + '_' + #code")
    public @Nullable CommonCodeInfo findByCode(String groupCode, String code) {
        return commonCodeGroupRepository.findDetailByCode(groupCode, code);
    }

    @CacheEvict(cacheNames = "commonCode", key = "#groupCode")
    public void clearCache(String groupCode) {
        log.info("[공통코드 캐시 초기화] groupCode: {}", groupCode);
    }

}
