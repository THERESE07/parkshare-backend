package rw.parkshare.backend.common.dto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Function;

/**
 * Stable JSON shape for paged lists: ?page=0&size=20 (size is capped at 100).
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static final int MAX_SIZE = 100;

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    /** Builds a safe page request; newest records first by default. */
    public static Pageable request(int page, int size, String sortProperty) {
        int safeSize = Math.max(1, Math.min(size, MAX_SIZE));
        return PageRequest.of(Math.max(0, page), safeSize, Sort.by(Sort.Direction.DESC, sortProperty));
    }
}
