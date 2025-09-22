export interface PageResponsePayload<T> {
    page: number;
    size: number;
    totalPages: number;
    totalElements: number;
    content: T[];
}

export default class PageResponse<T> implements PageResponsePayload<T> {
    page: number;
    size: number;
    totalPages: number;
    totalElements: number;
    content: T[];

    constructor(payload: PageResponsePayload<T>) {
        this.page = payload.page;
        this.size = payload.size;
        this.totalPages = payload.totalPages;
        this.totalElements = payload.totalElements;
        this.content = payload.content;
    }

    static fromPage<U>(response: any, mapper: (item: any) => U): PageResponse<U> {
        const rawContent = Array.isArray(response?.content) ? response.content : [];
        const size = Number(response?.size ?? response?.pageable?.pageSize ?? rawContent.length ?? 0);
        const number = Number(response?.number ?? response?.page ?? 0);
        const totalPages = Number(response?.totalPages ?? response?.page?.totalPages ?? 0);
        const totalElements = Number(
            response?.totalElements ?? response?.page?.totalElements ?? rawContent.length ?? 0,
        );

        return new PageResponse<U>({
            page: number + 1,
            size,
            totalPages,
            totalElements,
            content: rawContent.map((item: any) => mapper(item)),
        });
    }
}
