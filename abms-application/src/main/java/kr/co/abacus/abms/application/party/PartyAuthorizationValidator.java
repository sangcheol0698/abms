package kr.co.abacus.abms.application.party;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.auth.CurrentActorPermissionSupport;

@RequiredArgsConstructor
@Component
class PartyAuthorizationValidator {

    private static final String PARTY_WRITE_PERMISSION_CODE = "party.write";

    private final CurrentActorPermissionSupport permissionSupport;

    void validateCreate(CurrentActor actor) {
        validateWrite(actor, "협력사 생성 권한이 없습니다.");
    }

    void validateUpdate(CurrentActor actor) {
        validateWrite(actor, "협력사 수정 권한이 없습니다.");
    }

    void validateDelete(CurrentActor actor) {
        validateWrite(actor, "협력사 삭제 권한이 없습니다.");
    }

    private void validateWrite(CurrentActor actor, String message) {
        permissionSupport.requirePermission(actor, PARTY_WRITE_PERMISSION_CODE, message);
    }
}
