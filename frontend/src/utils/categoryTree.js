/**
 * API'den düz liste gelen kategorileri ana (parentId null) ve çocuklar map'ine ayırır.
 */
export function buildCategoryGroups(categories) {
  const list = Array.isArray(categories) ? categories : [];
  const roots = list
    .filter((c) => c.parentId == null)
    .slice()
    .sort((a, b) => String(a.name).localeCompare(String(b.name), 'tr'));
  const childrenByParent = new Map();
  for (const c of list) {
    if (c.parentId != null) {
      const pid = Number(c.parentId);
      if (!childrenByParent.has(pid)) childrenByParent.set(pid, []);
      childrenByParent.get(pid).push(c);
    }
  }
  for (const arr of childrenByParent.values()) {
    arr.sort((a, b) => String(a.name).localeCompare(String(b.name), 'tr'));
  }
  return { roots, childrenByParent };
}

export function getChildrenOf(parentId, childrenByParent) {
  return childrenByParent.get(Number(parentId)) || [];
}
