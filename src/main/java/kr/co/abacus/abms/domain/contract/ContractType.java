package kr.co.abacus.abms.domain.contract;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContractType {
    INITIAL("최초"),
    AMENDMENT("변경");

    private final String description;
}
