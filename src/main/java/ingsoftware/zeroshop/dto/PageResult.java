package ingsoftware.zeroshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> content;
    private int pageNumber;      // 1-indexed
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasPrevious;
    private boolean hasNext;
    private boolean first;
    private boolean last;
    private int fromIndex;
    private int toIndex;

    public static <T> PageResult<T> of(List<T> allItems, int page, int size) {
        if (allItems == null) {
            allItems = Collections.emptyList();
        }
        if (page < 1) page = 1;
        if (size < 1) size = 10;

        int totalElements = allItems.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0) totalPages = 1;
        if (page > totalPages) page = totalPages;

        int start = (page - 1) * size;
        int end = Math.min(start + size, totalElements);

        List<T> pageContent;
        if (start >= totalElements) {
            pageContent = Collections.emptyList();
        } else {
            pageContent = allItems.subList(start, end);
        }

        int fromIdx = totalElements > 0 ? start + 1 : 0;
        int toIdx = totalElements > 0 ? start + pageContent.size() : 0;

        return PageResult.<T>builder()
                .content(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasPrevious(page > 1)
                .hasNext(page < totalPages)
                .first(page == 1)
                .last(page == totalPages)
                .fromIndex(fromIdx)
                .toIndex(toIdx)
                .build();
    }
}
