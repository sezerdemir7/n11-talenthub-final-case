/** Spring benzeri PageResponse / PageResponse DTO parse */
export function parsePageResponse(data) {
  if (!data) return { content: [], totalPages: 0, totalElements: 0, page: 0 };
  const content = data.content ?? data.items ?? data.records ?? [];
  const totalPages =
    data.totalPages ?? data.totalPage ?? (Array.isArray(content) && content.length ? 1 : 0);
  const totalElements = data.totalElements ?? data.total ?? content.length;
  const page = data.page ?? data.number ?? 0;
  return { content, totalPages: Math.max(0, Number(totalPages) || 0), totalElements, page };
}
