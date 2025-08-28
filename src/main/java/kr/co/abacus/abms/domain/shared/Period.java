package kr.co.abacus.abms.domain.shared;

import java.time.LocalDate;

import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

@Embeddable
public record Period(LocalDate startDate, @Nullable LocalDate endDate) {

}
