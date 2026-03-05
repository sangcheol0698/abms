package kr.co.abacus.abms.adapter.api.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfApi {

    @GetMapping("/api/csrf")
    public ResponseEntity<Void> csrfInit() {
        return ResponseEntity.ok().build();
    }

}
