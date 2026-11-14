import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getCategory } from "../../api/category/categoryApi.js";
import { categorySearchPath } from "../common/catalogPaths.js";

export default function CategorySearchLink({ categoryId }) {
  const [category, setCategory] = useState(null);
  useEffect(() => {
    const request = new AbortController();
    getCategory(categoryId, { signal: request.signal, skipAuth: true }).then((item) => {
      if (!request.signal.aborted && item?.id === categoryId && item.slug && item.status === "ACTIVE") setCategory(item);
    }).catch(() => {});
    return () => request.abort();
  }, [categoryId]);
  if (!category) return null;
  return <Link to={categorySearchPath(category)} className="inline-flex min-h-11 w-fit items-center rounded text-sm font-semibold text-primary hover:underline">Khám phá {category.name} <span aria-hidden="true" className="ml-2">→</span></Link>;
}
