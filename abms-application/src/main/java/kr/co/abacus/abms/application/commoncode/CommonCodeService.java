package kr.co.abacus.abms.application.commoncode;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.co.abacus.abms.application.commoncode.dto.CommonCodeInfo;
import kr.co.abacus.abms.application.commoncode.outbound.CommonCodeDetailRepository;
import kr.co.abacus.abms.domain.commoncode.CommonCodeDetail;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommonCodeService {

    private final CommonCodeDetailRepository commonCodeDetailRepository;

    @Cacheable(cacheNames = "commonCode", key = "#groupCode")
    public List<CommonCodeInfo> findByGroupCode(String groupCode) {
        log.info("[공통코드 조회] groupCode: {}", groupCode);

        return commonCodeDetailRepository.findByGroupCode(groupCode).stream()
                .map(CommonCodeInfo::from)
                .toList();
    }

    @Cacheable(cacheNames = "commonCode", key = "#groupCode + '_' + #code")
    public @Nullable CommonCodeInfo findByCode(String groupCode, String code) {
        @Nullable CommonCodeDetail detail = commonCodeDetailRepository.findByCode(groupCode, code);
        return detail != null ? CommonCodeInfo.from(detail) : null;
    }

    @CacheEvict(cacheNames = "commonCode", key = "#groupCode")
    public void clearCache(String groupCode) {
        log.info("[공통코드 캐시 초기화] groupCode: {}", groupCode);
    }

}
