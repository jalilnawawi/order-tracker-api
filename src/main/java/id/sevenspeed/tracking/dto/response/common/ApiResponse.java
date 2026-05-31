package id.sevenspeed.tracking.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import id.sevenspeed.tracking.util.DateTimeUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ApiError error;
    private final Meta meta;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .meta(Meta.now())
                .build();
    }

    public static <T> ApiResponse<List<T>> paginated(Page<T> page) {
        return ApiResponse.<List<T>>builder()
                .success(true)
                .data(page.getContent())
                .meta(Meta.paginated(page))
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ApiError.of(code, message))
                .meta(Meta.now())
                .build();
    }

    // ------------------------------------------------------------------

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Meta {
        private final String timestamp;
        private final Pagination pagination;

        public static Meta now() {
            return Meta.builder()
                    .timestamp(DateTimeUtil.now())
                    .build();
        }

        public static Meta paginated(Page<?> page) {
            return Meta.builder()
                    .timestamp(DateTimeUtil.now())
                    .pagination(Pagination.of(page))
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Pagination {
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;

        public static Pagination of(Page<?> page) {
            return Pagination.builder()
                    .page(page.getNumber())
                    .size(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .build();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class FieldError {
        private final String field;
        private final String message;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiError {
        private final String code;
        private final String message;
        private final List<FieldError> fields; // tambah ini

        public static ApiError of(String code, String message) {
            return ApiError.builder()
                    .code(code)
                    .message(message)
                    .build();
        }

        public static ApiError ofValidation(String message, List<FieldError> fields) {
            return ApiError.builder()
                    .code("VALIDATION_ERROR")
                    .message(message)
                    .fields(fields)
                    .build();
        }
    }
}