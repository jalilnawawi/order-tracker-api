package id.sevenspeed.tracking.dto.response.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Satu tahap pada timeline order yang sudah di-precompute untuk customer.
 * Tanpa nama operator (privasi customer). Lihat kontrak FE §6.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderTimelineStepResponse {

    private String stepName;
    private String status; // DONE | CURRENT | UPCOMING | FAILED | ON_HOLD
    private String message; // kalimat ramah Bahasa Indonesia
    private String at; // waktu event terakhir tahap ini; null kalau belum tersentuh
}